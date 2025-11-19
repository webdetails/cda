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


package pt.webdetails.cda.cache.monitor;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.IOException;

/**
 * Info about cached item that shouldn't be factored in key comparison
 */
public class ExtraCacheStats implements Serializable {

  private static final long serialVersionUID = 1L;

  private long hits;
  private long lastAccessTime;
  private long insertOrUpdateTime;

  public ExtraCacheStats( long hits, long lastAccessTime, long insertOrUpdateTime ) {
    this.hits = hits;
    this.lastAccessTime = lastAccessTime;
    this.insertOrUpdateTime = insertOrUpdateTime;
  }

  public long getHits() {
    return hits;
  }

  public void setHits(long hits) {
    this.hits = hits;
  }

  public long getLastAccessTime() {
    return lastAccessTime;
  }

  public void setLastAccessTime(long lastAccessTime) {
    this.lastAccessTime = lastAccessTime;
  }

  public long getInsertOrUpdateTime() {
    return insertOrUpdateTime;
  }

  public void setInsertOrUpdateTime(long insertOrUpdateTime) {
    this.insertOrUpdateTime = insertOrUpdateTime;
  }

  @Serial
  private void writeObject(ObjectOutputStream out ) throws IOException {
    out.writeLong( hits );
    out.writeLong( lastAccessTime );
    out.writeLong( insertOrUpdateTime );
  }

  @Serial
  private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
    hits = in.readLong();
    lastAccessTime = in.readLong();
    insertOrUpdateTime = in.readLong();
  }

  @Override
  public String toString() {
    return ExtraCacheStats.class.getName()
      + " [hits=" + hits
      + ", lastAccessTime=" + lastAccessTime
      + ", insertAndUpdateTime=" + insertOrUpdateTime
      + "]";
  }
}
