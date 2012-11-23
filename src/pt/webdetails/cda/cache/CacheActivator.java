/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Session;
import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
//import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.api.engine.IUserRoleListService;
//import org.pentaho.platform.scheduler.SchedulerHelper;
//import org.quartz.Scheduler;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.utils.PluginHibernateUtil;

/**
 *
 * @author pdpi
 */
public class CacheActivator implements IAcceptsRuntimeInputs
{

  static final String TRIGGER_NAME = "cacheWarmer";
  static final String BACKUP_TRIGGER_NAME = "backupCacheWarmer";
  static final String JOB_GROUP = "CDA";
  static final String JOB_ACTION = "scheduler.xaction";
  static final String BACKUP_JOB_ACTION = "backupScheduler.xaction";
  static final String SESSION_PRINCIPAL = "SECURITY_PRINCIPAL";
  static final long ONE_HOUR = 3600000; // In miliseconds
  
  private static final Log logger = LogFactory.getLog(CacheActivator.class);


  public CacheActivator()
  {
  }


  public void setInputs(Map<String, Object> map)
  {
    return;
  }


  public boolean execute() throws Exception
  {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    Session s = PluginHibernateUtil.getSession();
    Date rightNow = new Date();

    try
    {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

      s.beginTransaction();
      /* If there's any work at all to be done, the first thing we do is proactively
       * reschedule this action to one hour from now, to ensure that, if for some
       * reason the queue fails to reschedule after excuting all due queries, we'll
       * still recover at some point in the future.
       */
      PriorityQueue<CachedQuery> queue = CacheScheduleManager.getInstance().getQueue();
      if (queue.peek().getNextExecution().before(rightNow))
      {
        Date anHourFromNow = new Date(rightNow.getTime() + ONE_HOUR);
        reschedule(anHourFromNow);
      }
      else
      {
        CacheScheduleManager.logger.info("No work to be done");
      }
      while (queue.peek().getNextExecution().before(rightNow))
      {
        processQueries(s, queue);
        rightNow = new Date();
      }
      reschedule(queue);

      return true;
    }
    catch (Exception e)
    {//TODO: shouldn't we rollback here?
    }
    finally
    {
      s.flush();
      s.getTransaction().commit();
      s.close();
      Thread.currentThread().setContextClassLoader(contextCL);
      return true;
    }
  }


  public boolean validate() throws Exception
  {
    return true;
  }


  public void processQueries(Session s, PriorityQueue<CachedQuery> queue)
  {

    CacheScheduleManager.logger.debug("Refreshing cached query...");
    CachedQuery q = queue.poll();
    try
    {
      IPentahoSession session = PentahoSessionHolder.getSession();
      s.refresh(q);
      setSession(q);
      q.execute();
      PentahoSessionHolder.setSession(session);
    }
    catch (Exception ex)
    {
      CacheScheduleManager.logger.error("Failed to execute " + q.toString());
    }


    q.updateNext();
    queue.add(q);
    s.update(q);
  }


  public static void reschedule(PriorityQueue<CachedQuery> queue)
  {
    CachedQuery q = queue.peek();

    Date dueAt = ( q != null)? q.getNextExecution() : null;
    //Scheduler sched = QuartzSystemListener.getSchedulerInstance(); unused
    IScheduler scheduler = PentahoSystem.get(IScheduler.class, "IScheduler2", null);
    
    //should we use cda name?
    try {
      scheduler.removeJob(q.getCdaFile());
      scheduler.createJob(q.getCdaFile(), JOB_GROUP, null, JobTrigger.ONCE_NOW);
    } catch(SchedulerException ex){
      logger.error(ex);
    }
    
    //SchedulerHelper.deleteJob(session, JOB_ACTION, JOB_GROUP);
    //SchedulerHelper.createSimpleTriggerJob(session, "system", "cda/actions", JOB_ACTION, TRIGGER_NAME, JOB_GROUP, "", dueAt, null, 0, 0);

  }


  public static void reschedule(Date date)
  {

    IPentahoSession session = new StandaloneSession(JOB_GROUP);
    IScheduler scheduler = PentahoSystem.get(IScheduler.class, "IScheduler2", null);
    
    //what name should be used?
    try {
      scheduler.removeJob("");
      scheduler.createJob("", JOB_GROUP, null, JobTrigger.ONCE_NOW);
    } catch(SchedulerException ex){
      logger.error(ex);
    }
    
    //SchedulerHelper.deleteJob(session, JOB_ACTION, JOB_GROUP);
    //SchedulerHelper.createSimpleTriggerJob(session, "system", "cda/actions", JOB_ACTION, TRIGGER_NAME, JOB_GROUP, "", date, null, 0, 0);


  }


  public static void setSession(CachedQuery q)
  {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    String user = q.getUserName();
    UserSession session = new UserSession(user, null, false, null);
    List<String> auths = userRoleListService.getRolesForUser(null, user);

    GrantedAuthority[] grantedAuthorities = new GrantedAuthority[auths.size()];
    if (!auths.isEmpty()) {
      for (int i = 0; i < auths.size(); i++) {
        grantedAuthorities[i] = new GrantedAuthorityImpl(auths.get(i));
      }
    }
    
    Authentication auth = new UsernamePasswordAuthenticationToken(user, null, grantedAuthorities);
    session.setAttribute(SESSION_PRINCIPAL, auth);
    session.doStartupActions(null);
    PentahoSessionHolder.setSession(session);
  }


  public static void rescheduleBackup()
  {
    String cron = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.cache.backupWarmerCron");
    cron = cron == null ? "0 0 0/30 * * ?" : cron;
    
    IPentahoSession session = new StandaloneSession(JOB_GROUP);
    IScheduler scheduler = PentahoSystem.get(IScheduler.class, "IScheduler2", null);
    
    //SchedulerHelper.deleteJob(session, BACKUP_JOB_ACTION, JOB_GROUP);
    //SchedulerHelper.createCronJob(session, "system", "cda/actions", BACKUP_JOB_ACTION, BACKUP_TRIGGER_NAME, JOB_GROUP, "", cron);
  }
}
