/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.cache.scheduler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.CdaSettingsReadException;
import pt.webdetails.cda.settings.SettingsManager;

public class QueryExecution {
  private CdaSettings cdaSettings;
  private QueryOptions queryOptions;
  private String cronString;
  private String jsonString;

  public QueryExecution( SettingsManager settingsMgr, String jsonString ) throws
    JSONException,
    CdaSettingsReadException,
    AccessDeniedException {
    JSONObject json = new JSONObject( jsonString );
    this.jsonString = jsonString;
    String cdaFile = json.getString( "cdaFile" );
    cdaSettings = settingsMgr.parseSettingsFile( cdaFile );
    queryOptions = createQueryOptions( json );
    cronString = json.getString( "cronString" );
  }

  public CdaSettings getCdaSettings() {
    return cdaSettings;
  }

  public QueryOptions getQueryOptions() {
    return queryOptions;
  }

  public String getJsonString() {
    return jsonString;
  }

  public String getCronString() {
    return cronString;
  }

  public static QueryOptions createQueryOptions( JSONObject json ) throws JSONException {
    QueryOptions queryOpts = new QueryOptions();
    queryOpts.setDataAccessId( json.getString( "dataAccessId" ) );

    if ( json.has( "parameters" ) ) {
      Object parameters = json.get( "parameters" );
      if ( parameters instanceof JSONArray ) {
        JSONArray parametersArray = (JSONArray) parameters;
        for ( int i = 0; i < parametersArray.length(); i++ ) {
          JSONObject param = parametersArray.getJSONObject( i );
          String name = param.getString( "name" );
          JSONArray arrayOpt = param.optJSONArray( "value" );
          if ( arrayOpt != null ) {
            // array parameter
            String[] values = new String[ ( arrayOpt.length() ) ];
            for ( int j = 0; j < values.length; j++ ) {
              values[ i ] = arrayOpt.getString( j );
            }
            queryOpts.addParameter( name, values );
          } else {
            // regular parameter
            String value = param.getString( "value" );
            queryOpts.addParameter( name, value );
          }
        }
      }
    }
    // ensure cache is refreshed
    queryOpts.setCacheBypass( true );
    return queryOpts;
  }
}
