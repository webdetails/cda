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
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.filetests.CdaTestEnvironment;
import pt.webdetails.cda.filetests.CdaTestingContentAccessFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DataservicesConnectionTest {

  @Test
  public void testHashCode() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( 0, dataservicesConnection.hashCode() );
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
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnection dataservicesConnection = new DataservicesConnection( connection );
    assertNotNull( dataservicesConnection );
  }

  @Test
  public void testGetInitializedConnectionProvider() throws Exception {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    Connection connection = mock( Connection.class );
    Statement statement = mock( Statement.class );
    ResultSet resultSet = mock( ResultSet.class );
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );
    when( resultSet.next() ).thenReturn( true ).thenReturn( false );
    when( resultSet.getMetaData() ).thenReturn( resultSetMetaData );
    when( statement.executeQuery( Mockito.anyString() ) ).thenReturn( resultSet );
    when( connection.createStatement( Mockito.anyInt(), Mockito.anyInt() ) ).thenReturn( statement );
    DriverConnectionProvider dataserviceLocalConnectionProvider = mock( DriverConnectionProvider.class );
    when( dataserviceLocalConnectionProvider.createConnection( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( connection );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider() ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );

    Element connectionElement = mock( Element.class );
    when( connectionElement.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    DataservicesConnection dataservicesConnection = new DataservicesConnection( connectionElement );
    ConnectionProvider connectionProvider = dataservicesConnection.getInitializedConnectionProvider();
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
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    differentDataservicesConnection.initializeConnection( connection );
    assertFalse( differentDataservicesConnection.equals( dataservicesConnection ) );
  }
}
