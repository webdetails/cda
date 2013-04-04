/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.events;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryTooLongEvent extends CdaEvent { //implements JsonSerializable {

//  public static class Fields extends CdaEvent.Fields {
//    public static final String DURATION = "duration";
//  }
  
  private long duration;

  public QueryTooLongEvent(QueryInfo queryInfo, long duration) throws JSONException{
    super(CdaEventType.QueryTooLong, queryInfo);
    this.duration = duration;
  }
  
  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject obj = super.toJSON();
    obj.put("duration", duration);
    return obj;
  }

}
