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

package pt.webdetails.cda.cache.monitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import pt.webdetails.cda.exporter.JsonExporter;

/**
 * Info about cached item that shouldn't be factored in key comparison
 */
public class ExtraCacheInfo implements Serializable {//TODO: serialization

  private static final long serialVersionUID = 1L;
  private static final int TABLE_SNAPSHOT_ROWS = 10;

  static Log logger = LogFactory.getLog(ExtraCacheInfo.class);
  
  private String cdaSettingsId;
  private String dataAccessId;
  private long queryDurationMs;
  private JSONObject tableSnapshot;
  private int nbrRows;
  
  private long entryTime;
  private int timeToLive;//TODO: delete?
  
  
  public ExtraCacheInfo(String cdaSettingsId, String dataAccessId, long queryDurationMs, TableModel tm)
  {
    this.cdaSettingsId = cdaSettingsId;
    this.dataAccessId = dataAccessId;
    this.queryDurationMs = queryDurationMs;
    this.nbrRows = tm.getRowCount();
    JsonExporter exporter = new JsonExporter(null);
    
    try
    {
      this.tableSnapshot = exporter.getTableAsJson(tm, TABLE_SNAPSHOT_ROWS);
    } 
    catch(Exception e)
    {
      logger.error("Error exporting table snapshot as json.", e);
    }
  }

  public String getCdaSettingsId() {
    return cdaSettingsId;
  }

  public void setCdaSettingsId(String cdaSettingsId) {
    this.cdaSettingsId = cdaSettingsId;
  }

  public String getDataAccessId() {
    return dataAccessId;
  }

  public void setDataAccessId(String dataAccessId) {
    this.dataAccessId = dataAccessId;
  }

  public long getQueryDurationMs() {
    return queryDurationMs;
  }

  public void setQueryDurationMs(long queryDurationMs) {
    this.queryDurationMs = queryDurationMs;
  }

  public JSONObject getTableSnapshot() {
    return tableSnapshot;
  }

  public void setTableSnapshot(JSONObject tableSnapshot) {
    this.tableSnapshot = tableSnapshot;
  }

  public int getNbrRows() {
    return nbrRows;
  }

  public void setNbrRows(int nbrRows) {
    this.nbrRows = nbrRows;
  }

  public long getEntryTime() {
    return entryTime;
  }

  public void setEntryTime(long entryTime) {
    this.entryTime = entryTime;
  }

  public int getTimeToLive() {
    return timeToLive;
  }

  public void setTimeToLive(int timeToLive) {
    this.timeToLive = timeToLive;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(cdaSettingsId);
    out.writeObject(dataAccessId);
    out.writeLong(queryDurationMs);
    out.writeInt(nbrRows);
    out.writeLong(entryTime);
    out.writeInt(timeToLive);
    out.writeObject(tableSnapshot != null ? tableSnapshot.toString() : null);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    cdaSettingsId = (String) in.readObject();
    dataAccessId = (String) in.readObject();
    queryDurationMs = in.readLong();
    nbrRows = in.readInt();
    entryTime = in.readLong();
    timeToLive = in.readInt();
    
    try {
      tableSnapshot = new JSONObject( (String) in.readObject() );
    } catch (Exception e) {
      tableSnapshot = null;
    }
  }
  
  @Override
  public String toString(){
    return ExtraCacheInfo.class.getName() + 
        " [cdaSettingsId=" + cdaSettingsId +
        ", dataAccessId=" + dataAccessId + 
        ", entryTime=" + entryTime +
        ", timeToLive=" + timeToLive +
        ", queryDurationMs=" + queryDurationMs +
        ", nbrRows=" + nbrRows +
        "]";
  }
  
}
