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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.filetests.CdaTestEnvironment;
import pt.webdetails.cda.filetests.CdaTestingContentAccessFactory;
import pt.webdetails.cpf.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DataservicesConnectionTest {
  @Before
  public void setUp() throws Exception {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    Connection connection = mock( Connection.class );
    Statement statement = mock( Statement.class );
    ResultSet resultSet = mock( ResultSet.class );
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );
    when( resultSet.next() ).thenReturn( true ).thenReturn( false );
    when( resultSet.getMetaData() ).thenReturn( resultSetMetaData );
    when( statement.executeQuery( anyString() ) ).thenReturn( resultSet );
    when( connection.createStatement( Mockito.anyInt(), Mockito.anyInt() ) ).thenReturn( statement );
    DriverConnectionProvider dataserviceLocalConnectionProvider = mock( DriverConnectionProvider.class );
    when( dataserviceLocalConnectionProvider.createConnection( anyString(), anyString() ) ).thenReturn( connection );
    Properties properties = new Properties();
    when( dataserviceLocalConnectionProvider.getProperty( anyString() ) ).thenAnswer(
      invocationOnMock -> properties.getProperty( (String)invocationOnMock.getArguments()[0] )
    );
    when( dataserviceLocalConnectionProvider.setProperty( anyString(), anyString() ) ).thenAnswer(
      invocationOnMock -> properties.setProperty( (String)invocationOnMock.getArguments()[0], (String)invocationOnMock.getArguments()[1] )
    );
    when( dataserviceLocalConnectionProvider.getPropertyNames() ).thenReturn( properties.keySet().toArray( new String[0] ) );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider( anyMap() ) ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );
  }

  @Test
  public void testHashCode() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( 0, dataservicesConnection.hashCode() );
  }

  @Test
  public void testGetProperties() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertNotNull( dataservicesConnection.getProperties() );
    assertEquals( 1, dataservicesConnection.getProperties().size() );
  }

  @Test
  public void testGetTypeForFile() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertNotNull( dataservicesConnection.getTypeForFile() );
    assertEquals( "dataservices.dataservices", dataservicesConnection.getTypeForFile() );
  }

  @Test
  public void testGenericType() {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( ConnectionCatalog.ConnectionType.DATASERVICES, dataservicesConnection.getGenericType() );
  }

  @Test
  public void testGetType() {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( DataservicesConnection.TYPE, dataservicesConnection.getType() );
  }

  @Test
  public void testNewWithParameters() throws Exception {
    Element connection = mock( Element.class );
    when( connection.selectObject( anyString() ) ).thenReturn( "" );
    DataservicesConnection dataservicesConnection = new DataservicesConnection( connection );
    assertNotNull( dataservicesConnection );
  }

  @Test
  public void testGetInitializedConnectionProviderWithProperties() throws Exception {
    Element connectionElement = mock( Element.class );
    when( connectionElement.selectObject( anyString() ) ).thenReturn( "" );
    ArrayList<Element> properties = new ArrayList<>( );
    properties.add( getElementFromSnippet( "<prop name=\"prop1\">will return mock value on test setup</prop>" ) );
    when( connectionElement.elements( "Property" ) ).thenReturn( properties );
    ArrayList<Element> variables = new ArrayList<>( );
    variables.add( getElementFromSnippet( "<e datarow-name=\"param1\" variable-name=\"var1\"></e>" ) );
    when( connectionElement.elements( "variables" ) ).thenReturn( variables );
    DataservicesConnection dataservicesConnection = new DataservicesConnection( connectionElement );
    Map<String, String> dataserviceParameters = new TreeMap<>( );
    DriverConnectionProvider connectionProvider = (DriverConnectionProvider) dataservicesConnection.getInitializedConnectionProvider( dataserviceParameters );
    assertNotNull( connectionProvider );
    assertNotNull( connectionProvider.getProperty( "prop1" ) );
    assertEquals( "will return mock value on test setup", connectionProvider.getProperty( "prop1" ) );
    connectionProvider.getUrl();
  }

  @Test
  public void testGetInitializedConnectionProvider() throws Exception {
    Element connectionElement = mock( Element.class );
    when( connectionElement.selectObject( anyString() ) ).thenReturn( "" );
    DataservicesConnection dataservicesConnection = new DataservicesConnection( connectionElement );
    Map<String, String> dataserviceParameters = new TreeMap<>( );
    ConnectionProvider connectionProvider = dataservicesConnection.getInitializedConnectionProvider( dataserviceParameters );
    assertNotNull( connectionProvider );
  }

  @Test
  public void testEquals() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertTrue( dataservicesConnection.equals( dataservicesConnection ) );

    DataservicesConnection differentDataservicesConnection = new DataservicesConnection();
    assertTrue( dataservicesConnection.equals( differentDataservicesConnection ) );
    assertFalse( dataservicesConnection.equals( new Object() ) );
    assertFalse( dataservicesConnection.equals( null ) );

    Element connection = mock( Element.class );
    when( connection.selectObject( anyString() ) ).thenReturn( "" );
    differentDataservicesConnection.initializeConnection( connection );
    assertFalse( differentDataservicesConnection.equals( dataservicesConnection ) );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
