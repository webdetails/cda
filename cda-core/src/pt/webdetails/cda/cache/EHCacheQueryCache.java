/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaCoreContentGenerator;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;


import mondrian.olap.InvalidArgumentException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

public class EHCacheQueryCache implements IQueryCache {

  private static final Log logger = LogFactory.getLog(EHCacheQueryCache.class);
  private static final String CACHE_NAME = "pentaho-cda-dataaccess";
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_CFG_FILE_DIST = "ehcache-dist.xml";
  private static final String PLUGIN_PATH = "system/" + CdaCoreContentGenerator.PLUGIN_NAME + "/";
  private static final String USE_TERRACOTTA_PROPERTY = "pt.webdetails.cda.UseTerracotta";
  private static CacheManager cacheManager;
  
  private static class CacheElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private TableModel table;
    private ExtraCacheInfo info;
    
    public CacheElement(TableModel table, ExtraCacheInfo info){
      this.table = table;
      this.info = info;
    }

    public TableModel getTable() {
      return table;
    }

    public ExtraCacheInfo getInfo() {
      return info;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeObject(table);
      out.writeObject(info);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      table = (TableModel) in.readObject();
      info = (ExtraCacheInfo) in.readObject();
    }
    
  }
  
  protected static synchronized net.sf.ehcache.Cache getCacheFromManager() throws CacheException
  {
    if (cacheManager == null)
    {// 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
      boolean useTerracotta = Boolean.parseBoolean(CdaBoot.getInstance().getGlobalConfig().getConfigProperty(USE_TERRACOTTA_PROPERTY));
      String cacheConfigFile = useTerracotta ? CACHE_CFG_FILE_DIST : CACHE_CFG_FILE;

      if (CdaEngine.isStandalone())
      {//look for the one under src/jar
        URL cfgFile = CdaBoot.class.getResource(cacheConfigFile);
        cacheManager = new net.sf.ehcache.CacheManager(cfgFile);
        logger.debug("Cache started using " + cfgFile);
      }
      else
      {//look at cda folder in pentaho
        try
        {//preferred way: proper config in plugin folder
         String cfgFile = PentahoSystem.getApplicationContext().getSolutionPath(PLUGIN_PATH + cacheConfigFile);
         cacheManager = new net.sf.ehcache.CacheManager(cfgFile);
         logger.debug("Cache started using " + cfgFile);
        }
        catch(CacheException exc)
        {//fallback to standalone 
          logger.warn("Cache configuration not found in plugin folder, using fallback.");
          URL cfgFile = CdaBoot.class.getResource(cacheConfigFile);
          cacheManager = new net.sf.ehcache.CacheManager(cfgFile);
        }
      }
      // enable clean shutdown so ehcache's diskPersistent attribute can work
      if (!useTerracotta)
      {
        enableCacheProperShutdown(true);
      }

    }

    if (cacheManager.cacheExists(CACHE_NAME) == false)
    {
      cacheManager.addCache(CACHE_NAME);
    }

    return cacheManager.getCache(CACHE_NAME);
  }
  
  
  
  private static void enableCacheProperShutdown(final boolean force)
  {
    if (!force)
    {
      try
      {
        System.getProperty(net.sf.ehcache.CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY);
        return;//unless force, ignore if already set
      }
      catch (NullPointerException npe)
      {
      } // key null, continue
      catch (InvalidArgumentException iae)
      {
      }// key not there, continue
      catch (SecurityException se)
      {
        return;//no permissions to set
      }
    }
    System.setProperty(net.sf.ehcache.CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
  }
  
  Cache cache = null;
  
  public EHCacheQueryCache(Cache cache){
    this.cache = cache;
  }
  
  public EHCacheQueryCache(){
    this.cache = getCacheFromManager();
  }
  
  @Deprecated
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec){
    putTableModel(key,table,ttlSec,new ExtraCacheInfo("","",-1,table));
  }
  
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo info) 
  {
    final CacheElement cacheElement = new CacheElement(table, info);
    final Element storeElement = new Element(key, cacheElement);
    storeElement.setTimeToLive(ttlSec);
    cache.put(storeElement);
    cache.flush();
    
    // Print cache status size
    logger.debug("Cache status: " + cache.getMemoryStoreSize() + " in memory, " + 
            cache.getDiskStoreSize() + " in disk");
  }

  @Override
  public TableModel getTableModel(TableCacheKey key) {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try{
      //make sure we have the right class loader in thread to instantiate cda classes in case DiskStore is used
      //TODO: ehcache 2.5 has ClassLoaderAwareCache
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      final Element element = cache.get(key);
      if (element != null) // Are we explicitly saying to bypass the cache?
      {
        final TableModel cachedTableModel = (TableModel) ((CacheElement) element.getObjectValue()).getTable();
        if (cachedTableModel != null)
        {
          // we have a entry in the cache ... great!
          logger.debug("Found tableModel in cache. Returning");
          // Print cache status size
          logger.debug("Cache status: " + cache.getMemoryStoreSize() + " in memory, " + 
                  cache.getDiskStoreSize() + " in disk");
          return cachedTableModel;
        }
      }
      return null;
    }
    catch(Exception e){
      logger.error("Error while attempting to load from cache, bypassing cache (cause: " + e.getClass() + ")", e);
      return null;
    }
    finally{
      Thread.currentThread().setContextClassLoader(contextCL);
    }
  }

  @Override
  public void clearCache() {
    cache.removeAll();
  }
  
  public Cache getCache(){
    return this.cache;
  }

  @Override
  public boolean remove(TableCacheKey key) {
    return cache.remove(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterable<TableCacheKey> getKeys() {
    return cache.getKeys();
  }

  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key) 
  {
    Element element = cache.getQuiet(key);
    if(element == null){
      logger.warn("Null element in cache, removing.");
      remove(key);
      return null;
    }
    Object val = element.getValue();
    if(val instanceof CacheElement){
      return ((CacheElement)val).getInfo();  
    }
    else {
      logger.error("Expected " + CacheElement.class.getCanonicalName() + ", found " + val.getClass().getCanonicalName() + " instead");
      remove(key);
      return null;
    }
  }

  @Override
  public CacheElementInfo getElementInfo(TableCacheKey key) {
    
    Element element = cache.getQuiet(key);
    CacheElementInfo info = new CacheElementInfo();
    info.setKey(key);
    if(element!= null){
      
      info.setInsertTime(element.getLatestOfCreationAndUpdateTime());
      info.setAccessTime(element.getLastAccessTime());
      info.setHits(element.getHitCount());
      Object val = element.getValue();
      if(val instanceof CacheElement){
        info.setRows(((CacheElement) val).getTable().getRowCount()); 
      }
    }
    return info;
  }

  @Override
  public int removeAll(String cdaSettingsId, String dataAccessId) {
    int deleteCount = 0;
    
    if(cdaSettingsId == null){
      deleteCount = cache.getSize();
      clearCache();
    }
    
    for(TableCacheKey key : getKeys()){
      ExtraCacheInfo info = ((CacheElement)cache.getQuiet(key).getObjectValue()).getInfo();
      
      if(StringUtils.equals(cdaSettingsId, info.getCdaSettingsId()) &&
              (dataAccessId==null || 
               StringUtils.equals(dataAccessId, info.getDataAccessId())))
      {
        if(remove(key)) deleteCount++;
      }
    }
    return deleteCount;
  }



  @Override
  public void shutdownIfRunning() {
    if(cacheManager!= null){
      if(cache!=null){
        cache.flush();
      }
      if(cacheManager.getStatus() == Status.STATUS_ALIVE){
        logger.debug("Shutting down cache manager.");
        cacheManager.shutdown();
        cacheManager = null;
      }
    }
  }


}
