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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cda.filetests.CdaTestCase;
import pt.webdetails.cpf.Util;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataservicesConnectionInfoTest extends CdaTestCase {

  @Test
  public void testConstructor() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    ArrayList<Element> properties = new ArrayList<>( );
    properties.add( getElementFromSnippet( "<prop name=\"prop1\">prop value 1</prop>" ) );
    properties.add( getElementFromSnippet( "<prop name=\"prop2\">prop value 2</prop>" ) );
    when( connection.elements( "Property" ) ).thenReturn( properties );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertNotNull( dataservicesConnectionInfo.getProperties() );
    assertEquals( 2, dataservicesConnectionInfo.getProperties().size() );
    assertEquals( "prop value 1", dataservicesConnectionInfo.getProperties().getProperty( "prop1" ) );
    assertEquals( "prop value 2", dataservicesConnectionInfo.getProperties().getProperty( "prop2" ) );
  }

  @Test
  public void testConstructorNoElements() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    ArrayList<Element> properties = new ArrayList<>( );
    when( connection.elements( "Property" ) ).thenReturn( properties );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertNotNull( dataservicesConnectionInfo.getProperties() );
    assertEquals( 0, dataservicesConnectionInfo.getProperties().size() );
  }

  @Test
  public void testHashCode() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    ArrayList<Element> properties = new ArrayList<>( );
    properties.add( getElementFromSnippet( "<prop name=\"prop1\">prop value 1</prop>" ) );
    properties.add( getElementFromSnippet( "<prop name=\"prop2\">prop value 2</prop>" ) );
    when( connection.elements( "Property" ) ).thenReturn( properties );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertEquals( dataservicesConnectionInfo.getProperties().hashCode(), dataservicesConnectionInfo.hashCode() );
  }

  @Test
  public void testGetProperties() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertNotNull( dataservicesConnectionInfo.getProperties() );
    assertEquals( 0, dataservicesConnectionInfo.getProperties().size() );
  }

  @Test
  public void testEquals() throws Exception {
    Element connection = mock( Element.class );
    ArrayList<Element> properties = new ArrayList<>( );
    properties.add( getElementFromSnippet( "<prop name=\"prop1\">prop value</prop>" ) );
    when( connection.elements( "Property" ) ).thenReturn( properties );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( dataservicesConnectionInfo ) );
    Element otherConnection = mock( Element.class );
    DataservicesConnectionInfo differentDataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( differentDataservicesConnectionInfo ) );
    when( otherConnection.elements( "Property" ) ).thenReturn( properties );
    DataservicesConnectionInfo otherDataservicesConnectionInfo = new DataservicesConnectionInfo( otherConnection );
    assertEquals( 1, otherDataservicesConnectionInfo.getProperties().size() );
    assertEquals( "prop value", otherDataservicesConnectionInfo.getProperties().getProperty( "prop1" ) );
    assertTrue( dataservicesConnectionInfo.equals( otherDataservicesConnectionInfo ) );
    assertFalse( dataservicesConnectionInfo.equals( new Object() ) );
  }

  @Test
  public void testNotEquals() throws Exception {
    Element connection = mock( Element.class );
    ArrayList<Element> properties = new ArrayList<>( );
    properties.add( getElementFromSnippet( "<prop name=\"prop1\">prop value</prop>" ) );
    when( connection.elements( "Property" ) ).thenReturn( properties );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( dataservicesConnectionInfo ) );
    Element otherConnection = mock( Element.class );
    DataservicesConnectionInfo differentDataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( differentDataservicesConnectionInfo ) );
    ArrayList<Element> newProperties = new ArrayList<>( );
    when( otherConnection.elements( "Property" ) ).thenReturn( newProperties );
    DataservicesConnectionInfo otherDataservicesConnectionInfo = new DataservicesConnectionInfo( otherConnection );
    assertEquals( 0, otherDataservicesConnectionInfo.getProperties().size() );
    assertNull( otherDataservicesConnectionInfo.getProperties().getProperty( "prop1" ) );
    assertFalse( dataservicesConnectionInfo.equals( otherDataservicesConnectionInfo ) );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
