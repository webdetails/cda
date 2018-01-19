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

package pt.webdetails.cda.connections.dataservices;

import org.dom4j.Element;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cda.filetests.CdaTestCase;
import java.util.Arrays;
import static org.mockito.Mockito.*;


public class DataservicesConnectionInfoTest extends CdaTestCase {

  @Test
  public void testHashCode() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertEquals( 0, dataservicesConnectionInfo.hashCode() );
  }

  @Test
  public void testEquals() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( dataservicesConnectionInfo ) );
    Element otherConnection = mock( Element.class );
    DataservicesConnectionInfo differentDataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( differentDataservicesConnectionInfo ) );
    when( otherConnection.selectObject( Mockito.anyString() ) ).thenReturn( "someStr" );
    when( connection.attributeValue( Mockito.anyString() ) ).thenReturn( "someValue" );
    when( connection.getText() ).thenReturn( "someText" );
    when( otherConnection.elements( Mockito.anyString() ) ).thenReturn( Arrays.asList( connection ) );
    DataservicesConnectionInfo otherDataservicesConnectionInfo = new DataservicesConnectionInfo( otherConnection );
    assertEquals( 2, otherDataservicesConnectionInfo.getProperties().size() );
    assertEquals( "someStr", otherDataservicesConnectionInfo.getProperties().getProperty( "dataServiceName" ) );
    assertEquals( "someText", otherDataservicesConnectionInfo.getProperties().getProperty( "someValue" ) );
    assertFalse( dataservicesConnectionInfo.equals( otherDataservicesConnectionInfo ) );
    assertFalse( dataservicesConnectionInfo.equals( new Object() ) );
  }
}
