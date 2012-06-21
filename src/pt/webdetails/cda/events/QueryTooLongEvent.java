/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.events;

import org.json.JSONException;

public class QueryTooLongEvent extends CdaEvent { //implements JsonSerializable {//TODO: make JsonSerializable work both ways... use annotations

//  public static class Fields extends PluginEvent.Fields {
//    public static final String DURATION = "duration";
//    public static final String QUERY_INFO = "queryInfo";
//  }
  

  public QueryTooLongEvent(QueryInfo queryInfo, long duration) throws JSONException{
    super(CdaEventType.QueryTooLong, queryInfo);
    getEvent().put("duration", duration);
//    this.duration = duration;
  }
  
//  public String getEventType(){
//    return "queryDelay";
//  }
//  
//  private QueryInfo queryInfo;
//  
//  public QueryInfo getQueryInfo(){
//    return queryInfo;
//  }
//  
//  private long timeStamp = System.currentTimeMillis();
//  
//  public long getTimeStamp(){
//    return timeStamp;
//  }
//  
//  private long duration;
//  
  
//  @Override
//  public JSONObject toJSON() throws JSONException {
//    JSONObject obj = super.toJSON();
////        new JSONObject();
////    obj.put("eventType", getEventType()); 
////    obj.put("timestamp", getTimeStamp());
////    obj.put("queryInfo", queryInfo.toJSON());
//    //
//    obj.put("duration", duration);
//    return obj;
//  }

}
