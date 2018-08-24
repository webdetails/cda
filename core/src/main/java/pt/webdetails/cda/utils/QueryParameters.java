/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This abstract class with some query parameters functions.
 */
public abstract class QueryParameters {

  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";

  /**
   * Gets a {@link MultivaluedMap} from the message received.
   * @param message a JSON message with parameters.
   * @return a multi value map of values obtained from the JSON message received as parameter.
   * @throws JSONException Is thrown when there is a problem converting the message into a JSON object.
   */
  public static MultivaluedMap<String, String> getParameters( String message ) throws JSONException {
    MultivaluedMap<String, String> params = new MultivaluedHashMap<>( );

    JSONObject jsonObject = new JSONObject( message );
    Iterator<?> keys = jsonObject.keys();

    while ( keys.hasNext() ) {
      String key = (String) keys.next();
      Object item = jsonObject.get( key );
      if ( item instanceof JSONArray ) {
        JSONArray itemArray = (JSONArray) item;
        for ( int i = 0; i < itemArray.length(); i++ ) {
          params.add( key, itemArray.getString( i ) );
        }
      } else {
        params.add( key, jsonObject.getString( key ) );
      }
    }
    return params;
  }

  /**
   * Get the parameters from a query
   * @param parameters the query parameters
   * @return A {@link DoQueryParameters} object initialized with the received parameters.
   * @throws Exception
   */
  public static DoQueryParameters getDoQueryParameters( MultivaluedMap<String, String> parameters ) throws Exception {
    DoQueryParameters doQueryParams = new DoQueryParameters();

    //should populate everything but prefixed parameters TODO: recheck defaults
    BeanUtils.populate( doQueryParams, parameters );

    Map<String, Object> params = new HashMap<String, Object>();
    Map<String, Object> extraSettings = new HashMap<String, Object>();
    for ( String name : parameters.keySet() ) {
      if ( name.startsWith( PREFIX_PARAMETER ) ) {
        params.put( name.substring( PREFIX_PARAMETER.length() ), getParam( parameters.get( name ) ) );
      } else if ( name.startsWith( PREFIX_SETTING ) ) {
        extraSettings.put( name.substring( PREFIX_SETTING.length() ), getParam( parameters.get( name ) ) );
      }
    }
    doQueryParams.setParameters( params );
    doQueryParams.setExtraSettings( extraSettings );

    return doQueryParams;
  }

  /**
   * Gets the param object from the param list, taking into account if it's a list or  a list with one object.
   * @param paramValues the parameter values received in a multivalued map.
   * @return the object as an array, or the object value if the received list has size 1.
   */
  private static Object getParam( List<String> paramValues ) {
    if ( paramValues == null ) {
      return null;
    }
    if ( paramValues.size() == 1 ) {
      return paramValues.get( 0 );
    }
    if ( paramValues instanceof List ) {
      return paramValues.toArray();
    }
    return paramValues;
  }
}
