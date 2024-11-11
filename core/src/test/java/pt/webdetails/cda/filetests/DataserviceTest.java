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


package pt.webdetails.cda.filetests;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.DataservicesDataAccess;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DataserviceTest extends CdaTestCase {

  @Test
  public void testDatasourceDataservice() throws Exception {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    Connection connection = mock( Connection.class );
    Statement statement = mock( Statement.class );
    ResultSet resultSet = mock( ResultSet.class );
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );
    when( resultSet.next() ).thenReturn( true ).thenReturn( false );
    when( resultSet.getMetaData() ).thenReturn( resultSetMetaData );
    when( statement.executeQuery( any() ) ).thenReturn( resultSet );
    when( connection.createStatement( anyInt(), anyInt() ) ).thenReturn( statement );
    DriverConnectionProvider dataserviceLocalConnectionProvider = mock( DriverConnectionProvider.class );
    when( dataserviceLocalConnectionProvider.createConnection( any(), any() ) ).thenReturn( connection );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider( any() ) ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );

    final CdaSettings cdaSettings = spy( parseSettingsFile( "sample-dataservice.cda" ) );
    final QueryOptions queryOptions = new QueryOptions();
    String dataAccessId = "1";
    queryOptions.setDataAccessId( dataAccessId );

    CdaEngine engine = getEngine();
    engine.doQuery( cdaSettings, queryOptions );
    verify( dataserviceLocalConnectionProvider, atLeastOnce() ).createConnection( null, null );

    DataAccess dataAccess = cdaSettings.getDataAccess( dataAccessId );
    assertTrue( dataAccess instanceof DataservicesDataAccess );
    String queryActual = ( (DataservicesDataAccess) dataAccess ).getQuery();
    String queryExpected = "SELECT * FROM \"DataService from PDI\"";
    assertEquals( queryExpected, queryActual );
  }

  @Test
  public void testDatasourceDataserviceDriverConnectionProviderException() throws Exception {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider( any() ) ).thenThrow( new MalformedURLException( "wrong url" ) );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );

    final CdaSettings cdaSettings = spy( parseSettingsFile( "sample-dataservice.cda" ) );
    final QueryOptions queryOptions = new QueryOptions();
    String dataAccessId = "1";
    queryOptions.setDataAccessId( dataAccessId );

    CdaEngine engine = getEngine();
    try {
      engine.doQuery( cdaSettings, queryOptions );
      Assert.fail( "no exception" );
    } catch ( QueryException e ) {
      String msg = ExceptionUtils.getRootCauseMessage( e.getCause() );
      Assert.assertEquals( "MalformedURLException: wrong url", msg );
    }
  }

  @Test
  public void testDatasourceDataserviceCreateConnectionException() throws Exception {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    DriverConnectionProvider dataserviceLocalConnectionProvider = mock( DriverConnectionProvider.class );
    when( dataserviceLocalConnectionProvider.createConnection( any(), any() ) )
            .thenThrow( new SQLException( "couldn't create connection" ) );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider( any() ) ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );

    final CdaSettings cdaSettings = spy( parseSettingsFile( "sample-dataservice.cda" ) );
    final QueryOptions queryOptions = new QueryOptions();
    String dataAccessId = "1";
    queryOptions.setDataAccessId( dataAccessId );

    CdaEngine engine = getEngine();
    try {
      engine.doQuery( cdaSettings, queryOptions );
      Assert.fail( "no exception" );
    } catch ( QueryException e ) {
      String msg = ExceptionUtils.getRootCauseMessage( e.getCause() );
      Assert.assertEquals(
              "SQLException: couldn't create connection", msg );
    }
  }

  @Test
  public void testDatasourceDataserviceGetType() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( dataservicesConnection.getType(), "dataservices" );
  }
}
