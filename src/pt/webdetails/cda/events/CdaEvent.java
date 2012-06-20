/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.events;

import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cpf.JsonSerializable;
import pt.webdetails.cpf.messaging.PluginEvent;

public abstract class CdaEvent extends PluginEvent {
  
  enum CdaEventType {
    QueryTooLong,
    QueryError,
  }
  
  public static class QueryInfo implements JsonSerializable {
    String cdaSettingsId;
    String dataAccessId;
    String query;
    ParameterDataRow parameters;
    
    public QueryInfo(String cdaSettingsId, String dataAccessId, String query, ParameterDataRow parameters){
      this.cdaSettingsId = cdaSettingsId;
      this.dataAccessId = dataAccessId;
      this.query = query;
      this.parameters = parameters;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
      JSONObject obj = new JSONObject();
      obj.put("cdaSettingsId", cdaSettingsId);
      obj.put("dataAccessId", dataAccessId);
      obj.put("query", query);
      
      JSONObject params = new JSONObject();
      for(String paramName : parameters.getColumnNames()){
        params.put(paramName,parameters.get(paramName));
      }
      obj.put("parameters", params);
      
      return obj;
    }

  }

  public CdaEvent(CdaEventType eventType, QueryInfo queryInfo) throws JSONException{
    super("cda",eventType.toString(),null);
    JSONObject event = new JSONObject();
    event.put("queryInfo", queryInfo.toJSON());
    setEvent(event);
  }

}
