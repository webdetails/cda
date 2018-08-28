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

package pt.webdetails.cda.dataaccess.streaming;

import java.sql.Statement;
import java.sql.Wrapper;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService.IStreamingParams;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService.StreamingMode;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinPreparedStatement;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinStatement;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DefaultParametrizationProviderFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ParametrizationProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ParametrizationProviderFactory;
import org.pentaho.reporting.engine.classic.core.states.datarow.EmptyTableModel;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.dataaccess.DataAccessEnums;
import pt.webdetails.cda.dataaccess.InvalidParameterException;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.SimpleDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;
import pt.webdetails.cda.utils.streaming.SQLStreamingReportDataFactory;

/**
 * Push-enabled version of a data access.
 */
public class PushStreamDataAccess extends SimpleDataAccess implements IStreamingDataAccess {

  private static final Log logger = LogFactory.getLog( PushStreamDataAccess.class );

  protected StreamingMode windowMode;
  protected long windowSize;
  protected long windowEvery;
  protected long windowLimit;

  public PushStreamDataAccess() {
  }

  public PushStreamDataAccess( final Element element ) {
    super( element );
    this.windowMode = parseNode( element, "./WindowMode", this::parseWindowMode, null );
    this.windowSize = parseNode( element, "./WindowSize", Long::valueOf, 10L );
    this.windowEvery = parseNode( element, "./WindowEvery", Long::valueOf, 10L );
    this.windowLimit = parseNode( element, "./WindowLimit", Long::valueOf, 60000L );
  }

  public StreamingMode getWindowMode() {
    return windowMode;
  }

  public long getWindowSize() {
    return windowSize;
  }

  public long getWindowEvery() {
    return windowEvery;
  }

  public long getWindowLimit() {
    return windowLimit;
  }

  @Override
  public IPushWindowQuery doPushStreamQuery( QueryOptions queryOptions ) throws QueryException {
    return performRawQuery( queryOptions );
  }


  @Override
  protected TableModel queryDataSource( QueryOptions queryOptions ) throws QueryException {
    IPushWindowQuery rawQueryExecution = performRawQuery( queryOptions );
    TableModel result = rawQueryExecution.getTableModel();
    rawQueryExecution.closeDataSource();
    return result;
  }

  @Override
  protected IPushWindowQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException {
    String query = getQuery();
    try {
      DataservicesConnection conn = getConnection();
      PushStreamAndSQLReportDataFactory dataFactory = getDataFactory( conn, parameterDataRow );
      Observable<List<RowMetaAndData>> rowWindowSource = dataFactory.performPushQuery( query, parameterDataRow );
      return new PushWindowQuery( convertToTableModelObservable( rowWindowSource ) );
    } catch ( Exception e ) {
      throw new QueryException( e.getLocalizedMessage(), e );
    }
  }

  private IPushWindowQuery performRawQuery( QueryOptions queryOptions ) throws QueryException {
    try {
      final List<Parameter> parameters = getFilledParameters( queryOptions );
      final ParameterDataRow parameterDataRow = Parameter.createParameterDataRowFromParameters( parameters );
      return performRawQuery( parameterDataRow );
    } catch ( InvalidParameterException e ) {
      throw new QueryException( "Error parsing parameters ", e );
    }
  }

  private DataservicesConnection getConnection() throws UnknownConnectionException  {
    return (DataservicesConnection) getCdaSettings().getConnection( getConnectionId() );
  }

  @Override
  public String getType() {
    return DataAccessEnums.DataAccessInstanceType.DATASERVICES_PUSH.getType();
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.DATASERVICES;
  }

  private static Observable<TableModel> convertToTableModelObservable( Observable<List<RowMetaAndData>> source ) {
    return source.map( RowMetaToTableModel.getConverter()::apply );
  }

  private static class DisposingObserver<T> implements Observer<T>, AutoCloseable {

    private Disposable source;

    @Override
    public void onSubscribe( Disposable d ) {
      source = d;
    }

    @Override
    public void onNext( T t ) {
    }

    @Override
    public void onError( Throwable e ) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void close() throws Exception {
      if ( source != null && !source.isDisposed() ) {
        source.dispose();
      }
    }
  }

  protected PushStreamAndSQLReportDataFactory getDataFactory( DataservicesConnection connection,
      ParameterDataRow parameterDataRow )
    throws InvalidConnectionException {
    return new PushStreamAndSQLReportDataFactory(
        connection.getInitializedConnectionProvider( parameterDataRow, CdaEngine.getEnvironment().getFormulaContext() ),
        getStreamingParams() );
  }

  private IStreamingParams getStreamingParams() {
    return new StreamingParams( windowMode, windowSize, windowEvery, windowLimit );
  }

  private static class StreamingParams implements IStreamingParams {

    private long windowSize, windowEvery, windowLimit;
    private StreamingMode windowMode;

    public StreamingParams( StreamingMode windowMode, long windowSize, long windowEvery, long windowLimit ) {
      this.windowMode = windowMode;
      this.windowEvery = windowEvery;
      this.windowLimit = windowLimit;
      this.windowSize = windowSize;
    }

    @Override
    public StreamingMode getWindowMode() {
      return windowMode;
    }

    @Override
    public long getWindowSize() {
      return windowSize;
    }

    @Override
    public long getWindowEvery() {
      return windowEvery;
    }

    @Override
    public long getWindowLimit() {
      return windowLimit;
    }
  }

  protected class PushStreamAndSQLReportDataFactory extends SQLStreamingReportDataFactory {
    private static final long serialVersionUID = 1L;
    private IStreamingParams streamingParams;

    public PushStreamAndSQLReportDataFactory( ConnectionProvider connectionProvider, IStreamingParams params ) {
      super( connectionProvider,
        params.getWindowMode(), params.getWindowSize(), params.getWindowEvery(), params.getWindowLimit() );
      this.streamingParams = params;
    }

    private IStreamingParams getStreamingParams() {
      return streamingParams;
    }

    private <T> T unwrap( Wrapper stmt, Class<T> wrapped ) throws Exception {
      if ( stmt.isWrapperFor( wrapped ) ) {
        return stmt.unwrap( wrapped );
      }
      throw new Exception( String.format( "Failed to unwrap '%s' from '$s'", wrapped, stmt ) );
    }

    public Observable<List<RowMetaAndData>> performPushQuery( String query, ParameterDataRow parameters ) throws Exception {
      java.sql.Connection connection = getConnectionProvider().createConnection( null, null );
      ParametrizationProvider paramProvider = getParamProviderFactory().create( connection );
      String translatedQuery = paramProvider.rewriteQueryForParametrization( connection, query, parameters );
      String[] orderedNames = paramProvider.getPreparedParameterNames();
      if ( orderedNames.length > 0 ) {
        final java.sql.PreparedStatement pstmt = connection.prepareStatement( translatedQuery );
        for ( int i = 0; i < orderedNames.length; i++ ) {
          pstmt.setObject( i, parameters.get( orderedNames[i] ) );
        }
        IThinPreparedStatement thinStmt = unwrap( pstmt, IThinPreparedStatement.class );
        return thinStmt.executePushQuery( getStreamingParams() );
      } else {
        Statement stmt = connection.createStatement();
        IThinStatement thinStmt = unwrap( stmt, IThinStatement.class );
        return thinStmt.executePushQuery( query, getStreamingParams() );
      }
    }

    private ParametrizationProviderFactory getParamProviderFactory() {
      return new DefaultParametrizationProviderFactory();
    }
  }

  private IDataServiceClientService.StreamingMode parseWindowMode( String windowMode ) {
    return StreamingMode.ROW_BASED.toString().equalsIgnoreCase( windowMode )
        ? StreamingMode.ROW_BASED
        : StreamingMode.TIME_BASED;
  }

  /**
   * 
   */
  private static class PushWindowQuery implements IPushWindowQuery {
    private Observable<TableModel> source;
    private AutoCloseable toClose;
    private TableModel tableModel;

    public PushWindowQuery( Observable<TableModel> observable ) {
      this.source = observable;
      if ( logger.isDebugEnabled() ) {
        source = source
            .doOnDispose( () -> logger.debug( "Disposed StreamWindowQuery" ) )
            .doOnComplete( () -> logger.debug( " Completed StreamWindowQuery" ) );
        if ( logger.isTraceEnabled() ) {
          source = source.doOnNext( tm -> logger.trace( "Streamed TableModel, " + tm.getRowCount() + " rows" )  );
        }
      }
      DisposingObserver<TableModel> closer = new DisposingObserver<>();
      this.toClose = closer;
      this.source.subscribe( closer );
    }

    @Override
    public TableModel getTableModel() {
      try {
        if ( tableModel == null ) {
          tableModel = source.blockingFirst();
        }
      } catch ( NoSuchElementException e ) {
        // finished without items
        logger.warn( e );
        tableModel = new EmptyTableModel();
      }
      return tableModel;
    }

    @Override
    public void closeDataSource() throws QueryException {
      try {
        toClose.close();
      } catch ( Exception e ) {
        logger.error( e );
      }
    }

    @Override
    public ObservableSource<TableModel> getTableSource() {
      return source;
    }
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    List<PropertyDescriptor> properties = super.getInterface();
    properties.add(
      new PropertyDescriptor( "dataServiceName", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "windowMode", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "windowSize", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "windowEvery", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "windowLimit", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }


}
