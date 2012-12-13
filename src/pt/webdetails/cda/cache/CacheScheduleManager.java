/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Session;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cda.CdaContentGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.quartz.CronExpression;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.PluginHibernateException;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 *
 * @author pdpi
 */
public class CacheScheduleManager
{

  private static final String ENCODING = "UTF-8";
  static Log logger = LogFactory.getLog(CacheScheduleManager.class);
  final String PLUGIN_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + CdaContentGenerator.PLUGIN_NAME);
  public static int DEFAULT_MAX_AGE = 3600;  // 1 hour
  PriorityQueue<CachedQuery> queue;
  
//  private static final String ENCODING = "UTF-8";

  enum functions
  {
    LIST, CHANGE, RELOAD, DELETE, PERSIST, MONITOR, DETAILS, TEST, EXECUTE, IMPORT, 
    CACHED, GETDETAILS, CACHEOVERVIEW, REMOVECACHE
  }
  
  private static CacheScheduleManager _instance;


  public static synchronized CacheScheduleManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new CacheScheduleManager();
    }
    return _instance;
  }


  public CacheScheduleManager()
  {
    initialize();
  }


  public void handleCall(HttpServletRequest request, HttpServletResponse response)
  {
    String method = request.getParameter("method").toString();
    try
    {
      switch (functions.valueOf(method.toUpperCase()))
      {
        case CHANGE:
          change(request, response);
          break;
        case RELOAD:
          load(request, response);
          break;
        case LIST:
          list(request, response);
          break;
        case EXECUTE:
          execute(request, response);
          break;
        case DELETE:
          delete(request, response);
          break;
        case IMPORT:
          importQueries(request, response);
          break;
      }
    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }




  public void register(Query query)
  {
  }


  private void initialize()
  {
    try
    {
      initHibernate();
      initQueue();
    }
    catch (PluginHibernateException ex)
    {
      logger.warn("Found PluginHibernateException while initializing CacheScheduleManager " + Util.getExceptionDescription(ex));
    }
  }
  

  public void render(HttpServletRequest request, HttpServletResponse response)
  {
    //TODO:
  }


  private void load(HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    Session s = getHibernateSession();
    Long id = Long.decode(request.getParameter("id").toString());
    Query q = (Query) s.load(Query.class, id);
    if (q == null)
    {
      response.getOutputStream().write("{}".getBytes(ENCODING));
      logger.error("Couldn' get Query with id=" + id.toString());
    }
    try
    {
      JSONObject json = q.toJSON();
      response.getOutputStream().write(json.toString(2).getBytes(ENCODING));
      response.getOutputStream().flush();
    }
    catch (Exception e)
    {
      logger.error(e);
    }
    s.close();
  }


  private void monitor(HttpServletRequest request, HttpServletResponse response)
  {
    return; //NYI!
  }


  private void persist(HttpServletRequest request, HttpServletResponse response) throws Exception
  {

    Long id = Long.decode(request.getParameter("id").toString());
    Session s = getHibernateSession();
    s.beginTransaction();

    UncachedQuery uq = (UncachedQuery) s.load(UncachedQuery.class, id);
    CachedQuery cq = uq.cacheMe();
    if (uq != null)
    {
      s.delete(s);
    }
    JSONObject json = uq.toJSON();
    response.getOutputStream().write(json.toString(2).getBytes(ENCODING));
    s.flush();
    s.getTransaction().commit();
    s.close();
  }


  public void called(String file, String id, Boolean hit)
  {
    return; //not implemented yet!
    /*
    Session s = getSession();
    Query q;
    List l = s.createQuery("from CachedQuery where cdaFile=? and dataAccessId=?") //
    .setString(0, file) //
    .setString(1, id) //
    .list();
    
    if (l.size() == 0)
    {
    // No results, create a new (uncached) query object.
    q = new UncachedQuery();
    }
    else if (l.size() == 1)
    {
    q = (Query) l.get(0);
    }
    else
    {
    q = (Query) l.get(0);
    // Find correct params set
    //
    }
    q.registerRequest(hit);
    s.save(q);
     */
  }


  private void change(HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    String jsonString = request.getParameter("object").toString();
    JSONTokener jsonTokener = new JSONTokener(jsonString);
    try
    {
      Query q;
      JSONObject json = new JSONObject(jsonTokener);
      if (json.has("cronString"))
      {
        String cronString = json.getString("cronString");
        try
        {
          CronExpression ce = new CronExpression(cronString);
        }
        catch (Exception e)
        {
          logger.error("Failed to parse Cron string \"" + cronString + "\"");
          response.getOutputStream().write("{\"status\": \"error\", \"message\": \"failed to parse Cron String\"}".getBytes(ENCODING));
          response.getOutputStream().flush();
          return;
        }
        q = new CachedQuery(json);
        if (q != null)
        {
          queue.add((CachedQuery) q);
          CacheActivator.reschedule(queue);
        }
      }
      else
      {
        q = new UncachedQuery(json);
      }

      Session s = getHibernateSession();
      s.beginTransaction();
      s.save(q);
      s.flush();
      s.getTransaction().commit();
      s.close();

    }
    catch (JSONException jse)
    {
      response.getOutputStream().write("".getBytes(ENCODING));
      
    }

    response.getOutputStream().write("{\"status\": \"ok\"}".getBytes(ENCODING));
    response.getOutputStream().flush();
  }


  private void list(HttpServletRequest request, HttpServletResponse response) throws PluginHibernateException
  {
    JSONObject list = new JSONObject();
    JSONObject meta = new JSONObject();
    JSONArray queries = new JSONArray();
    Session s = getHibernateSession();
    List l = s.createQuery("from CachedQuery").list();
    for (Object o : l)
    {
      queries.put(((Query) o).toJSON());
    }
    try
    {

      meta.put("nextExecution", queue.size() > 0 ? queue.peek().getNextExecution().getTime() : 0);
      list.put("queries", queries);
      list.put("meta", meta);
    }
    catch (Exception e)
    {
      logger.error(e);
    }
    try
    {
      response.getOutputStream().write(list.toString(2).getBytes(ENCODING));
      response.getOutputStream().flush();
    }
    catch (Exception e)
    {
      logger.error(e);
    }
    finally
    {
      s.close();
    }
  }

  private void importQueries(HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    String jsonString = request.getParameter("object").toString();
    JSONTokener jsonTokener = new JSONTokener(jsonString);
    try
    {
      Query q;
      JSONObject json;
      Session s = getHibernateSession();
      s.beginTransaction();
      JSONObject root = new JSONObject(jsonTokener);
      JSONArray ja = root.getJSONArray("queries");
      for (int i = 0; i < ja.length(); i++)
      {
        json = ja.getJSONObject(i);
        if (json.has("cronString"))
        {
          q = new CachedQuery(json);
          queue.add((CachedQuery) q);
          CacheActivator.reschedule(queue);
        }
        else
        {
          q = new UncachedQuery(json);
        }
        s.save(q);

      }
      s.flush();
      s.getTransaction().commit();
      s.close();
    }
    catch (JSONException jse)
    {
      logger.error("Error importing queries: " + Util.getExceptionDescription(jse));
      response.getOutputStream().write("".getBytes(ENCODING));
      response.getOutputStream().flush();
    }
  }


  private void execute(HttpServletRequest request, HttpServletResponse response) throws PluginHibernateException
  {
    Long id = Long.decode(request.getParameter("id").toString());
    Session s = getHibernateSession();
    CachedQuery q = (CachedQuery) s.load(CachedQuery.class, id);

    if (q == null)
    {
      // Query doesn't exist or is not set for auto-caching
      return;
    }
    try
    {
      q.execute();
      q.updateNext();
      CacheActivator.reschedule(queue);
      response.getOutputStream().write("{\"status\": \"ok\"}".getBytes(ENCODING));
      response.getOutputStream().flush();
    }
    catch (Exception ex)
    {
      logger.error(ex);
      try
      {
        response.getOutputStream().write("{\"status\": \"error\"}".getBytes(ENCODING));
        response.getOutputStream().flush();
      }
      catch (Exception ex1)
      {
        logger.error(ex1);
      }
    }
    finally
    {
      s.beginTransaction();
      s.update(q);
      s.flush();
      s.getTransaction().commit();
      s.close();
    }
  }


  private void delete(HttpServletRequest request, HttpServletResponse response) throws PluginHibernateException
  {
    Long id = Long.decode(request.getParameter("id").toString());
    Session s = getHibernateSession();
    s.beginTransaction();

    Query q = (Query) s.load(Query.class, id);
    s.delete(q);

    for (CachedQuery cq : queue)
    {
      if (cq.getId() == id)
      {
        queue.remove(cq);
      }
    }

    s.flush();
    s.getTransaction().commit();
    s.close();
  }


  private Session getHibernateSession() throws PluginHibernateException
  {

    return PluginHibernateUtil.getSession();

  }

  class SortByTimeDue implements Comparator<CachedQuery>
  {

    public int compare(CachedQuery o1, CachedQuery o2)
    {
      return (int) (o1.getNextExecution().getTime() - o2.getNextExecution().getTime());
    }
  }


  private void initQueue() throws PluginHibernateException
  {
    Session s = getHibernateSession();
    s.beginTransaction();

    List l = s.createQuery("from CachedQuery").list();
    this.queue = new PriorityQueue<CachedQuery>(20, new SortByTimeDue());
    for (Object o : l)
    {
      CachedQuery cq = (CachedQuery) o;
      if (cq.getLastExecuted() == null)
      {
        cq.setLastExecuted(new Date(0L));
      }
      Date nextExecution;
      try
      {
        nextExecution = new CronExpression(cq.getCronString()).getNextValidTimeAfter(new Date());
      }
      catch (ParseException ex)
      {
        nextExecution = new Date(0);
        logger.error("Failed to schedule " + cq.toString());
      }
      cq.setNextExecution(nextExecution);
      this.queue.add(cq);

      s.save(cq);
    }

    s.flush();
    s.getTransaction().commit();
    s.close();
  }


  public static void initHibernate() throws PluginHibernateException
  {

    PluginHibernateUtil.initialize();

    // Get hbm file
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    InputStream in = resLoader.getResourceAsStream(CacheScheduleManager.class, "cachemanager.hbm.xml");
    
/*    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
    ClassLoader classLoader = pluginManager.getClassLoader(CdaContentGenerator.PLUGIN_NAME);

    InputStream inputStream = resLoader.getResourceAsStream(classLoader, "cachemanager.hbm.xml");
    
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(inputStream, writer, "UTF-8");
    } catch (IOException ex){
      return;
    }
    
    String content = writer.toString();
  */  
    // Close session and rebuild
    PluginHibernateUtil.closeSession();
    org.hibernate.cfg.Configuration configuration = PluginHibernateUtil.getConfiguration();
    configuration.addInputStream(in);
  //  configuration.addFile(RepositoryAccess.getSystemDir() + "/cda/cachemanager.hbm.xml");
    PluginHibernateUtil.rebuildSessionFactory();
  }


  /**
   * Initializes the CacheScheduleManager from a cold boot. Ensures all essential cached queries
   * are populated at boot time, and sets up the first query timer.
   */
  public void coldInit() throws PluginHibernateException
  {


    Configuration config = CdaBoot.getInstance().getGlobalConfig();
    String executeAtStart = config.getConfigProperty("pt.webdetails.cda.cache.executeAtStart");
    if (executeAtStart.equals("true"))
    {
      IPentahoSession session = new StandaloneSession("CDA");

      // run all queries
      Session s = getHibernateSession();
      List<CachedQuery> cachedQueries = s.createQuery("from CachedQuery").list();
      for (CachedQuery cq : cachedQueries)
      {
        try
        {
          cq.execute();
        }
        catch (Exception ex)
        {
          logger.error("Error executing " + cq.toString() + ":" + ex.toString());
        }
      }
      s.close();
    }

    CacheActivator.reschedule(queue);
    CacheActivator.rescheduleBackup();
  }


  /**
   * Re-initializes the CacheScheduleManager after. Should be called after a plug-in installation
   * at runtime, to ensure the query queue is kept consistent
   */
  public void hotInit()
  {
    return; //NYI
  }


  public PriorityQueue<CachedQuery> getQueue()
  {
    return queue;
  }
  
  
}
