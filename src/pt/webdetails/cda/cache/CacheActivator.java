package pt.webdetails.cda.cache;

import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler.QuartzSystemListener;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.quartz.Scheduler;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import pt.webdetails.cda.utils.PluginHibernateUtil;

/**
 *
 * @author pdpi
 */
public class CacheActivator implements IAcceptsRuntimeInputs
{

  static final String TRIGGER_NAME = "cacheWarmer";
  static final String JOB_GROUP = "CDA";
  static final String JOB_ACTION = "scheduler.xaction";
  static final long ONE_HOUR = 3600000; // In miliseconds


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
    /* the first thing we do is proactively reschedule this action to one hour
     * from now, to ensure that, if for some reason the queue fails to reschedule
     * after excuting all due queries, we'll still recover at some point in the
     * future.
     */
    Date anHourFromNow = new Date(rightNow.getTime() + ONE_HOUR);
    reschedule(anHourFromNow);
    try
    {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

      s.beginTransaction();

      PriorityQueue<CachedQuery> queue = CacheManager.getInstance().getQueue();

      while (queue.peek().getNextExecution().before(rightNow))
      {
        processQueries(s, queue);
        rightNow = new Date();
      }
      reschedule(queue);

      return true;
    }
    catch (Exception e)
    {
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

    CacheManager.logger.debug("Refreshing cached query...");
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
      CacheManager.logger.error("Failed to execute " + q.toString());
    }


    q.updateNext();
    queue.add(q);
    s.update(q);
  }


  public static void reschedule(PriorityQueue<CachedQuery> queue)
  {
    CachedQuery q = queue.peek();

    Date dueAt = q.getNextExecution();
    IPentahoSession session = new StandaloneSession("CDA");
    Scheduler sched = QuartzSystemListener.getSchedulerInstance();

    SchedulerHelper.deleteJob(session, JOB_ACTION, JOB_GROUP);
    SchedulerHelper.createSimpleTriggerJob(session, "system", "cda/actions", JOB_ACTION, TRIGGER_NAME, JOB_GROUP, "", dueAt, null, 0, 0);


  }


  public static void reschedule(Date date)
  {

    IPentahoSession session = new StandaloneSession("CDA");
    Scheduler sched = QuartzSystemListener.getSchedulerInstance();

    SchedulerHelper.deleteJob(session, JOB_ACTION, JOB_GROUP);
    SchedulerHelper.createSimpleTriggerJob(session, "system", "cda/actions", JOB_ACTION, TRIGGER_NAME, JOB_GROUP, "", date, null, 0, 0);


  }


  public static void setSession(CachedQuery q)
  {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    String user = q.getUserName();
    UserSession session = new UserSession(user, null, false, null);
    GrantedAuthority[] auths = userDetailsRoleListService.getUserRoleListService().getAuthoritiesForUser(user);
    Authentication auth = new UsernamePasswordAuthenticationToken(user, null, auths);
    session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
    session.doStartupActions(null);
    PentahoSessionHolder.setSession(session);
  }
}
