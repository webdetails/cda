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


package pt.webdetails.cda.connections.dataservices;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import pt.webdetails.cda.filetests.CdaTestCase;
import pt.webdetails.cpf.Util;

import java.util.ArrayList;
import java.util.Arrays;

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
    ArrayList<Element> variables = new ArrayList<>( );
    variables.add( getElementFromSnippet( "<e datarow-name=\"param1\" variable-name=\"var1\"></e>" ) );
    when( connection.elements( "variables" ) ).thenReturn( variables );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertNotNull( dataservicesConnectionInfo.getProperties() );
    assertEquals( 2, dataservicesConnectionInfo.getProperties().size() );
    assertEquals( 1, dataservicesConnectionInfo.getDefinedVariableNames().length );
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
    ArrayList<Element> variables = new ArrayList<>( );
    variables.add( getElementFromSnippet( "<e datarow-name=\"param1\" variable-name=\"var1\"></e>" ) );
    when( connection.elements( "variables" ) ).thenReturn( variables );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertEquals( ( 31 * dataservicesConnectionInfo.getProperties().hashCode() ) +
      Arrays.deepHashCode( parameterMappingToStringArray( dataservicesConnectionInfo.getDefinedVariableNames() ) ),
      dataservicesConnectionInfo.hashCode() );
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
    ArrayList<Element> variables = new ArrayList<>( );
    variables.add( getElementFromSnippet( "<e datarow-name=\"param1\" variable-name=\"var1\"></e>" ) );
    when( connection.elements( "variables" ) ).thenReturn( variables );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnectionInfo dataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( dataservicesConnectionInfo ) );
    Element otherConnection = mock( Element.class );
    DataservicesConnectionInfo differentDataservicesConnectionInfo = new DataservicesConnectionInfo( connection );
    assertTrue( dataservicesConnectionInfo.equals( differentDataservicesConnectionInfo ) );
    when( otherConnection.elements( "Property" ) ).thenReturn( properties );
    DataservicesConnectionInfo otherDataservicesConnectionInfo = new DataservicesConnectionInfo( otherConnection );
    assertEquals( 1, otherDataservicesConnectionInfo.getProperties().size() );
    assertEquals( 0, otherDataservicesConnectionInfo.getDefinedVariableNames().length );
    assertEquals( 1, dataservicesConnectionInfo.getDefinedVariableNames().length );
    assertEquals( "prop value", otherDataservicesConnectionInfo.getProperties().getProperty( "prop1" ) );
    assertEquals( "param1", dataservicesConnectionInfo.getDefinedVariableNames()[0].getName() );
    assertEquals( "var1", dataservicesConnectionInfo.getDefinedVariableNames()[0].getAlias() );
    assertFalse( dataservicesConnectionInfo.equals( otherDataservicesConnectionInfo ) );
    when( otherConnection.elements( "variables" ) ).thenReturn( variables );
    DataservicesConnectionInfo otherEqualsDataservicesConnectionInfo = new DataservicesConnectionInfo( otherConnection );
    assertTrue( dataservicesConnectionInfo.equals( otherEqualsDataservicesConnectionInfo ) );
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

  private String[][] parameterMappingToStringArray( ParameterMapping[] paramMaps ) {
    if ( paramMaps == null ) {
      return null;
    }
    String[][] result = new String[ paramMaps.length ][];
    for ( int i = 0; i < paramMaps.length; i++ ) {
      String[] item = new String[] { paramMaps[ i ].getName(), paramMaps[ i ].getAlias() };
      result[ i ] = item;
    }
    return result;
  }
}
