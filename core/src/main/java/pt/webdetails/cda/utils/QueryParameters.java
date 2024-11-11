/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides some query parameters functions.
 */
public class QueryParameters {

  private final String PREFIX_PARAMETER = "param";
  private final String PREFIX_SETTING = "setting";

  /**
   * Gets a {@link Map<String, List<String>>} from the message received.
   * @param message a JSON message with parameters.
   * @return a multi value map of values obtained from the JSON message received as parameter.
   * @throws JSONException Is thrown when there is a problem converting the message into a JSON object.
   */
  public Map<String, List<String>> getParametersFromJson( String message ) throws JSONException {
    Map<String, List<String>> params = new HashMap();

    JSONObject jsonObject = new JSONObject( message );
    Iterator<?> keys = jsonObject.keys();

    while ( keys.hasNext() ) {
      String key = (String) keys.next();
      Object item = jsonObject.get( key );
      if ( item instanceof JSONArray ) {
        JSONArray itemArray = (JSONArray) item;
        for ( int i = 0; i < itemArray.length(); i++ ) {
          addMultiValueToMap( params, key, itemArray.getString( i ) );
        }
      } else {
        addMultiValueToMap( params, key, jsonObject.getString( key ) );
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
  public DoQueryParameters getDoQueryParameters( Map<String, List<String>> parameters ) throws Exception {
    DoQueryParameters doQueryParams = new DoQueryParameters();

    //should populate everything but prefixed parameters TODO: recheck defaults
    BeanUtils.populate( doQueryParams, parameters );

    Map<String, Object> params = new HashMap<String, Object>();
    Map<String, Object> extraSettings = new HashMap<String, Object>();
    for ( String name : parameters.keySet() ) {
      if ( name.startsWith( PREFIX_PARAMETER ) ) {
        params.put( name.replaceFirst( PREFIX_PARAMETER, "" ), getParam( parameters.get( name ) ) );
      } else if ( name.startsWith( PREFIX_SETTING ) ) {
        extraSettings.put( name.replaceFirst( PREFIX_SETTING, "" ), getParam( parameters.get( name ) ) );
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
  private Object getParam( List<String> paramValues ) {
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

  public void addMultiValueToMap( Map<String, List<String>> map, String key, String value ) {
    if ( value != null ) {
      List<String> values = map.get( key );
      if ( values != null ) {
        values.add( value );
      } else {
        ArrayList<String> newValuesList = new ArrayList<>();
        newValuesList.add( value );
        map.put( key, newValuesList );
      }
    }
  }
}
