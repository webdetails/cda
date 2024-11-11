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


package pt.webdetails.cda.events;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cpf.messaging.JsonSerializable;
import pt.webdetails.cpf.messaging.PluginEvent;

public abstract class CdaEvent extends PluginEvent {

  public static class Fields extends PluginEvent.Fields {
    public static  final String QUERY_INFO = "queryInfo";
  }

  enum CdaEventType {

    QueryTooLong, QueryError,
  }

  public static class QueryInfo implements JsonSerializable {

    String cdaSettingsId;
    String dataAccessId;
    String query;
    ParameterDataRow parameters;

    public QueryInfo( String cdaSettingsId, String dataAccessId, String query, ParameterDataRow parameters ) {
      this.cdaSettingsId = cdaSettingsId;
      this.dataAccessId = dataAccessId;
      this.query = query;
      this.parameters = parameters;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
      JSONObject obj = new JSONObject();
      obj.put( "cdaSettingsId", cdaSettingsId );
      obj.put( "dataAccessId", dataAccessId );
      obj.put( "query", query );

      JSONArray params = new JSONArray();
      for ( String paramName : parameters.getColumnNames() ) {
        JSONArray param = new JSONArray();
        param.put( 0, (Object) paramName ).put( 1, (Object) parameters.get( paramName ) );
        params.put( param );
      }
      obj.put( "parameters", params );

      return obj;
    }
  }

  private QueryInfo queryInfo;


  public CdaEvent( CdaEventType eventType, QueryInfo queryInfo ) throws JSONException {
    super( "cda", eventType.toString(), queryInfo.cdaSettingsId );
    this.queryInfo = queryInfo;
    setKey( eventType.toString() );
  }

  public QueryInfo getQueryInfo() {
    return queryInfo;
  }

  public void setQueryInfo( QueryInfo queryInfo ) {
    this.queryInfo = queryInfo;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject obj = super.toJSON();
    obj.put( Fields.QUERY_INFO, queryInfo.toJSON() );
    return obj;
  }
}
