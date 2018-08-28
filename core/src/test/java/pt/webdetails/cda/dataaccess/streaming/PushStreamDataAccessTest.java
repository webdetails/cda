package pt.webdetails.cda.dataaccess.streaming;

import static org.junit.Assert.*;
import static pt.webdetails.cda.test.util.CdaTestHelper.getElementFromSnippet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.table.TableModel;

import static org.mockito.Mockito.*;

import org.dom4j.DocumentException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService.IStreamingParams;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService.StreamingMode;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinPreparedStatement;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinStatement;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.streaming.IStreamingDataAccess.IPushWindowQuery;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.CdaTestHelper;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cda.test.util.TableModelChecker;

public class PushStreamDataAccessTest {

  private ICdaEnvironment env;
  private IDataservicesLocalConnection loco;
  private Connection sqlConn;

  @Before
  public void setUp() throws Exception {
    CdaTestHelper.initBareEngine( getMockEnvironment() );
  }

  @Test
  public void testParse() throws Exception {
    PushStreamDataAccess dataAccess = new PushStreamDataAccess( getElementFromSnippet(
      "<DataAccess id=\"pushy\" connection=\"conn\">"
      + "   <Name>A Push Stream Data Access</Name>"
      + "   <DataServiceName>dataServiceName</DataServiceName>"
      + "   <Query>query</Query>"
      + "   <WindowMode>TIME_BASED</WindowMode>"
      + "   <WindowSize>330</WindowSize>"
      + "   <WindowEvery>220</WindowEvery>"
      + "</DataAccess>" ) );
    assertEquals( StreamingMode.TIME_BASED, dataAccess.getWindowMode() );
    assertEquals( 330, dataAccess.getWindowSize() );
    assertEquals( 220, dataAccess.getWindowEvery() );
    assertEquals( 60000, dataAccess.getWindowLimit() );
    assertEquals( "conn", dataAccess.getConnectionId() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testDoPushStreamQuery() throws Exception {
    PushStreamDataAccess dataAccess = setUpBasicDataAccess();

    RowMeta header = new RowMeta();
    header.addValueMeta( new ValueMetaString( "str" ) );
    header.addValueMeta( new ValueMetaInteger( "int" ) );
    setUpPushStatement( Observable.fromArray(
      Arrays.asList(
        new RowMetaAndData( header, "a", 1 ),
        new RowMetaAndData( header, "b", 2 ) ),
      Arrays.asList(
        new RowMetaAndData( header, "x", 9 ),
        new RowMetaAndData( header, "y", 8 ) ) ) );

    IPushWindowQuery pushQuery = dataAccess.doPushStreamQuery( new QueryOptions() );
    ObservableSource<TableModel> source = pushQuery.getTableSource();

    Observer<TableModel> obs = mock( Observer.class );
    source.subscribe( obs );

    verify( obs ).onSubscribe( any() );
    ArgumentCaptor<TableModel> captor = ArgumentCaptor.forClass( TableModel.class );
    verify( obs, times( 2 ) ).onNext( captor.capture() );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals(
      new SimpleTableModel(
        new Object[] { "a", 1 },
        new Object[] { "b", 2 } ),
      captor.getAllValues().get( 0 ) );
    checker.assertEquals(
      new SimpleTableModel(
        new Object[] { "x", 9 },
        new Object[] { "y", 8 } ),
      captor.getAllValues().get( 1 ) );
    verify( obs ).onComplete();
  }

  @Test
  public void testDoPushStreamQueryParamRequest() throws Exception {
    PushStreamDataAccess dataAccess = new PushStreamDataAccess( getElementFromSnippet(
      "<DataAccess connection=\"conn\">"
      + "<WindowMode>ROW_BASED</WindowMode>"
      + "<WindowSize>10</WindowSize>"
      + "<WindowEvery>7</WindowEvery>"
      + "<WindowLimit>1000</WindowLimit>"
      + "<Query>select a from b where c=${param1} and d=${param2}</Query>"
      + "<Parameters>"
      + "<Parameter name=\"param2\" type=\"String\" default=\"\"/>"
      + "<Parameter name=\"param1\" type=\"String\" default=\"\"/>"
      + "</Parameters>"
      + "</DataAccess>" ) );

    setUpDataAccess( dataAccess );

    QueryOptions opts = new QueryOptions();
    opts.addParameter( "param2", "value2" );
    opts.addParameter( "param1", "value1" );
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaInteger() );
    IThinPreparedStatement stmt =
        setUpPushPreparedStatement( Observable.fromCallable( () -> Arrays.asList( new RowMetaAndData( rm, 0 ) ) ) );

    dataAccess.doPushStreamQuery( opts );
    verify( sqlConn ).prepareStatement( eq( "select a from b where c=? and d=?" ) );
    verify( stmt ).executePushQuery( argThat(
      new BaseMatcher<IStreamingParams>() {
        @Override
        public boolean matches( Object item ) {
          IStreamingParams params = (IStreamingParams) item;
          return params.getWindowMode().equals( StreamingMode.ROW_BASED )
              && params.getWindowSize() == 10
              && params.getWindowEvery() == 7
              && params.getWindowLimit() == 1000;
        }

        @Override
        public void describeTo( Description description ) {
        }
      }
      ) );
    verify( stmt ).setObject( eq( 0 ), eq( "value1" ) );
    verify( stmt ).setObject( eq( 1 ), eq( "value2" ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testDoQuery() throws Exception {
    PushStreamDataAccess dataAccess = setUpBasicDataAccess();
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaInteger() );
    setUpPushStatement( Observable.fromArray( Arrays.asList( new RowMetaAndData( rm, 0 ) ) ) );
    TableModel table = dataAccess.doQuery( new QueryOptions() );
    assertNotNull( table );
    assertEquals( 0, table.getValueAt( 0, 0 ) );
  }

  @Test
  public void testDoQueryEmpty() throws Exception {
    PushStreamDataAccess dataAccess = setUpBasicDataAccess();
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaInteger() );
    setUpPushStatement( Observable.empty() );
    TableModel table = dataAccess.doQuery( new QueryOptions() );
    assertNotNull( table );
  }

  @Test
  public void testCloseDataSourceDisposes() throws Exception {
    PushStreamDataAccess dataAccess = setUpBasicDataAccess();
    AtomicReference<Boolean> isDisposed = new AtomicReference<Boolean>( false );
    setUpPushStatement( Observable.<List<RowMetaAndData>>never().doOnDispose( () -> isDisposed.set( true ) ) );
    IPushWindowQuery pushQuery = dataAccess.doPushStreamQuery( new QueryOptions() );
    pushQuery.closeDataSource();
    assertTrue( isDisposed.get() );
  }

  @Test( expected = QueryException.class )
  public void testConnectionUnwrapFail() throws Exception {
    PushStreamDataAccess dataAccess = setUpBasicDataAccess();
    IThinStatement stmt = setUpPushStatement( Observable.empty() );
    when( stmt.isWrapperFor( anyObject() ) ).thenReturn( false );
    dataAccess.doPushStreamQuery( new QueryOptions() );
  }

  @Test
  public void testGetInterface() throws Exception {
    AbstractDataAccess dataAccess = setUpBasicDataAccess();
    List<PropertyDescriptor> properties = dataAccess.getInterface();
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "query" ) ) );
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "connection" ) ) );
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "windowMode" ) ) );
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "windowSize" ) ) );
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "windowEvery" ) ) );
    assertTrue( properties.stream().anyMatch( p -> p.getName().equals( "windowLimit" ) ) );
  }

  private ICdaEnvironment getMockEnvironment() throws Exception {
    env = CdaTestHelper.getMockEnvironment();
    loco = mock( IDataservicesLocalConnection.class );
    sqlConn = mock( Connection.class );
    DriverConnectionProvider prov = new DriverConnectionProvider() {
      private static final long serialVersionUID = 1L;
      @Override
      public Connection createConnection( String user, String password ) throws SQLException {
        return sqlConn;
      }
    };
    when( loco.getDriverConnectionProvider( any() ) ).thenReturn( prov );
    when( env.getDataServicesLocalConnection() ).thenReturn( loco );
    return env;
  }

  private IThinStatement setUpPushStatement( Observable<List<RowMetaAndData>> source ) throws Exception {
    IThinStatement stmt = mock( IThinStatement.class );
    when( stmt.isWrapperFor( IThinStatement.class ) ).thenReturn( true );
    when( stmt.unwrap( IThinStatement.class ) ).thenReturn( stmt );
    when ( stmt.executePushQuery( anyString(), any( IStreamingParams.class ) ) ).thenReturn( source );
    when( sqlConn.createStatement() ).thenReturn( stmt );
    return stmt;
  }

  private IThinPreparedStatement setUpPushPreparedStatement( Observable<List<RowMetaAndData>> source ) throws Exception {
    IThinPreparedStatement stmt = mock( IThinPreparedStatement.class );
    when( stmt.isWrapperFor( IThinPreparedStatement.class ) ).thenReturn( true );
    when( stmt.unwrap( IThinPreparedStatement.class ) ).thenReturn( stmt );
    when ( stmt.executePushQuery( any( IStreamingParams.class ) ) ).thenReturn( source );
    when( sqlConn.createStatement() ).thenReturn( stmt );
    when( sqlConn.prepareStatement( anyString() ) ).thenReturn( stmt );
    return stmt;
  }

  private PushStreamDataAccess setUpBasicDataAccess() throws DocumentException, InvalidConnectionException {
    PushStreamDataAccess dataAccess = new PushStreamDataAccess( getElementFromSnippet(
      "<DataAccess connection=\"conn\">"
      + "<Query>query</Query>"
      + "</DataAccess>" ) );
    setUpDataAccess( dataAccess );
    return dataAccess;
  }

  private void setUpDataAccess( PushStreamDataAccess dataAccess ) throws InvalidConnectionException, DocumentException  {
    ResourceKey rk = new ResourceKey( "schema", "test.cda", null );
    CdaSettings settings = new CdaSettings( "test.cda", rk );
    settings.addConnection( new DataservicesConnection( getElementFromSnippet(
      String.format( "<Connection id=\"%s\" type=\"dataservices.dataservices\"/>", dataAccess.getConnectionId() ) ) ) );
    settings.addDataAccess( dataAccess );
  }
}
