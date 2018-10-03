/*
 *
 *  * Copyright 2018 Hitachi Vantara. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *
 */

package pt.webdetails.cda.utils;

import org.json.JSONException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryParametersTest {

  @Test
  public void testGetParameters() throws JSONException {
    String message = "{param1: value1, param2: value2, array: [array1, array2], param3: value3, param3: value31}";
    Map<String, List<String>> params = new QueryParameters().getParametersFromJson( message );
    assertEquals( 4, params.size() );
    assertTrue( params.get( "param1" ) != null );
    assertTrue( params.get( "param2" ) != null );
    assertTrue( params.get( "array" ) != null );
    assertTrue( params.get( "param3" ) != null );
    assertEquals( "value1", params.get( "param1" ).get( 0 ) );
    assertEquals( "value2", params.get( "param2" ).get( 0 ) );
    assertEquals( "array1", params.get( "array" ).get( 0 ) );
    assertEquals( "array2", params.get( "array" ).get( 1 ) );
    assertEquals( 1, params.get( "param3" ).size() );
    assertEquals( "value31", params.get( "param3" ).get( 0 ) );
  }

  @Test
  public void testGetDoQueryParameters() throws Exception {
    String message = "{param1: value1, param2: value2, array: [array1, array2], param3: value3, param3: value31, setting1: set}";
    Map<String, List<String>> params = new QueryParameters().getParametersFromJson( message );
    DoQueryParameters doQueryParameters = new QueryParameters().getDoQueryParameters( params );
    assertEquals( "json", doQueryParameters.getOutputType() );
    assertEquals( 3, doQueryParameters.getParameters().size() );
    assertEquals( "value1", doQueryParameters.getParameters().get( "1" ) );
    assertEquals( "value2", doQueryParameters.getParameters().get( "2" ) );
    assertEquals( "value31", doQueryParameters.getParameters().get( "3" ) );
    assertEquals( 1, doQueryParameters.getExtraSettings().size() );
    assertEquals( "set", doQueryParameters.getExtraSettings().get("1") );
  }

  @Test
  public void testMultiValueQueryParameterAddOperation() {
    QueryParameters queryParameters = new QueryParameters();
    Map<String, List<String>> map = new HashMap<>();

    assertNotNull( map );
    assertEquals( 0, map.size() );

    queryParameters.addMultiValueToMap( map, "key1", "value1" );
    assertEquals( 1, map.size() );
    assertEquals( "value1", map.get( "key1" ).get( 0 ) );

    queryParameters.addMultiValueToMap( map, "key1", "value2" );
    assertEquals( 1, map.size() );
    assertEquals( "value1", map.get( "key1" ).get( 0 ) );
    assertEquals( "value2", map.get( "key1" ).get( 1 ) );
  }

  @Test
  public void testMultiValueQueryParameterAddOperationNulls() {
    QueryParameters queryParameters = new QueryParameters();
    Map<String, List<String>> map = new HashMap<>();

    assertNotNull( map );
    assertEquals( 0, map.size() );

    queryParameters.addMultiValueToMap( map, "key1", null );
    assertEquals( 0, map.size() );

    queryParameters.addMultiValueToMap( map, "key2", "value1" );
    assertEquals( 1, map.size() );
    assertEquals( "value1", map.get( "key2" ).get( 0 ) );
    queryParameters.addMultiValueToMap( map, "key2", null );
    assertEquals( 1, map.size() );
    assertEquals( "value1", map.get( "key2" ).get( 0 ) );
  }
}
