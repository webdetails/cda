package pt.webdetails.cda.cache.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import com.hazelcast.core.MapEntry;
import com.hazelcast.impl.base.DataRecordEntry;

import pt.webdetails.cda.cache.HazelcastQueryCache;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.utils.framework.JsonCallHandler;

public class CacheMonitorHandler extends JsonCallHandler 
{

  private static Log logger = LogFactory.getLog(JsonCallHandler.class);
  
  private static CacheMonitorHandler _instance;

  public static synchronized CacheMonitorHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new CacheMonitorHandler();
    }
    return _instance;
  }


  public CacheMonitorHandler()
  {
    registerMethods();
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
    
    registerMethod("clusterInfo", new JsonCallHandler.Method() 
    {
      public JSONObject execute(IParameterProvider params) throws JSONException {
        return getOKJson(HazelcastCacheMonitor.getClusterInfo()); 
      }
    });
    
    registerMethod("mapInfo", new JsonCallHandler.Method() 
    {
      public JSONObject execute(IParameterProvider params) throws JSONException {
        return getOKJson(HazelcastCacheMonitor.getMapInfo()); 
      }
    });
    
//    registerMethod("shutdown", new JsonCallHandler.Method() {
//      
//      @Override
//      public JSONObject execute(IParameterProvider params) throws Exception {
//        if(get)
//      }
//    });
    
//    registerMethod("getElementDiskSize", new JsonCallHandler.Method()
//    {  
//      /**
//       * Get serialized size of an element
//       */
//      public JSONObject execute(IParameterProvider params) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException 
//      {
//        String serializedKey = params.getStringParameter("key", null);
//        return getElementSize(serializedKey, CacheSizeType.DISK);
//      }
//    });
//    
//    registerMethod("getElementMemorySize", new JsonCallHandler.Method()
//    {
//      /**
//       * Get in-memory size of an element
//       */
//      public JSONObject execute(IParameterProvider params) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException 
//      {
//        String serializedKey = params.getStringParameter("key", null);
//        return getElementSize(serializedKey, CacheSizeType.MEMORY);
//      }
//    });
    
  }
  
  
  private static class ResultFields extends JsonCallHandler.JsonResultFields {
    public static final String CDA_SETTINGS_ID = "cdaSettingsId";
    public static final String DATA_ACCESS_ID = "dataAccessId";
    public static final String COUNT = "count";
    public static final String ITEMS = "items";
  }
  
  
  private static class ErrorMsgs {
//    public static final String SIZEOF_NO_INSTRUMENTATION = "SizeOf needs to be declared as a java agent for this to work.";
    public static final String CACHE_ITEM_NOT_FOUND = "Cache element no longer in cache.";
    public static final String NO_CACHE_KEY_ARG = "No cache key received.";
  }
  
  private static JSONObject getcacheQueryTable(String encodedCacheKey) throws JSONException, ExporterException, UnsupportedEncodingException, IOException, ClassNotFoundException {
    
    if(encodedCacheKey == null){
      throw new IllegalArgumentException(ErrorMsgs.NO_CACHE_KEY_ARG);
    }
    
    JSONObject result = new JSONObject();
    IQueryCache cdaCache = AbstractDataAccess.getCdaCache();

    TableCacheKey lookupCacheKey = TableCacheKey.getTableCacheKeyFromString(encodedCacheKey);
    
    JSONObject table = cdaCache.getCacheEntryInfo(lookupCacheKey).getTableSnapshot();

    if(table != null){
      // put query results
      result.put(ResultFields.RESULT, table);
      result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    }
    else {
      return getErrorJson(ErrorMsgs.CACHE_ITEM_NOT_FOUND);
    }
    
    return result;
    
  }
  
  private static JSONObject getCachedQueriesOverview() throws JSONException {
    
    HashMap<String, HashMap<String, Integer>> cdaMap = new HashMap<String, HashMap<String,Integer>>();
    
    IQueryCache queryCache = AbstractDataAccess.getCdaCache();
    JSONArray results = new JSONArray();
    
    if(queryCache instanceof HazelcastQueryCache){
      HazelcastQueryCache hCache = (HazelcastQueryCache) queryCache;
      for(Entry<TableCacheKey, ExtraCacheInfo> entry : hCache.getStatsMap().entrySet()) {
//        if(info == null)
//        {
//          logger.error("Could not get info for cache key " + cacheKey);
//          continue;
//        }
        ExtraCacheInfo info = null;
        try{
          info = entry.getValue();
        }
        catch(ClassCastException e)
        {//handle issue when map will return a dataRecordEntry instead of what it's supposed to
          Object obj = hCache.getStatsMap().get(entry.getKey());
          logger.error("Expected ExtraCacheInfo in cache, found " + obj.getClass().getCanonicalName() + " instead.");
          if(obj instanceof DataRecordEntry)
          {
            DataRecordEntry drEntry = (DataRecordEntry) obj;
            logger.info("DataRecordEntry found, attempting recovery");
            Object val = drEntry.getValue();
            
            if(val instanceof ExtraCacheInfo)
            {
              info = (ExtraCacheInfo) val;
              logger.warn("ExtraCacheInfo found in record, attempting to replace cache entry..");
              hCache.getStatsMap().replace(entry.getKey(), info);//TODO: another classCastException here?
              logger.info("Entry replaced OK.");
            }
            else {
              logger.error("DataRecordEntry in cache has value of unexpected class " + obj.getClass().getCanonicalName());
              logger.warn("Removing incompatible cache entry.");
              hCache.remove(entry.getKey());
            }
          }
        }
        
        String cdaSettingsId = info.getCdaSettingsId();
        String dataAccessId = info.getDataAccessId();
        
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
    
    IQueryCache cdaCache = AbstractDataAccess.getCdaCache();
    boolean success = cdaCache.remove(key);
    
    JSONObject result = new JSONObject();
    if(success){
      result.put(JsonResultFields.STATUS, ResponseStatus.OK);
      result.put(JsonResultFields.RESULT, true);
      return result;
    }
    else {
      return getErrorJson(ErrorMsgs.CACHE_ITEM_NOT_FOUND);
    }
  }
  
//  private enum CacheSizeType{
//    DISK, MEMORY
//  }
  
  
//  private static JSONObject getElementSize(String serializedKey, CacheSizeType sizeType) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException
//  {
//    if(serializedKey == null){
//      return getErrorJson(ErrorMsgs.NO_CACHE_KEY_ARG);
//    }
//    
//    TableCacheKey key = TableCacheKey.getTableCacheKeyFromString(serializedKey);
//    
//    IQueryCache cache = AbstractDataAccess.getCdaCache();
//    
//    long size = 0;
//    
//    if(cache instanceof HazelcastQueryCache)
//    {
//      MapEntry<TableCacheKey, TableModel> element = ((HazelcastQueryCache) cache).getMap().getMapEntry(key);
//      size = element.getCost();
//    }
//    else if (cache instanceof EHCacheQueryCache)
//    {
//      
//      Cache cdaCache = AbstractDataAccess.getCache();
//      Element element = cdaCache.get(key);
//      
//      if(element == null){
//        return getErrorJson(ErrorMsgs.CACHE_ITEM_NOT_FOUND);
//      }
//      
//      switch(sizeType) {
//        case DISK:
//          size = element.getSerializedSize();
//          break;
//        case MEMORY:
//          try{
//            size = SizeOf.deepSizeOf(element);
//          }
//          catch(IllegalStateException e){
//            return getErrorJson(ErrorMsgs.SIZEOF_NO_INSTRUMENTATION);
//          }
//          break;
//      }
//    }
//   
//    if(size > 0)
//    {
//      return getOKJson(size);
//    }
//    else 
//    {
//      return getErrorJson("Error determining element size.");
//    }
//  }

  public static class CacheElementInfo{
    
    TableCacheKey key;
    
    Integer rows;
    long insertTime;
    long accessTime;
    long hits;
    Long byteSize;
    Long duration;
    
    public CacheElementInfo(MapEntry<TableCacheKey, TableModel> mapEntry, ExtraCacheInfo info){
      key = mapEntry.getKey();
      
    //  rows = tm.getRowCount();
      insertTime = Math.max(mapEntry.getLastUpdateTime(), mapEntry.getCreationTime());
      accessTime = Math.max(insertTime, mapEntry.getLastAccessTime());
      hits = mapEntry.getHits();
      
      byteSize = mapEntry.getCost();
      
      
      rows = info.getNbrRows();
      duration = info.getQueryDurationMs();
//      info.
      
    }
    
    public CacheElementInfo(TableCacheKey key, ExtraCacheInfo info)
    {
      
    }
    
    public CacheElementInfo(TableCacheKey cacheKey, net.sf.ehcache.Element element){
      key = cacheKey;
      
      if(element != null)
      {
        Object val = element.getValue();
        if(val instanceof TableModel){
          rows = ((TableModel) val).getColumnCount(); 
        }
        insertTime = element.getLatestOfCreationAndUpdateTime();
        accessTime = element.getLastAccessTime();
        hits = element.getHitCount();
        
        byteSize = null;
      }
    }
    
    public JSONObject toJson() throws JSONException, IOException
    {
      JSONObject queryInfo = new JSONObject();
      queryInfo.put("query", key.getQuery());
      
      JSONObject parameters = new JSONObject();
      ParameterDataRow pRow = key.getParameterDataRow();
      if(pRow != null) for(String paramName : pRow.getColumnNames()){
        parameters.put(paramName, pRow.get(paramName));
      }
      queryInfo.put("parameters", parameters);
      
      queryInfo.put("rows", rows != null ? rows.intValue() : null);
      
      //inserted
      queryInfo.put("inserted", insertTime);
      queryInfo.put("accessed", accessTime);
      queryInfo.put("hits", hits); 
      queryInfo.put("size", byteSize != null ? byteSize.longValue() : null);
      
      if(duration != null){
        queryInfo.put("duration", duration.longValue());
      }
      //use id to get table;
      //identifier
      String identifier = TableCacheKey.getTableCacheKeyAsString(key);
      queryInfo.put("key", identifier);
      
      return queryInfo;
    }
  }
  
  private static JSONObject listQueriesInCache(String cdaSettingsId, String dataAccessId) throws JSONException, ExporterException, IOException {
    
    JSONArray results = new JSONArray();
    
    IQueryCache queryCache = AbstractDataAccess.getCdaCache();
    
    if(queryCache instanceof HazelcastQueryCache)
    {
      HazelcastQueryCache hazelCache = (HazelcastQueryCache) queryCache;
      for(Entry<TableCacheKey, ExtraCacheInfo> entry : hazelCache.getStatsMap().entrySet())
//      for(TableCacheKey key : hazelCache.getKeys(cdaSettingsId, dataAccessId))
      {
        if( !StringUtils.equals(entry.getValue().getCdaSettingsId(), cdaSettingsId) ||
            !StringUtils.equals(entry.getValue().getDataAccessId(), dataAccessId))
        {
          continue;
        }
        TableCacheKey key = entry.getKey();
        
        MapEntry<TableCacheKey, TableModel> mapEntry =  hazelCache.getMap().getMapEntry(key);
        if(mapEntry == null){
          logger.error("No model entry found for existing key, skipping.");
          continue;
        }
        
        CacheElementInfo cacheInfo = new CacheElementInfo( mapEntry, entry.getValue() );
        results.put(cacheInfo.toJson());
      }
      
    }
    
    
    
//    if(queryCache instanceof HazelcastQueryCache){
//      HazelcastQueryCache hazelCache = (HazelcastQueryCache) queryCache;
//      
//      for(TableCacheKey cacheKey : queryCache.getKeys())
//      {
//        
//        if(!StringUtils.equals(cdaSettingsId, cacheKey.getCdaSettingsId()) ||
//           !StringUtils.equals(dataAccessId, cacheKey.getDataAccessId()))
//         {//not what we're looking for
//           continue;
//         }
//        
//        MapEntry<TableCacheKey, TableModel> mapEntry = hazelCache.getMap().getMapEntry(cacheKey);
//        if(mapEntry == null){
//          logger.error("No model entry found for existing key, skipping.");
//          continue;
//        }
//        
//        CacheElementInfo cacheInfo = new CacheElementInfo( mapEntry ); //TODO: mapEntry increments hits, no get quiet available 
//        results.put(cacheInfo.toJson());
//      }
//      
//    }
//    else {
//      Cache cdaCache = AbstractDataAccess.getCache();
//
//      for (Object key : cdaCache.getKeys()) {
//
//        if (key instanceof TableCacheKey) {
//
//          JSONObject queryInfo = new JSONObject();//TODO:
//
//          TableCacheKey cacheKey = (TableCacheKey) key;
//
//          if(!StringUtils.equals(cdaSettingsId, cacheKey.getCdaSettingsId()) ||
//             !StringUtils.equals(dataAccessId, cacheKey.getDataAccessId()))
//           {//not what we're looking for
//             continue;
//           }
//          
//          CacheElementInfo cacheInfo = new CacheElementInfo(cacheKey, cdaCache.getQuiet(key));
//          results.put(cacheInfo.toJson());
//        }
//      }
//    }
    
    JSONObject result = new JSONObject();
    result.put(ResultFields.CDA_SETTINGS_ID, cdaSettingsId);
    result.put(ResultFields.DATA_ACCESS_ID, dataAccessId);
    result.put(ResultFields.ITEMS, results);
    
    JSONObject response = new JSONObject();
    response.put(ResultFields.STATUS, ResponseStatus.OK);
    response.put(ResultFields.RESULT, result);
    
    return response;
  }

  
}
