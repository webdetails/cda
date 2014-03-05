/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

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
//import org.pentaho.platform.api.engine.IPentahoSession;
//import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.reporting.libraries.base.config.Configuration;
import org.quartz.CronExpression;
//import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.PluginHibernateException;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;

/**
 *
 * @author pdpi
 */
public class CacheScheduleManager
{

  private static final String ENCODING = "UTF-8";
  private static Log logger = LogFactory.getLog(CacheScheduleManager.class);
  final String PLUGIN_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + CdaContentGenerator.PLUGIN_NAME);
  public static int DEFAULT_MAX_AGE = 3600;  // 1 hour
  PriorityBlockingQueue<CachedQuery> queue;
  
//  private static final String ENCODING = "UTF-8";

  enum functions
  {
    LIST, CHANGE, RELOAD, DELETE, EXECUTE, IMPORT, 
//    CACHED, GETDETAILS, CACHEOVERVIEW, REMOVECACHE PERSIST, MONITOR,TEST, DETAILS, 
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


  public void handleCall(final String method,final String obj, OutputStream out)
  {
    try
    {
      switch (functions.valueOf(method.toUpperCase()))
      {
        case CHANGE:
          change(obj, out);
          break;
        case RELOAD:
          load(obj, out);
          break;
        case LIST:
          list(out);
          break;
        case EXECUTE:
          execute(obj, out);
          break;
        case DELETE:
          delete(obj, out);
          break;
        case IMPORT:
          importQueries(obj, out);
          break;
        default:
          logger.error( "Unhandled method " + method );
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
  

  public void render(IParameterProvider requestParams, OutputStream out)
  {
    //TODO:
  }


  private void load(String obj, OutputStream out) throws Exception
  {
    Session s = getHibernateSession();
    Long id = Long.decode(obj);
    Query q = (Query) s.load(Query.class, id);
    if (q == null)
    {
      out.write("{}".getBytes(ENCODING));
      logger.error("Couldn' get Query with id=" + id.toString());
    }
    try
    {
      JSONObject json = q.toJSON();
      out.write(json.toString(2).getBytes(ENCODING));
    }
    catch (Exception e)
    {
      logger.error(e);
    }
    s.close();
  }

//
//  private void monitor(IParameterProvider requestParams, OutputStream out)
//  {
//    return; //NYI!
//  }


//  private void persist(IParameterProvider requestParams, OutputStream out) throws Exception
//  {
//
//    Long id = Long.decode(requestParams.getParameter("id").toString());
//    Session s = getHibernateSession();
//    s.beginTransaction();
//
//    UncachedQuery uq = (UncachedQuery) s.load(UncachedQuery.class, id);
//    CachedQuery cq = uq.cacheMe();
//    if (uq != null)
//    {
//      s.delete(s);
//    }
//    JSONObject json = uq.toJSON();
//    out.write(json.toString(2).getBytes(ENCODING));
//    s.flush();
//    s.getTransaction().commit();
//    s.close();
//  }


//  public void called(String file, String id, Boolean hit)
//  {
//    return; //not implemented yet!
//    /*
//    Session s = getSession();
//    Query q;
//    List l = s.createQuery("from CachedQuery where cdaFile=? and dataAccessId=?") //
//    .setString(0, file) //
//    .setString(1, id) //
//    .list();
//    
//    if (l.size() == 0)
//    {
//    // No results, create a new (uncached) query object.
//    q = new UncachedQuery();
//    }
//    else if (l.size() == 1)
//    {
//    q = (Query) l.get(0);
//    }
//    else
//    {
//    q = (Query) l.get(0);
//    // Find correct params set
//    //
//    }
//    q.registerRequest(hit);
//    s.save(q);
//     */
//  }


  private void change(String obj, OutputStream out) throws Exception
  {
    String jsonString = obj;
    JSONTokener jsonTokener = new JSONTokener(jsonString);
    try
    {
      Query query;
      JSONObject json = new JSONObject(jsonTokener);
      if (json.has("cronString"))
      {
        String cronString = json.getString("cronString");
        try
        {
          // test if parses ok
          @SuppressWarnings( "unused" )
          CronExpression ce = new CronExpression(cronString);
        }
        catch (Exception e)
        {
          logger.error("Failed to parse Cron string \"" + cronString + "\"");
          out.write("{\"status\": \"error\", \"message\": \"failed to parse Cron String\"}".getBytes(ENCODING));
          return;
        }
        query = new CachedQuery(json);
        if (query != null)
        {
          queue.add((CachedQuery) query);
          CacheActivator.reschedule(queue);
        }
      }
      else
      {
        query = new UncachedQuery(json);
      }

      Session session = getHibernateSession();
      session.beginTransaction();
      session.save(query);
      session.flush();
      session.getTransaction().commit();
      session.close();

    }
    catch (JSONException jse)
    {
      out.write("".getBytes(ENCODING));
    }

    out.write("{\"status\": \"ok\"}".getBytes(ENCODING));
  }


  private void list( OutputStream out) throws PluginHibernateException
  {
    JSONObject list = new JSONObject();
    JSONObject meta = new JSONObject();
    JSONArray queries = new JSONArray();
    Session s = getHibernateSession();

    @SuppressWarnings( "unchecked" )
    List<CachedQuery> cachedQueries = s.createQuery("from CachedQuery").list();
    for (CachedQuery query : cachedQueries)
    {
      queries.put(query.toJSON());
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
      out.write(list.toString(2).getBytes(ENCODING));
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

  private void importQueries(String obj, OutputStream out) throws Exception
  {
    String jsonString = obj;
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
      out.write("".getBytes(ENCODING));
    }
  }


  private void execute(String obj, OutputStream out) throws PluginHibernateException
  {
    Long id = Long.decode(obj);
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
      out.write("{\"status\": \"ok\"}".getBytes(ENCODING));
    }
    catch (Exception ex)
    {
      logger.error(ex);
      try
      {
        out.write("{\"status\": \"error\"}".getBytes(ENCODING));
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


  private void delete(String obj, OutputStream out) throws PluginHibernateException
  {
    Long id = Long.decode(obj);
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

    @SuppressWarnings( "unchecked" )
    List<CachedQuery> cachedQueries = s.createQuery( "from " + CachedQuery.class.getSimpleName() ).list(); //TODO: simple name?
    this.queue = new PriorityBlockingQueue<CachedQuery>(20, new SortByTimeDue());
    for (CachedQuery cq : cachedQueries)
    {
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

    // Get hbm file
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    InputStream in = resLoader.getResourceAsStream(CdaContentGenerator.class, "cachemanager.hbm.xml");

    // Close session and rebuild
    PluginHibernateUtil.closeSession();
    org.hibernate.cfg.Configuration configuration = PluginHibernateUtil.getConfiguration();
    //if (configuration.getClassMapping(CachedQuery.class.getCanonicalName()) == null)
    //{
    configuration.addInputStream(in);
    try
    {
      PluginHibernateUtil.rebuildSessionFactory();
    }
    catch (Exception e)
    {
      logger.error( "PluginHibernateUtil.rebuildSessionFactory", e );
      return;
    }
    //}
  }


  /**
   * Initializes the CacheScheduleManager from a cold boot. Ensures all essential cached queries
   * are populated at boot time, and sets up the first query timer.
   */
  public void coldInit() throws PluginHibernateException
  {


//    Configuration config = CdaBoot.getInstance().getGlobalConfig();
    String executeAtStart = CdaEngine.getInstance().getConfigProperty("pt.webdetails.cda.cache.executeAtStart");
    if (executeAtStart.equals("true"))
    {
//      IPentahoSession session = new StandaloneSession("CDA");

      // run all queries
      Session s = getHibernateSession();
      @SuppressWarnings( "unchecked" )
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
//    CacheActivator.rescheduleBackup();
  }


  /**
   * Re-initializes the CacheScheduleManager after. Should be called after a plug-in installation
   * at runtime, to ensure the query queue is kept consistent
   */
  public void hotInit()
  {
    return; //NYI
  }


  public Queue<CachedQuery> getQueue()
  {
    return queue;
  }
  
  
}
