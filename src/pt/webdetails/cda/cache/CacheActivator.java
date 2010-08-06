package pt.webdetails.cda.cache;

import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.scheduler.QuartzSystemListener;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.quartz.Scheduler;

/**
 *
 * @author pdpi
 */
public class CacheActivator implements IAcceptsRuntimeInputs
{

  static final String TRIGGER_NAME = "cacheWarmer";
  static final String JOB_GROUP = "CDA";
  static final String JOB_ACTION = "scheduler.xaction";


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
    try
    {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    Session s = HibernateUtil.getSession();
    PriorityQueue<CachedQuery> queue = CacheManager.getInstance().getQueue();
    Date rightNow = new Date();
    while (queue.peek().getNextExecution().before(rightNow))
    {
      processQueries(queue);
    }
    reschedule(queue);
    s.flush();
    return true;}
    catch (Exception e)
    {
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(contextCL);
      return true;
    }
  }


  public boolean validate() throws Exception
  {
    return true;
  }


  public void processQueries(PriorityQueue<CachedQuery> queue)
  {
    CachedQuery q = queue.poll();
    try
    {
      q.execute();
      q.updateNext();
      queue.add(q);
    }
    catch (Exception ex)
    {
      CacheManager.logger.error("Failed to execute " + q.toString());
    }
    CacheManager.logger.debug("Refreshing cached query...");
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
}
