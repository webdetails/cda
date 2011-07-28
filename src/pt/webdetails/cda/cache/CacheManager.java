/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;

import org.apache.commons.lang.StringUtils;
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
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.quartz.CronExpression;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.PluginHibernateException;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.JsonExporter;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cda.utils.framework.JsonCallHandler;

/**
 *
 * @author pdpi
 */
public class CacheManager extends JsonCallHandler
{

  static Log logger = LogFactory.getLog(CacheManager.class);
  final String PLUGIN_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + CdaContentGenerator.PLUGIN_NAME);
  public static int DEFAULT_MAX_AGE = 3600;  // 1 hour
  PriorityQueue<CachedQuery> queue;
  
//  private static final String ENCODING = "UTF-8";

  enum functions
  {
    LIST, CHANGE, RELOAD, DELETE, PERSIST, MONITOR, DETAILS, TEST, EXECUTE, IMPORT, 
    CACHED, GETDETAILS, CACHEOVERVIEW, REMOVECACHE
  }
  
  private static CacheManager _instance;


  public static synchronized CacheManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new CacheManager();
    }
    return _instance;
  }


  public CacheManager()
  {
    initialize();
  }


  public void handleCall(IParameterProvider requestParams, OutputStream out)
  {
    String method = requestParams.getParameter("method").toString();
    try
    {
      switch (functions.valueOf(method.toUpperCase()))
      {
        case CHANGE:
          change(requestParams, out);
          break;
        case RELOAD:
          load(requestParams, out);
          break;
        case LIST:
          list(requestParams, out);
          break;
        case EXECUTE:
          execute(requestParams, out);
          break;
        case DELETE:
          delete(requestParams, out);
          break;
        case IMPORT:
          importQueries(requestParams, out);
          break;
        default:
          super.handleCall(requestParams, out);
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
    registerMethods();
    try
    {
      initHibernate();
      initQueue();
    }
    catch (PluginHibernateException ex)
    {
      logger.warn("Found PluginHibernateException while initializing CacheManager " + Util.getExceptionDescription(ex));
    }
  }
  

  private void registerMethods()
  {
    
    registerMethod("cached", new JsonCallHandler.Method() 
    {
      /**
       * get all cached items for given cda file and data access id
       */
      public JSONObject execute(IParameterProvider params) throws JSONException, ExporterException, IOException {
        String cdaSettingsId = params.getStringParameter("cdaSettingsId", null);
        String dataAccessId = params.getStringParameter("dataAccessId", null);
        return listQueriesInCache(cdaSettingsId, dataAccessId);
      }
    });
    
    registerMethod("cacheOverview", new JsonCallHandler.Method() 
    {
      /**
       * get details on a particular cached item
       */
      public JSONObject execute(IParameterProvider params) throws JSONException 
      {
        return getCachedQueriesOverview();
      }
    });
    
    registerMethod("getDetails", new JsonCallHandler.Method() 
    {
      /**
       * get details on a particular cached item
       */
      public JSONObject execute(IParameterProvider params) throws UnsupportedEncodingException, JSONException, ExporterException, IOException, ClassNotFoundException  
      {
        try{
          String encodedCacheKey=params.getStringParameter("key", null);
          return getcacheQueryTable(encodedCacheKey);
        }
        catch(ExporterException e){
          logger.error( "Error exporting table.", e);
          return createJsonResultFromException(e);
        }
      }
    });
    
    registerMethod("removeCache", new JsonCallHandler.Method() 
    {
      /**
       * Remove item from cache 
       */
      public JSONObject execute(IParameterProvider params) throws JSONException, UnsupportedEncodingException, IOException, ClassNotFoundException 
      {
        String serializedCacheKey = params.getStringParameter("key", null);
        return removeQueryFromCache(serializedCacheKey);
      }
    });
  }


  public void render(IParameterProvider requestParams, OutputStream out)
  {
    //TODO:
  }


  private void load(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    Session s = getHibernateSession();
    Long id = Long.decode(requestParams.getParameter("id").toString());
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


  private void monitor(IParameterProvider requestParams, OutputStream out)
  {
    return; //NYI!
  }


  private void persist(IParameterProvider requestParams, OutputStream out) throws Exception
  {

    Long id = Long.decode(requestParams.getParameter("id").toString());
    Session s = getHibernateSession();
    s.beginTransaction();

    UncachedQuery uq = (UncachedQuery) s.load(UncachedQuery.class, id);
    CachedQuery cq = uq.cacheMe();
    if (uq != null)
    {
      s.delete(s);
    }
    JSONObject json = uq.toJSON();
    out.write(json.toString(2).getBytes(ENCODING));
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


  private void change(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    String jsonString = requestParams.getParameter("object").toString();
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
          out.write("{\"status\": \"error\", \"message\": \"failed to parse Cron String\"}".getBytes(ENCODING));
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
      out.write("".getBytes(ENCODING));
    }

    out.write("{\"status\": \"ok\"}".getBytes(ENCODING));
  }


  private void list(IParameterProvider requestParams, OutputStream out) throws PluginHibernateException
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

  private void importQueries(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    String jsonString = requestParams.getParameter("object").toString();
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


  private void execute(IParameterProvider requestParams, OutputStream out) throws PluginHibernateException
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
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


  private void delete(IParameterProvider requestParams, OutputStream out) throws PluginHibernateException
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
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
      return;
    }
    //}
  }


  /**
   * Initializes the CacheManager from a cold boot. Ensures all essential cached queries
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
   * Re-initializes the CacheManager after. Should be called after a plug-in installation
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
  

  
  private static class ResultFields extends JsonCallHandler.JsonResultFields {
  //  public static final String TABLE = "table";
    public static final String CDA_SETTINGS_ID = "cdaSettingsId";
    public static final String DATA_ACCESS_ID = "dataAccessId";
    public static final String COUNT = "count";
    public static final String ITEMS = "items";
   // public static final String RESULTS = "results";
  }
  
  private static JSONObject getcacheQueryTable(String encodedCacheKey) throws JSONException, ExporterException, UnsupportedEncodingException, IOException, ClassNotFoundException {
    
    if(encodedCacheKey == null){
      throw new IllegalArgumentException("No cache key received.");
    }
    
    JSONObject result = new JSONObject();
    Cache cdaCache = AbstractDataAccess.getCache();

    TableCacheKey lookupCacheKey = TableCacheKey.getTableCacheKeyFromString(encodedCacheKey);
    net.sf.ehcache.Element elem = cdaCache.getQuiet(lookupCacheKey);

    if(elem != null){
      // put query results
      JsonExporter exporter = new JsonExporter(null);
      result.put(ResultFields.RESULT, exporter.getTableAsJson((TableModel) elem.getObjectValue(), 100));
      result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    }
    else {
      result.put(JsonResultFields.STATUS, ResponseStatus.ERROR);
      result.put(ResultFields.ERROR_MSG, "item not found");
    }
    
    return result;
    
  }
  
  private static JSONObject getCachedQueriesOverview() throws JSONException {
    
    HashMap<String, HashMap<String, Integer>> cdaMap = new HashMap<String, HashMap<String,Integer>>();
    
    Cache cdaCache = AbstractDataAccess.getCache();
    JSONArray results = new JSONArray();
    
    for(Object key : cdaCache.getKeys()) {

      TableCacheKey cacheKey = (TableCacheKey) key;
      String cdaSettingsId = cacheKey.getCdaSettingsId();
      String dataAccessId = cacheKey.getDataAccessId();
      
      //aggregate occurrences
      HashMap<String, Integer> dataAccessIdMap = cdaMap.get(cdaSettingsId);
      if( dataAccessIdMap == null ){
        dataAccessIdMap = new HashMap<String, Integer>();
        dataAccessIdMap.put(dataAccessId, 1);
        cdaMap.put(cdaSettingsId, dataAccessIdMap);
      }
      else {
        Integer count = dataAccessIdMap.get(dataAccessId);
        if(count == null){
          dataAccessIdMap.put(dataAccessId, 1);
        }
        else {
          dataAccessIdMap.put(dataAccessId, ++count);
        }
      }
    }
    
    for(String cdaSettingsId :  cdaMap.keySet()){
      for(String dataAccessId : cdaMap.get(cdaSettingsId).keySet() ){
        Integer count = cdaMap.get(cdaSettingsId).get(dataAccessId);
        JSONObject queryInfo = new JSONObject();
        queryInfo.put(ResultFields.CDA_SETTINGS_ID, cdaSettingsId);
        queryInfo.put(ResultFields.DATA_ACCESS_ID, dataAccessId); 
        queryInfo.put(ResultFields.COUNT, count.intValue());
        
        results.put(queryInfo);
      }
    }
    
    JSONObject result = new JSONObject();
    result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    result.put(ResultFields.RESULT, results);
    return result;
  }
  
  private static JSONObject removeQueryFromCache(String serializedCacheKey) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException 
  {
    TableCacheKey key = TableCacheKey.getTableCacheKeyFromString(serializedCacheKey);
    
    Cache cdaCache = AbstractDataAccess.getCache();
    boolean success = cdaCache.remove(key);
    
    JSONObject result = new JSONObject();
    if(success){
      result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    }
    else {
      result.put(JsonResultFields.STATUS, ResponseStatus.ERROR);
      result.put(JsonResultFields.ERROR_MSG, "Item not found in cache.");
    }
    return result;
  }
  
  private static JSONObject listQueriesInCache(String cdaSettingsId, String dataAccessId) throws JSONException, ExporterException, IOException {
    
    JSONArray results = new JSONArray();
    
    Cache cdaCache = AbstractDataAccess.getCache();
    
    for(Object key : cdaCache.getKeys()) {
      
      if(key instanceof TableCacheKey){
        
        JSONObject queryInfo = new JSONObject();
        
        TableCacheKey cacheKey = (TableCacheKey) key;
        
        if(!StringUtils.equals(cdaSettingsId, cacheKey.getCdaSettingsId()) ||
           !StringUtils.equals(dataAccessId, cacheKey.getDataAccessId()))
        {//not what we're looking for
          continue;
        }
        
        //query
        queryInfo.put("query", cacheKey.getQuery());
        //parameters
        ParameterDataRow prow = cacheKey.getParameterDataRow();
        JSONObject parameters = new JSONObject();
        if(prow != null) for(String paramName : prow.getColumnNames()){
          parameters.put(paramName, prow.get(paramName));
        }
        queryInfo.put("parameters", parameters);
        
        //cacheKey.query;
        net.sf.ehcache.Element elem = cdaCache.getQuiet(key);
        
        if(elem != null){
                  
          TableModel tableModel = (TableModel) elem.getObjectValue();
          queryInfo.put("rows", tableModel.getRowCount());
          
          //inserted
          queryInfo.put("inserted", elem.getLatestOfCreationAndUpdateTime());
          queryInfo.put("accessed", elem.getLastAccessTime());
          queryInfo.put("hits", elem.getHitCount()); 
          
          //use id to get table;
          //identifier
          String identifier = TableCacheKey.getTableCacheKeyAsString(cacheKey);
          queryInfo.put("key", identifier);
          
          results.put(queryInfo);
        }
      }
      else {
        logger.warn("Found non-TableCacheKey object in cache, skipping...");
      }
    }
    
    JSONObject result = new JSONObject();
    result.put(ResultFields.CDA_SETTINGS_ID, cdaSettingsId);
    result.put(ResultFields.DATA_ACCESS_ID, dataAccessId);
    result.put(ResultFields.ITEMS, results);
    //total stats
    result.put("cacheLength", cdaCache.getSize());
    result.put("memoryStoreLength", cdaCache.getMemoryStoreSize());
    result.put("diskStoreLength", cdaCache.getDiskStoreSize());
    
    JSONObject response = new JSONObject();
    response.put(ResultFields.STATUS, ResponseStatus.OK);
    response.put(ResultFields.RESULT, result);
    
    return response;
  }
  
  
}
