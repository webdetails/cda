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


package pt.webdetails.cda.dataaccess;

import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.DataservicesConnectionInfo;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.filetests.CdaTestEnvironment;
import pt.webdetails.cda.filetests.CdaTestingContentAccessFactory;
import pt.webdetails.cpf.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

public class DataservicesDataAccessTest extends TestCase {

  DataservicesDataAccess da;

  @Before
  public void setUp() {
    initBareEngine( getMockEnvironment() );
    da = new DataservicesDataAccess();
  }

  @Test
  public void testGetType() {
    assertEquals( da.getType(), "dataservices" );
  }

  @Test
  public void testGetLabel() {
    assertEquals( da.getLabel(), "sql" );
  }

  @Test
  public void testGetConnectionType() {
    assertEquals( da.getConnectionType(), ConnectionCatalog.ConnectionType.DATASERVICES );
  }

  @Test
  public void testGetInterface() {
    List<PropertyDescriptor> daInterface = da.getInterface();

    List<PropertyDescriptor> dataServiceNameProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "dataServiceName" ) ).collect( Collectors.toList() );
    assertEquals( 1, dataServiceNameProperty.size() );

    List<PropertyDescriptor> dataServiceQueryProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "dataServiceQuery" ) ).collect( Collectors.toList() );
    assertEquals( 1, dataServiceQueryProperty.size() );

    List<PropertyDescriptor> queryNameProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "query" ) ).collect( Collectors.toList() );
    assertEquals( 0, queryNameProperty.size() );
  }

  @Test
  public void testSetDataServiceQuery() {
    assertNull( da.getQuery() );
    da.setDataServiceQuery( "QUERY" );
    assertEquals( "QUERY", da.getQuery() );
  }

  @Test
  public void testGetDataServiceName() throws Exception {
    DataservicesDataAccess daServiceNameTest = new DataservicesDataAccess( getElementFromSnippet( "<DataAccess><DataServiceName>dataServiceName</DataServiceName><DataServiceQuery>query</DataServiceQuery></DataAccess>" ) );
    assertEquals( "dataServiceName", daServiceNameTest.getDataServiceName() );
  }

  @Test
  public void testGetSQLReportDataFactory() throws Exception {
    DataservicesConnection dataservicesConnection = mock( DataservicesConnection.class );
    ConnectionProvider connectionProvider = mock( ConnectionProvider.class );
    when( dataservicesConnection.getInitializedConnectionProvider( any() ) ).thenReturn( connectionProvider );
    when( dataservicesConnection.getInitializedConnectionProvider( any(), any() ) ).thenReturn( connectionProvider );

    DataservicesConnectionInfo connectionInfo = mock( DataservicesConnectionInfo.class );
    final ParameterMapping[] parametersMapping = new ParameterMapping[ 1 ];
    parametersMapping[0] = new ParameterMapping( "param", "alias" );
    when( connectionInfo.getDefinedVariableNames() ).thenReturn( parametersMapping );
    when( dataservicesConnection.getConnectionInfo() ).thenReturn( connectionInfo );

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
    when( dataserviceLocalConnectionProvider.getProperty( anyString() ) ).thenReturn( "value" );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider( any() ) ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    ParameterDataRow parameterDataRow = mock( ParameterDataRow.class );
    CdaEngine.init( testEnvironment );

    SQLReportDataFactory sqlReportDataFactory = da.getSQLReportDataFactory( dataservicesConnection, parameterDataRow );
    assertNotNull( sqlReportDataFactory );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
