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

package pt.webdetails.cda.xml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.DataservicesConnectionInfo;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.dataaccess.DataAccessEnums;
import pt.webdetails.cda.dataaccess.DataservicesDataAccess;
import pt.webdetails.cda.dataaccess.StreamingDataservicesDataAccess;
import pt.webdetails.cda.filetests.CdaTestEnvironment;
import pt.webdetails.cda.filetests.CdaTestingContentAccessFactory;
import pt.webdetails.cpf.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class DomVisitorTest {

  private DomVisitor domVisitor;

  @Before
  public void setUp() throws Exception {
    this.domVisitor = new DomVisitor();

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
    when( dataserviceLocalConnection.getDriverConnectionProvider( any() ) ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );
  }

  @Test
  public void testVisitStreamingDataServices() throws Exception {
    StreamingDataservicesDataAccess streamingDataservicesDataAccess = mock( StreamingDataservicesDataAccess.class );
    Element element = getElementFromSnippet( "<emptyElement/>" );
    when( streamingDataservicesDataAccess.getAccess() ).thenReturn( DataAccessEnums.ACCESS_TYPE.PUBLIC );
    when( streamingDataservicesDataAccess.isCacheEnabled() ).thenReturn( true );
    when( streamingDataservicesDataAccess.getName() ).thenReturn( "name" );
    when( streamingDataservicesDataAccess.getQuery() ).thenReturn( "select * from dummy" );
    when( streamingDataservicesDataAccess.getDataServiceName() ).thenReturn( "dummy" );
    when( streamingDataservicesDataAccess.getWindowMode() ).thenReturn( "ROW_BASED" );
    when( streamingDataservicesDataAccess.getWindowSize() ).thenReturn( 0l );
    when( streamingDataservicesDataAccess.getWindowEvery() ).thenReturn( 0l );
    when( streamingDataservicesDataAccess.getWindowLimit() ).thenReturn( 0L );
    domVisitor.visit( streamingDataservicesDataAccess, element );

    assertNull( element.element( "Query" ) );
    assertNull( element.element( "DataServiceName" ) );
    assertEquals( "dummy", element.element( "StreamingDataServiceName" ).getText() );
    assertEquals( "select * from dummy", element.element( "DataServiceQuery" ).getText() );
    assertEquals( "ROW_BASED", element.element( "WindowMode" ).getText() );
    assertEquals( "0", element.element( "WindowSize" ).getText() );
    assertEquals( "0", element.element( "WindowEvery" ).getText() );
    assertEquals( "0", element.element( "WindowLimit" ).getText() );
  }

  @Test
  public void testVisitDataServices() throws Exception {
    DataservicesDataAccess dataservicesDataAccess = mock( DataservicesDataAccess.class );
    Element element = getElementFromSnippet( "<emptyElement/>" );
    when( dataservicesDataAccess.getAccess() ).thenReturn( DataAccessEnums.ACCESS_TYPE.PUBLIC );
    when( dataservicesDataAccess.isCacheEnabled() ).thenReturn( true );
    when( dataservicesDataAccess.getName() ).thenReturn( "name" );
    when( dataservicesDataAccess.getQuery() ).thenReturn( "select * from dummy" );
    when( dataservicesDataAccess.getDataServiceName() ).thenReturn( "dummy" );
    domVisitor.visit( dataservicesDataAccess, element );

    assertNull( element.element( "Query" ) );
    assertNull( element.element( "StreamingDataServiceName" ) );
    assertEquals( "dummy", element.element( "DataServiceName" ).getText() );
    assertEquals( "select * from dummy", element.element( "DataServiceQuery" ).getText() );
  }

  @Test
  public void testVisitDataServicesConnection() throws Exception {
    Element element = getElementFromSnippet( "<emptyElement/>" );
    DataservicesConnection connection = mock( DataservicesConnection.class );
    when( connection.getId() ).thenReturn( "id" );
    when( connection.getTypeForFile() ).thenReturn( "type" );

    ConnectionProvider mockConnectionProvider = mock( ConnectionProvider.class );
    doReturn( mockConnectionProvider ).when( connection ).getInitializedConnectionProvider( any() );
    DataservicesConnectionInfo mockConnectionInfo = mock( DataservicesConnectionInfo.class );
    when( connection.getConnectionInfo() ).thenReturn( mockConnectionInfo );
    final ParameterMapping[] parametersMapping = new ParameterMapping[ 1 ];
    parametersMapping[0] = new ParameterMapping( "param", "alias" );
    when( mockConnectionInfo.getDefinedVariableNames() ).thenReturn( parametersMapping );

    domVisitor.visit( connection, element );

    assertEquals( "id", element.element( "Connection" ).attribute( "id" ).getText() );
    assertEquals( "type", element.element( "Connection" ).attribute( "type" ).getText() );
  }

  @Test
  public void testNvl() {
    assertNotNull( domVisitor.nvl( null ) );
    assertEquals( "", domVisitor.nvl( null ) );
    assertEquals( "", domVisitor.nvl( "" ) );
    assertEquals( "not empty", domVisitor.nvl( "not empty" ) );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
