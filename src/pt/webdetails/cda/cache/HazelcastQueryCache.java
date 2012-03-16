/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cda.CdaContentGenerator;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.impl.base.DataRecordEntry;
import com.hazelcast.query.SqlPredicate;

/**
 * 
 * Hazelcast implementation of CDA query cache
 * 
 */
public class HazelcastQueryCache extends ClassLoaderAwareCaller implements IQueryCache {

  private static final Log logger = LogFactory.getLog(HazelcastQueryCache.class);
  
  public static final String PROPERTY_SUPER_CLIENT = "hazelcast.super.client";
  public static final String MAP_NAME = "cdaCache";
  public static final String AUX_MAP_NAME = "cdaCacheStats";
  
  private static final String PLUGIN_PATH = "system/" + CdaContentGenerator.PLUGIN_NAME + "/";
  private static final String CACHE_CFG_FILE_HAZELCAST = "hazelcast.xml";
  
  //main cache (will hold actual values)
  private static IMap<TableCacheKey, TableModel> cache;
  //used for holding extra info 
  private static IMap<TableCacheKey, ExtraCacheInfo> cacheStats;
  
  
  public HazelcastQueryCache(){
    this(PentahoSystem.getApplicationContext().getSolutionPath(PLUGIN_PATH + CACHE_CFG_FILE_HAZELCAST),true);
  }
  
  private HazelcastQueryCache(String cfgFile, boolean superClient){
    super(Thread.currentThread().getContextClassLoader());
    init(cfgFile, true);
  }
  
    
  private static void init(String configFile, boolean superClient)
  {  
    
    logger.info("CDA CDC Hazelcast INIT");
    
    cache = Hazelcast.getMap(MAP_NAME);
    logger.debug("Hazelcast cache started, using map " + MAP_NAME);
    
    cacheStats = Hazelcast.getMap(AUX_MAP_NAME);

    ClassLoader cdaPluginClassLoader = Thread.currentThread().getContextClassLoader();
    
    SyncRemoveStatsEntryListener syncRemoveStats = new SyncRemoveStatsEntryListener( cdaPluginClassLoader );
    cache.removeEntryListener(syncRemoveStats);
    //WARNING: setting includeValue to false will result in a premature key serialization
    // that will make hazelcast fail if its classloader cannot resolve the key class
    //This is fixed in hazelcast 2.0
    cache.addEntryListener(syncRemoveStats, true);//false
    //logging/debug, includes value
    cache.addEntryListener(new LoggingEntryListener(cdaPluginClassLoader), true);
    
    logger.debug("Added entry listener");
    
  }
  
  public void shutdownIfRunning()
  {
    if(Hazelcast.getLifecycleService().isRunning()){
      logger.debug("Shutting down Hazelcast...");
      Hazelcast.getLifecycleService().shutdown();
    }
  }
  
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo info) {
    cache.put(key, table);
    info.setEntryTime(System.currentTimeMillis());//TODO: distributed alternative?
    info.setTimeToLive(ttlSec*1000);
    cacheStats.put(key, info);
  }

  @Override
  public TableModel getTableModel(TableCacheKey key) {
    try
    {
      TableModel tm = cache.get(key);
      if(tm == null) return null;
      
      ExtraCacheInfo info = cacheStats.get(key);
      if(info != null)
      {
        //per instance ttl not supported by hazelcast, need to check manually
        if(info.getTimeToLive() > 0 && (info.getTimeToLive() + info.getEntryTime()) < System.currentTimeMillis())
        {
          cache.remove(key);
          logger.info("Cache element expired, removed from cache.");
          return null;
        }
        else return tm;
      }     
      else 
      {//no stats found; may be out of time to live, best to remove
        logger.error("Cache info not found! Removing element.");
        cache.remove(key);
        logger.info("element removed");
        return null;
      }
    } 
    catch(ClassCastException e)
    {//handle issue when map will return a dataRecordEntry instead of element type
      Object obj = cache.get(key);
      logger.error("Expected TableModel in cache, found " + obj.getClass().getCanonicalName() + " instead.");
      if(obj instanceof DataRecordEntry)
      {
        DataRecordEntry drEntry = (DataRecordEntry) obj;
        logger.info("Cache holding DataRecordEntry, attempting recovery");
        Object val = drEntry.getValue();
        
        if(val instanceof TableModel)
        {
          TableModel tm = (TableModel) val;
          logger.warn("TableModel found in record, attempting to replace cache entry..");
          cache.replace(key, tm);
          logger.info("Cache entry replaced.");
          return tm;
        }
        else {
          logger.error("DataRecordEntry in cache has value of unexpected type " + obj.getClass().getCanonicalName());
          logger.warn("Removing incompatible cache entry.");
          cache.remove(key);
        }
      }
      return null;
    }
    catch (Exception e){
      if(e.getCause() instanceof IOException) 
      {//most likely a StreamCorruptedException
        logger.error("IO error while attempting to get key " + key + "(" + e.getCause().getMessage() + "), removing from cache!");
        cache.remove(key);
        logger.info("entry removed");
        return null;
      }
      else logger.error("Unexpected exception ", e);
      return null;
    }
  }
  


  @Override
  public void clearCache() {
    cache.clear();
    cacheStats.clear();
  }

  @Override
  public boolean remove(TableCacheKey key) {
    return cache.remove(key) != null;
  }
  
  public IMap<TableCacheKey, TableModel> getMap(){
    return cache;
  }
  
  public IMap<TableCacheKey, ExtraCacheInfo> getStatsMap(){
    return cacheStats;
  }

  @Override
  public Iterable<TableCacheKey> getKeys() {
    return cache.keySet();
  }
  
  public Iterable<TableCacheKey> getKeys(String cdaSettingsId, String dataAccessId)
  {
    return cacheStats.keySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + " AND dataAccessId = " + dataAccessId));   
  }
  
  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key){
    return cacheStats.get(key);
  }
  
  /**
   * 
   *  Synchronizes both maps' removals and evictions
   *  
   *
   */
  private static final class SyncRemoveStatsEntryListener extends ClassLoaderAwareCaller implements EntryListener<TableCacheKey, TableModel>  {
    
    public SyncRemoveStatsEntryListener(ClassLoader classLoader){
      super(classLoader); 
    }
    
    @Override
    public void entryAdded(EntryEvent<TableCacheKey, TableModel> event) {}//ignore
    @Override
    public void entryUpdated(EntryEvent<TableCacheKey, TableModel> event) {}//ignore
    
    @Override
    public void entryRemoved(final EntryEvent<TableCacheKey, TableModel> event) 
    {

      runInClassLoader(new Runnable(){
      
        public void run(){
          TableCacheKey key = event.getKey();
          logger.debug("entry removed, removing stats for query " + key);
          cacheStats.remove(key);
        }
        
      });

    }

    @Override
    public void entryEvicted(final EntryEvent<TableCacheKey, TableModel> event) {

      runInClassLoader(new Runnable(){
        
        public void run(){
        TableCacheKey key = event.getKey();
        logger.debug("entry evicted, removing stats for query " + key);
        cacheStats.remove(key);
        }
        
      });
    }
    
    //used for listener removal
    @Override
    public boolean equals(Object other){
      return other instanceof SyncRemoveStatsEntryListener;
    }

  }
  
  static class LoggingEntryListener extends ClassLoaderAwareCaller implements EntryListener<TableCacheKey, TableModel> {
    
    private static final Log logger = LogFactory.getLog(HazelcastQueryCache.class);
    
    public LoggingEntryListener(ClassLoader classLoader){
        super(classLoader);
    }
    
    @Override
    public void entryAdded(final EntryEvent<TableCacheKey, TableModel> event) {
      runInClassLoader(new Runnable() {

        public void run() {
          logger.debug("CDA ENTRY ADDED " + event);
        }
      });

    }

    @Override
    public void entryRemoved(final EntryEvent<TableCacheKey, TableModel> event) {
      runInClassLoader(new Runnable() {
        public void run() {
          logger.debug("CDA ENTRY REMOVED " + event);
        }
      });
    }

    @Override
    public void entryUpdated(final EntryEvent<TableCacheKey, TableModel> event) {
      runInClassLoader(new Runnable() {
        public void run() {
          logger.debug("CDA ENTRY UPDATED " + event);
        }
      });
    }

    @Override
    public void entryEvicted(final EntryEvent<TableCacheKey, TableModel> event) {
      runInClassLoader(new Runnable() {
        public void run() {
          logger.debug("CDA ENTRY EVICTED " + event);
        }
      });
    }
    
  }

  @Override
  public int removeAll(final String cdaSettingsId, final String dataAccessId) {
    if(cdaSettingsId == null){
      int size = cache.size();
      cache.clear();
      return size;
    }
    
    try {
      return callInClassLoader(new Callable<Integer>(){
      
        public Integer call(){
          int size=0;
          Iterable<Entry<TableCacheKey, ExtraCacheInfo>> entries = getCacheStatsEntries(cdaSettingsId, dataAccessId);
          if(entries != null) for(Entry<TableCacheKey, ExtraCacheInfo> entry: entries){
            cache.remove(entry.getKey());
            size++;
          }
          return size;
        }
      });
    } catch (Exception e) {
      logger.error("Error calling removeAll", e);
      return -1;
    }
  }

  @Override
  public CacheElementInfo getElementInfo(TableCacheKey key) {
    ExtraCacheInfo info = cacheStats.get(key);
    MapEntry<TableCacheKey,TableModel> entry = cache.getMapEntry(key);
    
    CacheElementInfo ceInfo = new CacheElementInfo();
    ceInfo.setAccessTime(entry.getLastAccessTime());
    ceInfo.setByteSize(entry.getCost());
    ceInfo.setInsertTime(entry.getCreationTime());
    ceInfo.setKey(key);
    ceInfo.setHits(entry.getHits());
    
    ceInfo.setRows(info.getNbrRows());
    ceInfo.setDuration(info.getQueryDurationMs());

    return ceInfo;
  }
  
  /**
   * (Make sure right class loader is set when accessing the iterator) 
   * @param cdaSettingsId
   * @param dataAccessId
   * @return
   */
  public Iterable<ExtraCacheInfo> getCacheEntryInfo(String cdaSettingsId, String dataAccessId)
  {
    return cacheStats.values(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + ((dataAccessId != null)? " AND dataAccessId = " + dataAccessId : "")));
  }

  /**
   * (Make sure right class loader is set when accessing the iterator)
   * @param cdaSettingsId
   * @param dataAccessId
   * @return
   */
  public Iterable<Entry<TableCacheKey, ExtraCacheInfo>> getCacheStatsEntries(final String cdaSettingsId,final String dataAccessId)
  {
    return cacheStats.entrySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + ((dataAccessId != null)? " AND dataAccessId = " + dataAccessId : "")));
  }
  

}
