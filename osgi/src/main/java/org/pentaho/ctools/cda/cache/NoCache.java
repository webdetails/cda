/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.ctools.cda.cache;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

import javax.swing.table.TableModel;
import java.util.Collections;

public class NoCache implements IQueryCache {
  @Override
  public void putTableModel(TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo cacheInfo) {

  }

  @Override
  public TableModel getTableModel(TableCacheKey key) {
    return null;
  }

  @Override
  public boolean remove(TableCacheKey key) {
    return true;
  }

  @Override
  public int removeAll(String cdaSettingsId, String dataAccessId) {
    return 0;
  }

  @Override
  public void clearCache() {

  }

  @Override
  public Iterable<TableCacheKey> getKeys() {
    return Collections.emptyList();
  }

  @Override
  public CacheElementInfo getElementInfo(TableCacheKey key) {
    return null;
  }

  @Override
  public ExtraCacheInfo getCacheEntryInfo(TableCacheKey key) {
    return null;
  }

  @Override
  public void shutdownIfRunning() {

  }
}
