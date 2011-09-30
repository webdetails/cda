package pt.webdetails.cda.cache;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Transaction;
import com.hazelcast.impl.base.DataRecordEntry;


public class HazelcastQueryCache implements IQueryCache {

  private static final Log logger = LogFactory.getLog(HazelcastQueryCache.class);
  
  public static final String PROPERTY_SUPER_CLIENT = "hazelcast.super.client";
  public static final String MAP_NAME = "cdaCache";
  public static final String AUX_MAP_NAME = "cdaCacheStats";
  
  private static IMap<TableCacheKey, TableModel> cache;
  //used for holding extra info 
  private static IMap<TableCacheKey, ExtraCacheInfo> cacheStats;
  
  public HazelcastQueryCache(){
    this(null, false);
  }
  
  public HazelcastQueryCache(String cfgFile, boolean superClient){
    init(cfgFile, false);
  }
  
    
  public static void init(String configFile, boolean superClient)
  {  
    Config config = null;
    
    if(configFile != null){
      try {
        XmlConfigBuilder configBuilder = new XmlConfigBuilder(configFile);
        config = configBuilder.build();
      } catch (FileNotFoundException e) {
        logger.error("Config file not found, using defaults", e);
      }
    }
    
    if(config == null){
      config = new Config();
    }

    //super client: doesn't hold data but has first class access
    //needs a running instance to work
    try{
      String isSuper = System.getProperty(PROPERTY_SUPER_CLIENT);
      if(Boolean.parseBoolean(isSuper) && !superClient){
        System.setProperty(PROPERTY_SUPER_CLIENT , "false");
      }
      else if(superClient){
        System.setProperty(PROPERTY_SUPER_CLIENT, "true");
      }
    } catch (SecurityException e){
      logger.error("Error accessing " + PROPERTY_SUPER_CLIENT, e);
    }

    try{
      Hazelcast.init(config);
    }
    catch(IllegalStateException e){
      logger.warn("Hazelcast already started, could not load configuration. Shutdown all instances and restart if configuration needs changes.");
    }
    
    cache = Hazelcast.getMap(MAP_NAME);
    logger.debug("Hazelcast cache started, using map " + MAP_NAME);
    
    cacheStats = Hazelcast.getMap(AUX_MAP_NAME);

    SyncRemoveStatsEntryListener syncRemoveStats = new SyncRemoveStatsEntryListener();
    cache.removeEntryListener(syncRemoveStats);
    cache.addEntryListener(syncRemoveStats, false);
    
    cache.addEntryListener(new LoggingEntryListener(), true);
    logger.debug("Added entry listener");
  }
  
  public static void shutdownIfRunning()
  {
    if(Hazelcast.getLifecycleService().isRunning()){
      logger.debug("Shutting down Hazelcast...");
      Hazelcast.getLifecycleService().shutdown();
    }
  }
  
  @Override
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec) {
    cache.put(key, table);
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
        if(info.getTimeToLive() > 0 && info.getTimeToLive() + info.getEntryTime() > System.currentTimeMillis())
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
  
//  public Iterable<TableCacheKey> getKeys(String cdaSettingsId, String dataAccessId)
//  {
//    // this approach hangs indefinetely when entries are not owned by CDA's instance
//    return cacheStats.keySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + " AND dataAccessId = " + dataAccessId));   
//  }

  @Override
  public void putTableModel(TableCacheKey key, TableModel table, ExtraCacheInfo cacheInfo) 
  {
    Transaction trans = Hazelcast.getTransaction();
    trans.begin();
    try 
    {   
      cache.put(key, table);
      cacheStats.put(key, cacheInfo);
      trans.commit();
    }
    catch (Exception e)  
    {
      trans.rollback();
      logger.error("Error putting query results in cache.", e);
    }
  }
  
  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key){
    return cacheStats.get(key);
  }

  
  private static final class SyncRemoveStatsEntryListener implements EntryListener<TableCacheKey, TableModel>  {
    
    @Override
    public void entryAdded(EntryEvent<TableCacheKey, TableModel> event) {}//ignore
    @Override
    public void entryUpdated(EntryEvent<TableCacheKey, TableModel> event) {}//ignore
    
    @Override
    public void entryRemoved(EntryEvent<TableCacheKey, TableModel> event) 
    {
      TableCacheKey key = event.getKey();
      logger.debug("entry removed, removing stats for query " + key);
      cacheStats.remove(key);
    }

    @Override
    public void entryEvicted(EntryEvent<TableCacheKey, TableModel> event) {
      TableCacheKey key = event.getKey();
      logger.debug("entry evicted, removing stats for query " + key);
      cacheStats.remove(key);
    }
    
    @Override
    public boolean equals(Object other){
      return other instanceof SyncRemoveStatsEntryListener;
    }

  }
  
  static class LoggingEntryListener implements EntryListener<TableCacheKey, TableModel> {

    private static final Log logger = LogFactory.getLog(HazelcastQueryCache.class);
    
    @Override
    public void entryAdded(EntryEvent<TableCacheKey, TableModel> event) {
      logger.debug("ENTRY ADDED " + event);
    }

    @Override
    public void entryRemoved(EntryEvent<TableCacheKey, TableModel> event) {
      logger.debug("ENTRY REMOVED " + event);
    }

    @Override
    public void entryUpdated(EntryEvent<TableCacheKey, TableModel> event) {
      logger.debug("ENTRY UPDATED " + event);
    }

    @Override
    public void entryEvicted(EntryEvent<TableCacheKey, TableModel> event) {
      logger.debug("ENTRY EVICTED " + event);
    }
    
  }
  
//  public Iterable<ExtraCacheInfo> getCacheEntryInfo(String cdaSettingsId, String dataAccessId)
//  {
//    return cacheStats.values(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + " AND dataAccessId = " + dataAccessId));
//  }
//  
//  public Iterable<Entry<TableCacheKey, ExtraCacheInfo>> getCacheStatsEntries(String cdaSettingsId, String dataAccessId)
//  {
//    return cacheStats.entrySet(new SqlPredicate("cdaSettingsId = " + cdaSettingsId + " AND dataAccessId = " + dataAccessId));
//  }
  

}
