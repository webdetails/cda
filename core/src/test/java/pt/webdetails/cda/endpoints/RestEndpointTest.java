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

package pt.webdetails.cda.endpoints;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static pt.webdetails.cda.endpoints.RestEndpoint.PREFIX_PARAMETER;
import static pt.webdetails.cda.endpoints.RestEndpoint.PREFIX_SETTING;

public class RestEndpointTest {

  private RestEndpoint restEndpoint;
  private HttpServletRequest servletRequestMock;
  private MultivaluedMap<String, String> formParameters;

  @Before
  public void setup() {
    this.restEndpoint = spy( new RestEndpoint() );
    this.servletRequestMock = mock( HttpServletRequest.class );
    this.formParameters = new MultivaluedHashMap<>();
  }

  @Test
  public void testGetParameters_singleValueExtraParameterFromRequest() {
    final String expectedName = "foo1";
    final String expectedValue = "bar";

    mockGetParameterMapFromRequest( PREFIX_PARAMETER + expectedName, expectedValue );

    assertGetParametersFromRequest( PREFIX_PARAMETER, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_multipleValueExtraParameterFromRequest() {
    final String expectedName = "foo2";
    final String[] expectedValue = getParameterValues( "bar1", "bar2" );

    mockGetParameterMapFromRequest( PREFIX_PARAMETER + expectedName, expectedValue );

    assertGetParametersFromRequest( PREFIX_PARAMETER, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_singleValueExtraSettingFromRequest() {
    final String expectedName = "foo3";
    final String expectedValue = "bar";

    mockGetParameterMapFromRequest( PREFIX_SETTING + expectedName, expectedValue );

    assertGetParametersFromRequest( PREFIX_SETTING, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_multipleValueExtraSettingFromRequest() {
    final String expectedName = "foo4";
    final String[] expectedValue = getParameterValues( "bar1", "bar2" );

    mockGetParameterMapFromRequest( PREFIX_SETTING + expectedName, expectedValue );

    assertGetParametersFromRequest( PREFIX_SETTING, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_singleValueExtraParameterFromMultivaluedMap() {
    final String expectedName = "foo1";
    final String expectedValue = "bar1";

    this.formParameters.add( PREFIX_PARAMETER + expectedName, expectedValue );

    assertGetParametersFromMultivaluedMap( PREFIX_PARAMETER, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_multipleValueExtraParameterFromMultivaluedMap() {
    final String expectedName = "foo2";
    final String[] expectedValue = getParameterValues( "bar1", "bar2" );

    this.formParameters.addAll( PREFIX_PARAMETER + expectedName, expectedValue );

    assertGetParametersFromMultivaluedMap( PREFIX_PARAMETER, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_singleValueExtraSettingFromMultivaluedMap() {
    final String expectedName = "foo3";
    final String expectedValue = "bar3";

    this.formParameters.add( PREFIX_SETTING + expectedName, expectedValue );

    assertGetParametersFromMultivaluedMap( PREFIX_SETTING, expectedName, expectedValue );
  }

  @Test
  public void testGetParameters_multipleValueExtraSettingFromMultivaluedMap() {
    final String expectedName = "foo4";
    final String[] expectedValue = getParameterValues( "bar1", "bar2" );

    this.formParameters.addAll( PREFIX_SETTING + expectedName, expectedValue );

    assertGetParametersFromMultivaluedMap( PREFIX_SETTING, expectedName, expectedValue );
  }

  private void mockGetParameterMapFromRequest( String paramName, String ...paramValues ) {
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put( paramName, paramValues );

    doReturn( parameterMap ).when( this.servletRequestMock ).getParameterMap();
  }

  private void assertGetParametersFromRequest( String parameterType, String expectedName, Object expectedValue ) {
    Map<String, Object> extraParameters = this.restEndpoint.getParameters( this.servletRequestMock, parameterType );

    assertEquals( 1, extraParameters.size() );
    assertEquals( expectedValue, extraParameters.get( expectedName ) );
  }

  private void assertGetParametersFromMultivaluedMap( String parameterType, String expectedName, String expectedValue ) {
    Map<String, Object> extraParameters = this.restEndpoint.getParameters( this.formParameters, parameterType );

    assertEquals( 1, extraParameters.size() );
    assertEquals( expectedValue, extraParameters.get( expectedName ) );
  }

  private void assertGetParametersFromMultivaluedMap( String parameterType, String expectedName, String[] expectedValue ) {
    Map<String, Object> extraParameters = this.restEndpoint.getParameters( this.formParameters, parameterType );

    assertEquals( 1, extraParameters.size() );

    String[] actualValue = (String[]) extraParameters.get( expectedName );
    for ( int i = 0; i < expectedValue.length; i++ ) {
      assertEquals( expectedValue[ i ], actualValue[ i ] );
    }
  }

  private String[] getParameterValues( String ...values ) {
    return values;
  }
}
