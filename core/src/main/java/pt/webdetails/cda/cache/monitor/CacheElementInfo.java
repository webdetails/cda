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

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.dataaccess.Parameter;

public class CacheElementInfo {

  TableCacheKey key;

  Integer rows;
  long insertTime;
  long accessTime;
  long hits;
  Long byteSize;
  Long duration;
  int timeToLive;

  public TableCacheKey getKey() {
    return key;
  }

  public void setKey( TableCacheKey key ) {
    this.key = key;
  }

  public Integer getRows() {
    return rows;
  }

  public void setRows( Integer rows ) {
    this.rows = rows;
  }

  public long getInsertTime() {
    return insertTime;
  }

  public void setInsertTime( long insertTime ) {
    this.insertTime = insertTime;
  }

  public long getAccessTime() {
    return accessTime;
  }

  public void setAccessTime( long accessTime ) {
    this.accessTime = accessTime;
  }

  public long getHits() {
    return hits;
  }

  public void setHits( long hits ) {
    this.hits = hits;
  }

  public Long getByteSize() {
    return byteSize;
  }

  public void setByteSize( Long byteSize ) {
    this.byteSize = byteSize;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration( Long duration ) {
    this.duration = duration;
  }

  public int getTimeToLive() {
    return timeToLive;
  }

  public void setTimeToLive( int timeToLive ) {
    this.timeToLive = timeToLive;
  }

  public JSONObject toJson() throws JSONException, IOException {
    JSONObject queryInfo = new JSONObject();
    queryInfo.put( "query", key.getQuery() );

    JSONObject parameters = new JSONObject();
    for ( Parameter param : key.getParameters() ) {
      parameters.put( param.getName(), param.getStringValue() );
    }
    queryInfo.put( "parameters", parameters );

    queryInfo.put( "rows", rows != null ? rows.intValue() : null );

    // inserted
    queryInfo.put( "inserted", insertTime );
    queryInfo.put( "accessed", accessTime );
    queryInfo.put( "hits", hits );
    queryInfo.put( "size", byteSize != null ? byteSize.longValue() : null );

    if ( duration != null ) {
      queryInfo.put( "duration", duration.longValue() );
    }
    queryInfo.put( "timeToLive", timeToLive );

    // use id to get table identifier
    String identifier = TableCacheKey.getTableCacheKeyAsString( key );
    queryInfo.put( "key", identifier );

    return queryInfo;
  }

  @Override
  public String toString() {
    try {
      return toJson().toString();
    } catch ( Exception e ) {
      return super.toString();
    }
  }

}
