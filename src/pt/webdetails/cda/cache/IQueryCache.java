package pt.webdetails.cda.cache;

import javax.swing.table.TableModel;

import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

public interface IQueryCache {

  /**
   * Stores element in cache.
   * @param key key
   * @param table element to store.
   * @param ttlSec time to live in seconds
   */
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec);
  
  /**
   * Stores element in cache.
   * @param key key
   * @param table element to store.
   * @param ttlSec time to live in seconds
   * @param cacheInfo extra information for cache items that doesn't
   */
  public void putTableModel(TableCacheKey key, TableModel table, ExtraCacheInfo cacheInfo);
  
  
  /**
   * 
   * @param key the key to retrieve.
   * @return <code>TableModel</code> associated with key.
   */
  public TableModel getTableModel(TableCacheKey key);
  
//  /**
//   * 
//   * @param key the key to retrieve.
//   * @return <code>TableModel</code> associated with key.
//   */
//  public TableModel getTableModelQuietly(TableCacheKey key);
  
  /**
   * Removes element with given key from cache.
   * @param key
   * @return <code>true</code> if element existed.
   */
  public boolean remove(TableCacheKey key);
  
  /**
   * Removes all elements from cache.
   **/
  public void clearCache();
  
  /**
   * 
   * @return all keys in cache;
   */
  public Iterable<TableCacheKey> getKeys();
  
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key);
  
}
