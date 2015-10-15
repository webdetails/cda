/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.cache.scheduler;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Executes scheduled queries
 */
public class CdaCacheWarmer implements IVarArgsAction {

  private static final Log logger = LogFactory.getLog( CdaCacheWarmer.class );

  public static final String QUERY_INFO_PARAM = "cdaQuery";

  private CdaSettings cdaSettings;
  private QueryOptions queryOptions;
  private String jsonStringArg;

  public static CdaSettings getCdaSettings( CdaEngine engine, JSONObject json ) throws Exception {
    String cdaFile = json.getString( "cdaFile" );
    return engine.getSettingsManager().parseSettingsFile( cdaFile );
  }

  public static QueryOptions createQueryOptions( JSONObject json ) throws JSONException {
    QueryOptions queryOpts = new QueryOptions();
    queryOpts.setDataAccessId( json.getString( "dataAccessId" ) );
    if ( json.has( "parameters" ) ) {
      Object parameters = json.get( "parameters" );
      if ( parameters instanceof JSONArray ) {
        JSONArray parametersArray = (JSONArray) parameters;
        for ( int i = 0; i < parametersArray.length(); i++ ) {
          JSONObject param = parametersArray.getJSONObject( i );
          String name = param.getString( "name" );
          JSONArray arrayOpt = param.optJSONArray( "value" );
          if ( arrayOpt != null ) {
            // array parameter
            String[] values = new String[ ( arrayOpt.length() ) ];
            for ( int j = 0; j < values.length; j++ ) {
              values[ i ] = arrayOpt.getString( j );
            }
            queryOpts.addParameter( name, values );
          } else {
            // regular parameter
            String value = param.getString( "value" );
            queryOpts.addParameter( name, value );
          }
        }
      }
    }
    // ensure cache is refreshed
    queryOpts.setCacheBypass( true );
    return queryOpts;
  }

  @Override
  public void execute() throws Exception {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      if ( !loadArguments() ) {
        logger.error( "Not executed: unable to process arguments." );
        return;
      }
      if ( logger.isDebugEnabled() ) {
        logger.debug(
          String.format( "executing %s:%s\n\t %s",
            cdaSettings.getId(),
            queryOptions.getDataAccessId(),
            jsonStringArg )
        );
      } else {
        logger.info( String.format( "executing %s:%s...", cdaSettings.getId(), queryOptions.getDataAccessId() ) );
      }
      CdaEngine.getInstance().doQuery( cdaSettings, queryOptions );
    } catch ( Exception e ) {
      logger.error( "Execution failed.", e );
    } finally {
      Thread.currentThread().setContextClassLoader( contextClassLoader );
    }
  }

  @Override
  public void setVarArgs( Map<String, Object> args ) {
    if ( !args.containsKey( QUERY_INFO_PARAM ) ) {
      jsonStringArg = null;
    } else {
      try {
        jsonStringArg = (String) args.get( QUERY_INFO_PARAM );
      } catch ( ClassCastException e ) {
        logger.error( "Invalid parameter type", e );
      }
    }
  }

  private boolean loadArguments() {
    try {
      JSONObject json = new JSONObject( jsonStringArg );
      queryOptions = createQueryOptions( json );
      cdaSettings = getCdaSettings( CdaEngine.getInstance(), json );
      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format( "setVarArgs: %s = %s ", QUERY_INFO_PARAM, json ) );
      }
      return true;
    } catch ( JSONException e ) {
      logger.error( "Unable to parse JSON parameter", e );
    } catch ( UnifiedRepositoryAccessDeniedException e ) {
      // happens when invoked before startup
      logger.warn( "Access error, if problem persists check permissions." );
    } catch ( Exception e ) {
      logger.error( "Invalid parameters", e );
    }
    return false;
  }


}
