package pt.webdetails.cda.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.utils.framework.JsonCallHandler;
import pt.webdetails.cda.utils.framework.JsonCallHandler.JsonResultFields;
import pt.webdetails.cda.utils.framework.JsonCallHandler.ResponseStatus;

/**
 * Old CacheMonitorHandler except for the handling part
 */
public class CacheMonitor extends BaseService {

  //TODO: switch to jackson?

  /**
   * formerly known as "cached"
   * List queries in cache
   * @return
   */
  public String listCachedQueries(String cdaSettingsId, String dataAccessId) {
    
    return null;
  }

  private static class ResultFields extends JsonCallHandler.JsonResultFields {
    public static final String CDA_SETTINGS_ID = "cdaSettingsId";
    public static final String DATA_ACCESS_ID = "dataAccessId";
    public static final String COUNT = "count";
    public static final String ITEMS = "items";
  }

  private static class ErrorMsgs {
    public static final String CACHE_ITEM_NOT_FOUND = "Cache element no longer in cache.";
  }
  
  /**
   * "cached" method
   * @param cdaSettingsId
   * @param dataAccessId
   * @return JSON
   */
  public JSONObject listQueriesInCache(String cdaSettingsId, String dataAccessId) throws JSONException, IOException {
    
    JSONArray results = new JSONArray();
    
    IQueryCache cdaCache = AbstractDataAccess.getCdaCache();
    
    for(TableCacheKey key : cdaCache.getKeys()) {
      
      ExtraCacheInfo info = cdaCache.getCacheEntryInfo(key);
      if(info == null) continue;
      
      if(!StringUtils.equals(cdaSettingsId, info.getCdaSettingsId()) ||
         (dataAccessId != null && !StringUtils.equals(dataAccessId, info.getDataAccessId())))
      {//not what we're looking for
        continue;
      }
      
      CacheElementInfo cacheInfo = cdaCache.getElementInfo(key);
      if(cacheInfo != null){
        results.put(cacheInfo.toJson());
      }
    }
    
    JSONObject result = new JSONObject();
    result.put(ResultFields.CDA_SETTINGS_ID, cdaSettingsId);
    result.put(ResultFields.DATA_ACCESS_ID, dataAccessId);
    result.put(ResultFields.ITEMS, results);
//    //total stats
//    result.put("cacheLength", cdaCache.getSize());
//    result.put("memoryStoreLength", cdaCache.getMemoryStoreSize());
//    result.put("diskStoreLength", cdaCache.getDiskStoreSize());
    
    JSONObject response = new JSONObject();
    response.put(ResultFields.STATUS, ResponseStatus.OK);
    response.put(ResultFields.RESULT, result);
    
    return response;
  }

  /**
   * 
   * @param encodedCacheKey base64-encoded cache key
   * @return
   */
  public JSONObject getCacheQueryTable(String encodedCacheKey) throws JSONException {

    try {
      if(encodedCacheKey == null){
        throw new IllegalArgumentException("No cache key received.");
      }
      
      JSONObject result = new JSONObject();
      IQueryCache cdaCache = AbstractDataAccess.getCdaCache();
  
      TableCacheKey lookupCacheKey = TableCacheKey.getTableCacheKeyFromString(encodedCacheKey);
      ExtraCacheInfo info = cdaCache.getCacheEntryInfo(lookupCacheKey);
  
      if(info != null){
        // put query results
        result.put(ResultFields.RESULT, info.getTableSnapshot());
        result.put(JsonResultFields.STATUS, ResponseStatus.OK);
      }
      else {
        return JsonCallHandler.getErrorJson(ErrorMsgs.CACHE_ITEM_NOT_FOUND);
      }
      
      return result;
    }
    catch (Exception e) {
      return JsonCallHandler.getErrorJson( e.getLocalizedMessage() );
    }
    
  }

  /**
   * 
   * @param cdaSettingsIdFilter
   * @return
   * @throws JSONException
   */
  public JSONObject getCachedQueriesOverview(String cdaSettingsIdFilter) throws JSONException {
    
    HashMap<String, HashMap<String, Integer>> cdaMap = new HashMap<String, HashMap<String,Integer>>();
    
    IQueryCache cdaCache = AbstractDataAccess.getCdaCache();
    JSONArray results = new JSONArray();
    
    for(TableCacheKey key : cdaCache.getKeys()) {
      
      ExtraCacheInfo info = cdaCache.getCacheEntryInfo(key);
      
      if(info == null) continue;
      
      String cdaSettingsId = info.getCdaSettingsId();
      String dataAccessId = info.getDataAccessId();
      
      if(cdaSettingsIdFilter != null && !cdaSettingsIdFilter.equals(cdaSettingsId)){
        continue;
      }
      
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
      getOverviewForCdaSettingsId(cdaMap, results, cdaSettingsId);
    }
    
    JSONObject result = new JSONObject();
    result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    result.put(ResultFields.RESULT, results);
    return result;
  }


  /**
   * @param cdaMap
   * @param results
   * @param cdaSettingsId
   * @throws JSONException
   */
  private static void getOverviewForCdaSettingsId(HashMap<String, HashMap<String, Integer>> cdaMap, JSONArray results, String cdaSettingsId) throws JSONException {
    for(String dataAccessId : cdaMap.get(cdaSettingsId).keySet() ){
      Integer count = cdaMap.get(cdaSettingsId).get(dataAccessId);
      JSONObject queryInfo = new JSONObject();
      queryInfo.put(ResultFields.CDA_SETTINGS_ID, cdaSettingsId);
      queryInfo.put(ResultFields.DATA_ACCESS_ID, dataAccessId); 
      queryInfo.put(ResultFields.COUNT, count.intValue());
      
      results.put(queryInfo);
    }
  }

  /**
   * 
   * @param serializedCacheKey
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws JSONException
   */
  public JSONObject removeQueryFromCache(String serializedCacheKey) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException 
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

  public JSONObject removeAll(String cdaSettingsId, String dataAccessId) throws JSONException
  {
    IQueryCache cdaCache = AbstractDataAccess.getCdaCache();
    int result = cdaCache.removeAll(cdaSettingsId, dataAccessId);
    
    return getOkJson(result);
  }

  /**
   * 
   * @return
   * @throws JSONException
   */
  public JSONObject shutdown() throws JSONException {
    AbstractDataAccess.shutdownCache();
    return getOkJson("Cache shutdown.");
  }

  protected String getJsonString( JSONObject json ) throws JSONException {
    return json.toString(JsonCallHandler.INDENT_FACTOR);
  }
  
  protected JSONObject getOkJson(Object obj) throws JSONException {
    return JsonCallHandler.getOKJson(obj);
  }
  
  protected JSONObject getErrorJson(String msg) throws JSONException {
    return JsonCallHandler.getErrorJson(msg);
  }

}
