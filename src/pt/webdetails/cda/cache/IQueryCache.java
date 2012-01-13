package pt.webdetails.cda.cache;

import javax.swing.table.TableModel;

import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

public interface IQueryCache {

  /**
   * Stores element in cache.
   * @param key key
   * @param table element to store.
   * @param ttlSec time to live in seconds
   */
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec);
  
//  /**
//   * Stores element in cache.
//   * @param key key
//   * @param table element to store.
//   * @param ttlSec time to live in seconds
//   * @param cacheInfo extra information for cache items that doesn't affect cache comparison
//   */
//  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo cacheInfo);
  
  
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
   * Clears all elements that match given IDs
   * @param cdaSettingsId If null, deletes everything (same as clearCache)
   * @param dataAccessId Only used if cdaSettingsId is also provided;<code>null</code> matches all
   * @return Number of deleted entries
   */
  public int removeAll(String cdaSettingsId, String dataAccessId);
  
  /**
   * Removes all elements from cache.
   **/
  public void clearCache();
  
  /**
   * 
   * @return all keys in cache;
   */
  public Iterable<TableCacheKey> getKeys();
  
  public CacheElementInfo getElementInfo(TableCacheKey key);
//  
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key);
  
}
