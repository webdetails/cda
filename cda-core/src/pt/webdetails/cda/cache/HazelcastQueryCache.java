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

package pt.webdetails.cda.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaPropertiesHelper;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MapEntry;
import com.hazelcast.impl.base.DataRecordEntry;
import com.hazelcast.query.SqlPredicate;

import java.util.concurrent.TimeUnit;

/**
 * 
 * Hazelcast implementation of CDA query cache
 * 
 */
public class HazelcastQueryCache extends ClassLoaderAwareCaller implements IQueryCache {

  private static final Log logger = LogFactory.getLog(HazelcastQueryCache.class);
  
  public static final String MAP_NAME = "cdaCache";
  public static final String AUX_MAP_NAME = "cdaCacheStats";

  private static final String GROUP_NAME = "cdc";
  private static HazelcastInstance hzInstance;
  private static LifecycleService lifeCycleService;
  
  private static long getTimeout = CdaPropertiesHelper.getIntProperty("pt.webdetails.cda.cache.getTimeout", 5);
  private static TimeUnit timeoutUnit = TimeUnit.SECONDS;
  //max consecutive timeouts
  private static int maxTimeouts = CdaPropertiesHelper.getIntProperty("pt.webdetails.cda.cache.maxTimeouts", 4);
  private static long cacheDisablePeriod = CdaPropertiesHelper.getIntProperty("pt.webdetails.cda.cache.disablePeriod", 5);
  private static boolean debugCache = CdaPropertiesHelper.getBoolProperty("pt.webdetails.cda.cache.debug", true);

  
  private static int timeoutsReached = 0;
  private static boolean active=true;
  
  /** 
   * @return main cache (will hold actual values)
   */
  private static IMap<TableCacheKey, TableModel> getCache(){
    return getHazelcast().getMap(MAP_NAME);
  }
  
  /**
   * @return used for holding extra info 
   */
  private static IMap<TableCacheKey, ExtraCacheInfo> getCacheStats(){
    return getHazelcast().getMap(AUX_MAP_NAME);
  }
  
  private static synchronized HazelcastInstance getHazelcast(){
    if(hzInstance == null || !lifeCycleService.isRunning()){
      logger.debug("finding hazelcast instance..");
      for(HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()){
        logger.debug("found hazelcast instance [" + instance.getName() + "]");
        if(instance.getConfig().getGroupConfig().getName().equals(GROUP_NAME)){
          logger.info(GROUP_NAME + " hazelcast instance found: [" + instance.getName() + "]");
          hzInstance = instance;
          lifeCycleService = hzInstance.getLifecycleService();
          break;
        }
      }
      
      if(hzInstance == null){
        logger.fatal("No valid hazelcast instance found.");
      }
      else {
        init();
      }
    }
    return hzInstance;
  }
  

  
  public HazelcastQueryCache(){
    super(Thread.currentThread().getContextClassLoader());
  }

  private static int incrTimeouts(){
    return ++timeoutsReached;
  }

  //no problem if unsynched
  private static int resetTimeouts(){
    return timeoutsReached=0;
  }
  
    
  private static void init()
  {  
    logger.info( "CDA CDC Hazelcast INIT" );

    // sync cache removals with cacheStats
    ClassLoader cdaPluginClassLoader = Thread.currentThread().getContextClassLoader();
    SyncRemoveStatsEntryListener syncRemoveStats = new SyncRemoveStatsEntryListener( cdaPluginClassLoader );

    IMap<TableCacheKey, TableModel> cache = hzInstance.getMap( MAP_NAME );

    cache.removeEntryListener( syncRemoveStats );
    cache.addEntryListener( syncRemoveStats, false );
    
    if ( debugCache ) {
      logger.debug( "Added logging entry listener" );
      cache.addEntryListener( new LoggingEntryListener( cdaPluginClassLoader ), false );
    }
  }
  
  public void shutdownIfRunning()
  {
    //let cdc handle this
  }
  
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo info) {
    getCache().putAsync(key, table);
    // TODO:async version of :?
    //    getCache().put(key, table, ttlSec, TimeUnit.SECONDS);
    info.setEntryTime(System.currentTimeMillis());
    info.setTimeToLive(ttlSec*1000);
    getCacheStats().putAsync(key, info);
  }
  
  private <K,V> V getWithTimeout(K key, IMap<K,V> map){
    if(!active) return null;
    Future<V> future = map.getAsync(key);
    try{
      V result = future.get(getTimeout, timeoutUnit);
      resetTimeouts();
      return result;
    }
    catch(TimeoutException e){
      int nbrTimeouts = incrTimeouts();
      checkNbrTimeouts(nbrTimeouts);
      logger.error("Timeout " + getTimeout + " " +  timeoutUnit + " expired fetching from " + 
          map.getName() + " (timeout#" + nbrTimeouts + ")" );
    } catch (InterruptedException e) {
      logger.error(e);
    } catch (ExecutionException e) {
      logger.error(e);
    }
    return null;
  }
  
  private void checkNbrTimeouts(int nbrTimeouts){
    if(nbrTimeouts > 0 && nbrTimeouts % maxTimeouts == 0){
      //too many consecutive timeouts may mean hazelcast is gettint more requests
      //than it can handle, give it some space
      logger.error("Too many timeouts, disabling for " + cacheDisablePeriod + " seconds.");
      resetTimeouts();
      active = false;
      new Thread(new Runnable(){

        @Override
        public void run() {
          try {
            Thread.sleep(cacheDisablePeriod * 1000);
          } catch (InterruptedException e) {
            logger.error(e);
          } catch (Exception e){
            logger.error(e);
          }
          active = true;
        }
        
      }).start();
    }
  }
  
  @Override
  public TableModel getTableModel(TableCacheKey key) {
    try
    {
      ExtraCacheInfo info = getWithTimeout(key, getCacheStats());
      if(info != null)
      {
        //per instance ttl not supported by hazelcast, need to check manually
        if(info.getTimeToLive() > 0 && (info.getTimeToLive() + info.getEntryTime()) < System.currentTimeMillis())
        {
          logger.info("Cache element expired, removing from cache.");
          getCache().removeAsync(key);
          return null;
        }
        else {
          TableModel tm = getWithTimeout(key, getCache());
          if(tm == null) {
            logger.error("Cache stats out of sync! Removing element.");
            getCacheStats().removeAsync(key);
            return null;
          }
          logger.info("Table found in cache. Returning.");
          return tm;
        }
      }
      return null;
    } 
    catch(ClassCastException e)
    {
      //handle issue when map would return a dataRecordEntry instead of element type
      //TODO: hasn't been caught in a while, maybe we can drop this
      Object obj = getCache().get(key);
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
          getCache().replace(key, tm);
          logger.info("Cache entry replaced.");
          return tm;
        }
        else {
          logger.error("DataRecordEntry in cache has value of unexpected type " + obj.getClass().getCanonicalName());
          logger.warn("Removing incompatible cache entry.");
          getCache().remove(key);
        }
      }
      return null;
    }
    catch (Exception e){
      if(e.getCause() instanceof IOException) 
      {//most likely a StreamCorruptedException
        logger.error("IO error while attempting to get key " + key + "(" + e.getCause().getMessage() + "), removing from cache!", e);
        getCache().removeAsync(key);
        return null;
      }
      else logger.error("Unexpected exception ", e);
      return null;
    }
  }
  


  @Override
  public void clearCache() {
    getCache().clear();
    getCacheStats().clear();
  }

  @Override
  public boolean remove(TableCacheKey key) {
    return getCache().remove(key) != null;
  }


  @Override
  public Iterable<TableCacheKey> getKeys() {
    return getCache().keySet();
  }
  
  public Iterable<TableCacheKey> getKeys(String cdaSettingsId, String dataAccessId)
  {
    return getCacheStats().keySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + " AND dataAccessId = " + dataAccessId));   
  }
  
  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key){
    return  getCacheStats().get(key);
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
          getCacheStats().remove(key);
        }
        
      });

    }

    @Override
    public void entryEvicted(final EntryEvent<TableCacheKey, TableModel> event) {

      runInClassLoader(new Runnable(){
        
        public void run(){
          TableCacheKey key = event.getKey();
          logger.debug("entry evicted, removing stats for query " + key);
          getCacheStats().remove(key);
        }
        
      });
    }

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
      int size = getCache().size();
      getCache().clear();
      return size;
    }

    try {
      return callInClassLoader(new Callable<Integer>(){
      
        public Integer call(){
          int size=0;
          Iterable<Map.Entry<TableCacheKey, ExtraCacheInfo>> entries = getCacheStatsEntries(cdaSettingsId, dataAccessId);
          if(entries != null) for(Map.Entry<TableCacheKey, ExtraCacheInfo> entry: entries){
            getCache().remove(entry.getKey());
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
    ExtraCacheInfo info = getCacheStats().get(key);
    MapEntry<TableCacheKey,TableModel> entry = getCache().getMapEntry(key);
    final long NO_DATE = 0L;
    CacheElementInfo ceInfo = new CacheElementInfo();
    long creationTime = entry.getCreationTime();
    ceInfo.setAccessTime(entry.getLastAccessTime());
    if (ceInfo.getAccessTime() == NO_DATE) {
      ceInfo.setAccessTime(creationTime);
    }
    ceInfo.setByteSize(entry.getCost());
    ceInfo.setInsertTime(entry.getLastUpdateTime());
    if (ceInfo.getInsertTime() == NO_DATE) {
      ceInfo.setInsertTime(creationTime);
    }
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
  public Iterable<Map.Entry<TableCacheKey, ExtraCacheInfo>> getCacheStatsEntries(final String cdaSettingsId, final String dataAccessId)
  {
    //sql predicate would need to instantiate extraCacheInfo in host classloader
    //return getCacheStats().entrySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + ((dataAccessId != null)? " AND dataAccessId = " + dataAccessId : "")));
    ArrayList<Entry<TableCacheKey, ExtraCacheInfo>> result = new ArrayList<Entry<TableCacheKey, ExtraCacheInfo>>();
    for(Entry<TableCacheKey, ExtraCacheInfo> entry : getCacheStats().entrySet()) {
      if (entry.getValue().getCdaSettingsId().equals(cdaSettingsId)
          && (dataAccessId == null || dataAccessId.equals(entry.getValue().getDataAccessId()))) 
      {
        result.add(entry);
      }
    }
    return result;
  }

}
