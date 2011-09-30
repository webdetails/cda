package pt.webdetails.cda.cache;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class EHCacheQueryCache implements IQueryCache {

  private static final Log logger = LogFactory.getLog(EHCacheQueryCache.class);
  
  Cache cache = null;
  
  public EHCacheQueryCache(Cache cache){
    this.cache = cache;
  }
  
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec) 
  {
    final Element storeElement = new Element(key, table);
    storeElement.setTimeToLive(ttlSec);
    cache.put(storeElement);
    cache.flush();
    
    // Print cache status size
    logger.debug("Cache status: " + cache.getMemoryStoreSize() + " in memory, " + 
            cache.getDiskStoreSize() + " in disk");
  }
  
//  public void putTableModel(TableCacheKey key, TableModel table){
//    putTableModel(key, table, )
//  }

  @Override
  public TableModel getTableModel(TableCacheKey key) {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try{
      //make sure we have the right class loader in thread to instantiate cda classes in case DiskStore is used
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      final Element element = cache.get(key);
      if (element != null) // Are we explicitly saying to bypass the cache?
      {
        final TableModel cachedTableModel = (TableModel) element.getObjectValue();
        if (cachedTableModel != null)
        {
          // we have a entry in the cache ... great!
          logger.debug("Found tableModel in cache. Returning");
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

  public TableModel getTableModelQuietly(TableCacheKey key) {
    Element el = cache.getQuiet(key);
    return el != null? (TableModel)el.getValue() : null;
  }

  @Override
  public void putTableModel(TableCacheKey key, TableModel table, ExtraCacheInfo cacheInfo) {
    putTableModel(key, table, 3600);//TODO: temp
  }

  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key) 
  {
    Element element = cache.getQuiet(key);
    return new ExtraCacheInfo(null, null, element.getTimeToLive(), (TableModel) element.getValue());
  }

}
