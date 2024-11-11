/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.cache.scheduler;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.services.BaseService;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.PentahoHelper;
import pt.webdetails.cpf.messaging.JsonGeneratorSerializable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CdaCacheScheduler extends BaseService {

  private static final String JOB_NAME_PREFIX = "CDA-sched/";
  private static final String SUGAR_SCHEDULER_BEAN_ID = "IScheduler2";
  private static final String ACTION_BEAN_ID = "CdaCacheWarmup";

  private static final String STATUS_OK = "ok";
  private static final String STATUS_ERROR = "error";
  private static final String STATUS_FIELD = "status";


  public JsonGeneratorSerializable scheduleQueryExecution( String jsonString ) {

    try {
      QueryExecution query = new QueryExecution( getSettingsManager(), jsonString );
      checkSchedulerAccess( query, PentahoSessionHolder.getSession() );
      final IJob job = scheduleQuery( query );
      if ( job != null ) {
        logJob( job );
        return new JsonGeneratorSerializable() {

          public void writeToGenerator( JsonGenerator out ) throws IOException {
            out.writeStartObject();
            out.writeStringField( STATUS_FIELD, STATUS_OK );
            out.writeStringField( "jobId", job.getJobId() );
            out.writeEndObject();
          }
        };
      }
    } catch ( Exception e ) {
      getLog().error( e );
      return getError( e );
    }
    return null;
  }

  public JsonGeneratorSerializable deleteJob( String id ) {
    try {
      checkJobAccess( id, PentahoSessionHolder.getSession() );
      IScheduler scheduler = getScheduler();
      scheduler.removeJob( id );
    } catch ( Exception e ) {
      return getError( e );
    }
    return new JsonGeneratorSerializable() {

      public void writeToGenerator( JsonGenerator out ) throws IOException {
        out.writeStartObject();
        out.writeStringField( STATUS_FIELD, STATUS_OK );
        out.writeEndObject();
      }
    };
  }

  public JsonGeneratorSerializable executeJob( String id ) {
    IScheduler scheduler = getScheduler();
    try {
      checkJobAccess( id, PentahoSessionHolder.getSession() );
      scheduler.triggerNow( id );
    } catch ( Exception e ) {
      return getError( e );
    }
    return new JsonGeneratorSerializable() {

      public void writeToGenerator( JsonGenerator out ) throws IOException {
        out.writeStartObject();
        out.writeStringField( STATUS_FIELD, STATUS_OK );
        out.writeEndObject();
      }
    };
  }

  /**
   * CacheController#list
   *
   * @return
   */
  public JsonGeneratorSerializable listScheduledQueries() {
    IScheduler scheduler = getScheduler();
    try {
      List<IJob> jobs = scheduler.getJobs( getCdaJobFilter() );
      final List<JsonGeneratorSerializable> queries = new ArrayList<>();
      for ( IJob job : jobs ) {
        addJobInfoToScheduledQueries( queries, job );
      }

      return new JsonGeneratorSerializable() {
        public void writeToGenerator( JsonGenerator out ) throws IOException {
          out.writeStartObject();
          out.writeStringField( STATUS_FIELD, STATUS_OK );
          out.writeFieldName( "queries" );
          out.writeStartArray();
          for ( JsonGeneratorSerializable query : queries ) {
            query.writeToGenerator( out );
          }
          out.writeEndArray();
          out.writeEndObject();
        }
      };
    } catch ( SchedulerException e ) {
      getLog().error( " Error fetching job list.", e );
    }
    return null;
  }

  private void addJobInfoToScheduledQueries( List<JsonGeneratorSerializable> queries, IJob job ) {
    try {
      String jsonString = (String) job.getJobParams().get( CdaCacheWarmer.QUERY_INFO_PARAM );
      queries.add( toCachedQueryJson( new ScheduledQueryExecution( getSettingsManager(), jsonString, job ) ) );
    } catch ( Exception e ) {
      getLog().error( "Error reading job info.", e );
    }
  }

  private SettingsManager getSettingsManager() {
    return CdaEngine.getInstance().getSettingsManager();
  }

  private IJobFilter getCdaJobFilter() {
    return job -> job.getJobName().startsWith( JOB_NAME_PREFIX )
      && job.getJobParams().containsKey( CdaCacheWarmer.QUERY_INFO_PARAM )
      // avoid listing temporary one-shot jobs from execute
      && job.getNextRun() != null && job.getNextRun().getTime() > 0;
  }

  private void checkSchedulerAccess( QueryExecution queryExec, IPentahoSession session ) throws AccessDeniedException {
    if ( !canSchedule( ) ) {
      throw new AccessDeniedException(
        String.format( "User %s cannot schedule %s",
          session.getName(),
          queryExec.getCdaSettings().getId() ), null
      );
    }
  }

  private void checkJobAccess( String jobId, IPentahoSession session )
    throws AccessDeniedException, SchedulerException {
    IScheduler scheduler = getScheduler();
    IJob job = scheduler.getJob( jobId );
    if ( job == null ) {
      throw new IllegalArgumentException( "No such job." );
    }
    if ( !session.getName().equals( job.getUserName() ) && !PentahoHelper.isAdmin( session ) ) {
      throw new AccessDeniedException( "User must be admin or be the job owner to access existing job ", null );
    }
  }

  private boolean canSchedule( ) {
    // access rights to cda already checked by SettingsManager
    IAuthorizationPolicy authPolicy = getAuthorizationPolicy();
    if ( authPolicy == null ) {
      getLog()
        .error( String.format( "Unable to get authorization policy (%s)", IAuthorizationPolicy.class.getName() ) );
      return false;
    }
    // we can also check for scheduling permissions for this particular file
    // just scheduling permissions and cda access should be enough
    return authPolicy.isAllowed( SchedulerAction.NAME );
  }

  private IAuthorizationPolicy getAuthorizationPolicy() {
    return PentahoSystem.get( IAuthorizationPolicy.class );
  }

  private IScheduler getScheduler() {
    // there are two IScheduler singletons, trying the spanking new one
    // TODO: this interface is from scheduler2, is the bean name really needed?
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, SUGAR_SCHEDULER_BEAN_ID, null );
    if ( scheduler == null ) {
      // and make sure when it changes we're not caught with our pants down
      getLog().warn( String.format( "Scheduler bean '%s' not found, falling back to default", SUGAR_SCHEDULER_BEAN_ID ) );
      return PentahoSystem.get( IScheduler.class );
    }
    return scheduler;
  }

  private IJob scheduleQuery( QueryExecution query ) {
    try {

      CdaSettings cdaSettings = query.getCdaSettings();
      QueryOptions queryOpts = query.getQueryOptions();
      String cron = query.getCronString();

      String jobName = getJobName( cdaSettings, queryOpts );
      IScheduler scheduler = getScheduler();
      HashMap<String, Object> actionParameters = new HashMap<>();
      actionParameters.put( CdaCacheWarmer.QUERY_INFO_PARAM, query.getJsonString() );
      IJobTrigger trigger = createTrigger( cron );
      return scheduler.createJob( jobName, ACTION_BEAN_ID, actionParameters, trigger );
    } catch ( SchedulerException e ) {
      getLog().error( "Unable to schedule query.", e );
    }
    return null;
  }

  public static String getCronString( JSONObject json ) throws JSONException {
    return json.getString( "cronString" );
  }

  private IJobTrigger createTrigger( String cron ) {
    // quartz cron strings are at least 6 whitespace-separated arguments
    String[] split = StringUtils.split( cron );
    if ( split == null || split.length < 6 ) {
      throw new IllegalArgumentException( "Illegal cron string." );
    }
    // in pentaho's cron parser the optional year argument is mandatory
    if ( split.length == 6 ) {
      cron += " *";
    }
    IScheduler scheduler = getScheduler();
    return scheduler.createComplexTrigger( cron );
  }

  private void logJob( IJob job ) {
    Log log = getLog();
    log.info( String.format( "Job id=\"%s\", name=\"%s\",  ", job.getJobId(), job.getJobName() ) );
    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "\t state=%s, next execution at %s, ", job.getState(), job.getNextRun() ) );
    }
  }

  public String getJobName( CdaSettings cda, QueryOptions options ) {
    // job names needn't be unique, job keys are formed with (jboName,user,currentTimeMs)
    StringBuilder sb = new StringBuilder();
    sb.append( JOB_NAME_PREFIX );
    sb.append( cda.getId() );
    sb.append( "/" );
    sb.append( options.getDataAccessId() );
    return sb.toString();
  }

  private static JsonGeneratorSerializable toCachedQueryJson( final ScheduledQueryExecution query ) {
    //
    return new JsonGeneratorSerializable() {

      public void writeToGenerator( JsonGenerator out ) throws IOException {
        IJob job = query.getJob();
        QueryOptions opts = query.getQueryOptions();
        out.writeStartObject();
        // basic info
        out.writeStringField( "id", job.getJobId() );
        out.writeStringField( "cdaFile", query.getCdaSettings().getId() );
        out.writeStringField( "dataAccessId", opts.getDataAccessId() );
        //paramters [
        out.writeObjectFieldStart( "parameters" );
        for ( Parameter param : opts.getParameters() ) {
          out.writeStringField( param.getName(), param.getStringValue() );
        }
        //]
        out.writeEndObject();
        // exec info
        out.writeNumberField( "nextExecution", ( job.getNextRun() != null ) ? job.getNextRun().getTime() : 0L );
        out.writeNumberField( "lastExecuted", ( job.getLastRun() != null ) ? job.getLastRun().getTime() : 0L );
        out.writeStringField( "cronString", job.getJobTrigger().getCronString() );

        // TODO: we can cross-ref with cache monitor to get missing fields if worth it
        out.writeNumberField( "timeElapsed", -1 );

        //XXX execution errors are not given by job status; only says if it wasn't scheduled properly
        out.writeBooleanField( "success", job.getState() == JobState.NORMAL );
        out.writeEndObject();
      }
    };
  }

  private JsonGeneratorSerializable getError( final Exception e ) {
    return new JsonGeneratorSerializable() {

      public void writeToGenerator( JsonGenerator out ) throws IOException {
        out.writeStartObject();
        out.writeStringField( STATUS_FIELD, STATUS_ERROR );
        out.writeStringField( "errorMsg", e.getLocalizedMessage() );
        out.writeEndObject();
      }
    };
  }
}

