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

import io.reactivex.subjects.PublishSubject;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.DataservicesConnectionInfo;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitor;
import pt.webdetails.cpf.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;


public class StreamingDataservicesDataAccessTest extends TestCase {

  StreamingDataservicesDataAccess da;

  @Before
  public void setUp() {
    initBareEngine( getMockEnvironment() );
    da = new StreamingDataservicesDataAccess();
  }

  @Test
  public void testGetType() {
    assertEquals( da.getType(), "streaming" );
  }

  @Test
  public void testGetLabel() {
    assertEquals( da.getLabel(), "streaming" );
  }

  @Test
  public void testGetConnectionType() {
    assertEquals( da.getConnectionType(), ConnectionCatalog.ConnectionType.DATASERVICES );
  }

  @Test
  public void testConstructor() throws Exception {
    Element element = getElementFromSnippet("<element><DataServiceQuery>SELECT * FROM NOWHERE</DataServiceQuery>"
      + "<StreamingDataServiceName>DATA_SERVICE_NAME</StreamingDataServiceName>"
      + "<WindowMode>WINDOW_MODE</WindowMode>"
      + "<WindowSize>11111</WindowSize>"
      + "<WindowEvery>22222</WindowEvery>"
      + "<WindowLimit>33333</WindowLimit>"
      + "</element>");
    StreamingDataservicesDataAccess streamingDataservicesDataAccess = new StreamingDataservicesDataAccess(element);
    assertEquals( "SELECT * FROM NOWHERE", streamingDataservicesDataAccess.getQuery() );
    assertEquals( "DATA_SERVICE_NAME", streamingDataservicesDataAccess.getDataServiceName() );
    assertEquals( "WINDOW_MODE", streamingDataservicesDataAccess.getWindowMode() );
    assertEquals( 11111, streamingDataservicesDataAccess.getWindowSize() );
    assertEquals( 22222, streamingDataservicesDataAccess.getWindowEvery() );
    assertEquals( 33333, streamingDataservicesDataAccess.getWindowLimit() );
  }

  @Test
  public void testGetInterface() {
    List<PropertyDescriptor> daInterface = da.getInterface();

    List<PropertyDescriptor> dataServiceNameProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "streamingDataServiceName" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> queryProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "query" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> dataServiceQueryProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "dataServiceQuery" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> windowModeProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "windowMode" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> windowSizeProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "windowSize" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> windowEveryProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "windowEvery" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> windowLimitProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "windowLimit" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "cache" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheDurationProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "cacheDuration" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheKeysProperty = daInterface.stream()
      .filter( p -> p.getName().equals( "cacheKeys" ) ).collect( Collectors.toList() );

    assertEquals( 1, dataServiceNameProperty.size() );
    assertEquals( 1, dataServiceQueryProperty.size() );
    assertEquals( 1, windowModeProperty.size() );
    assertEquals( 1, windowSizeProperty.size() );
    assertEquals( 1, windowEveryProperty.size() );
    assertEquals( 1, windowLimitProperty.size() );
    assertEquals( 0, cacheProperty.size() );
    assertEquals( 0, cacheDurationProperty.size() );
    assertEquals( 0, cacheKeysProperty.size() );
    assertEquals( 0, queryProperty.size() );
  }

  @Test
  public void testSqlReportingDataFactoryType() throws Exception {
    DataservicesConnection mockConnection = mock( DataservicesConnection.class );
    ParameterDataRow parameterDataRow = mock( ParameterDataRow.class );
    ConnectionProvider mockConnectionProvider = mock( ConnectionProvider.class );
    doReturn( mockConnectionProvider ).when( mockConnection ).getInitializedConnectionProvider( any() );
    doReturn( mockConnectionProvider ).when( mockConnection ).getInitializedConnectionProvider( any(), any() );
    DataservicesConnectionInfo mockConnectionInfo = mock( DataservicesConnectionInfo.class );
    when( mockConnection.getConnectionInfo() ).thenReturn( mockConnectionInfo );
    final ParameterMapping[] parametersMapping = new ParameterMapping[ 1 ];
    parametersMapping[0] = new ParameterMapping( "param", "alias" );
    when( mockConnectionInfo.getDefinedVariableNames() ).thenReturn( parametersMapping );
    assertTrue( da.getSQLReportDataFactory( mockConnection, parameterDataRow ) instanceof SQLReportDataFactory );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }

  @Test
  public void testAccept() {
    DomVisitor visitor = mock( DomVisitor.class );
    Element element = mock( Element.class );
    da.accept( visitor, element );
    verify( visitor ).visit( da, element );
  }

  @Test
  public void testDoPushStreamQuery() throws Exception {
    QueryOptions queryOptions = mock( QueryOptions.class );
    PublishSubject<List<RowMetaAndData>> consumer = PublishSubject.create();
    IDataServiceClientService.IStreamingParams params = new StreamingDataservicesDataAccess.StreamingParams(
      IDataServiceClientService.StreamingMode.ROW_BASED,
      10,
      1,
      10000 );
    DataservicesConnection connection = mock( DataservicesConnection.class );

    StreamingDataservicesDataAccess streamingDataservicesDataAccess = spy( da );
    doReturn( connection ).when( streamingDataservicesDataAccess ).getConnection();
    doReturn( params ).when( streamingDataservicesDataAccess ).getStreamingParams();
    doReturn( new ArrayList() ).when( streamingDataservicesDataAccess ).getParameters();
    CdaSettings cdaSettings = mock( CdaSettings.class );
    doReturn( cdaSettings).when( streamingDataservicesDataAccess ).getCdaSettings();

    List<Parameter> parameters = streamingDataservicesDataAccess.getFilledParameters( queryOptions );
    ParameterDataRow parameterDataRow = Parameter.createParameterDataRowFromParameters( parameters );
    StreamingDataservicesDataAccess.PushStreamAndSQLReportDataFactory dataFactory = mock( StreamingDataservicesDataAccess.PushStreamAndSQLReportDataFactory.class );
    doReturn( dataFactory ).when( streamingDataservicesDataAccess ).getDataFactory( connection, parameterDataRow );

    streamingDataservicesDataAccess.doPushStreamQuery( queryOptions, consumer );
    verify( streamingDataservicesDataAccess ).doPushStreamQuery( queryOptions, consumer );
  }

  @Test
  public void testIStreamingParams(){
    IDataServiceClientService.IStreamingParams params = spy( new StreamingDataservicesDataAccess.StreamingParams(
      IDataServiceClientService.StreamingMode.ROW_BASED,
      10,
      1,
      1000 ) );

    assertEquals( 1, params.getWindowEvery() );
    assertEquals( 1000, params.getWindowLimit() );
    assertEquals( IDataServiceClientService.StreamingMode.ROW_BASED, params.getWindowMode() );
    assertEquals( 10, params.getWindowSize() );

    params = spy( new StreamingDataservicesDataAccess.StreamingParams(
      IDataServiceClientService.StreamingMode.ROW_BASED.toString(),
      20,
      2,
      2000 ) );

    assertEquals( 2, params.getWindowEvery() );
    assertEquals( 2000, params.getWindowLimit() );
    assertEquals( IDataServiceClientService.StreamingMode.ROW_BASED, params.getWindowMode() );
    assertEquals( 20, params.getWindowSize() );
  }
}
