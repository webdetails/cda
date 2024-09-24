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

import io.reactivex.Observer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinPreparedStatement;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinStatement;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DefaultParametrizationProviderFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ParametrizationProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ParametrizationProviderFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.dataaccess.streaming.IStreamingDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.streaming.SQLStreamingReportDataFactory;
import pt.webdetails.cda.xml.DomVisitor;

import java.sql.Statement;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a StreamingDataAccess that will get data from a Pentaho Data Service
 */
public class StreamingDataservicesDataAccess extends DataservicesDataAccess implements IStreamingDataAccess {

  private static final Log logger = LogFactory.getLog( StreamingDataservicesDataAccess.class );
  private static final DataAccessEnums.DataAccessInstanceType TYPE =
      DataAccessEnums.DataAccessInstanceType.STREAMING_DATASERVICES;
  protected String dataServiceName;
  protected String windowMode;
  protected long windowSize;
  protected long windowEvery;
  protected long windowLimit;

  public StreamingDataservicesDataAccess( final Element element ) {
    super( element );
    this.dataServiceName = element.selectSingleNode( "./StreamingDataServiceName" ).getText();
    this.windowMode = String.valueOf( element.selectSingleNode( "./WindowMode" ).getText() );
    this.windowSize = Integer.valueOf( element.selectSingleNode( "./WindowSize" ).getText() );
    this.windowEvery = Long.valueOf( element.selectSingleNode( "./WindowEvery" ).getText() );
    this.windowLimit = Long.valueOf( element.selectSingleNode( "./WindowLimit" ).getText() );
  }

  public String getDataServiceName() {
    return dataServiceName;
  }

  public String getWindowMode() {
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

  public StreamingDataservicesDataAccess() {
  }

  @Override
  public SQLReportDataFactory getSQLReportDataFactory( DataservicesConnection connection,
      ParameterDataRow parameterDataRow )
    throws InvalidConnectionException, UnknownConnectionException {
    IDataServiceClientService.StreamingMode mode =
        IDataServiceClientService.StreamingMode.ROW_BASED.toString().equalsIgnoreCase( windowMode )
            ? IDataServiceClientService.StreamingMode.ROW_BASED : IDataServiceClientService.StreamingMode.TIME_BASED;
    return new SQLStreamingReportDataFactory(
        connection.getInitializedConnectionProvider( parameterDataRow, CdaEngine.getEnvironment().getFormulaContext() ),
        mode, this.windowSize, this.windowEvery, this.windowLimit );
  }

  public String getType() {
    return TYPE.getType();
  }

  public String getLabel() {
    return TYPE.getLabel();
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(
      new PropertyDescriptor( "id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB ) );
    properties.add(
      new PropertyDescriptor( "access", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB ) );
    properties.add(
      new PropertyDescriptor( "parameters", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "output", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "columns", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "dataServiceQuery", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "connection", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB ) );
    properties.add(
      new PropertyDescriptor( "streamingDataServiceName", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
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

  public void accept( DomVisitor xmlVisitor, Element root ) {
    xmlVisitor.visit( (StreamingDataservicesDataAccess) this, root );
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.DATASERVICES;
  }

  @Override
  public void doPushStreamQuery( QueryOptions queryOptions, Observer<List<RowMetaAndData>> consumer ) throws QueryException {
    performStreamingRawQuery( queryOptions, consumer );
  }

  private void performStreamingRawQuery( QueryOptions queryOptions, Observer<List<RowMetaAndData>> consumer ) throws QueryException {
    try {
      final List<Parameter> parameters = getFilledParameters( queryOptions );

      logQueryStart( queryOptions, parameters );

      final ParameterDataRow parameterDataRow = Parameter.createParameterDataRowFromParameters( parameters );
      performStreamingRawQuery( parameterDataRow, consumer );
    } catch ( InvalidParameterException e ) {
      throw new QueryException( "Error parsing parameters ", e );
    }
  }

  protected void performStreamingRawQuery( ParameterDataRow parameterDataRow, Observer<List<RowMetaAndData>> consumer ) throws QueryException {
    String query = getQuery();
    try {
      DataservicesConnection conn = getConnection();
      PushStreamAndSQLReportDataFactory dataFactory = getDataFactory( conn, parameterDataRow );
      dataFactory.performPushQuery( query, parameterDataRow, consumer );
    } catch ( Exception e ) {
      throw new QueryException( e.getLocalizedMessage(), e );
    }
  }

  protected PushStreamAndSQLReportDataFactory getDataFactory( DataservicesConnection connection,
                                                                                   ParameterDataRow parameterDataRow )
    throws InvalidConnectionException {
    return new PushStreamAndSQLReportDataFactory(
      connection.getInitializedConnectionProvider( parameterDataRow, CdaEngine.getEnvironment().getFormulaContext() ),
      getStreamingParams() );
  }

  protected IDataServiceClientService.IStreamingParams getStreamingParams() {
    return new StreamingParams( windowMode, windowSize, windowEvery, windowLimit );
  }

  protected DataservicesConnection getConnection() throws UnknownConnectionException  {
    return (DataservicesConnection) getCdaSettings().getConnection( getConnectionId() );
  }

  protected static class StreamingParams implements IDataServiceClientService.IStreamingParams {

    private long windowSize, windowEvery, windowLimit;
    private IDataServiceClientService.StreamingMode windowMode;

    public StreamingParams( IDataServiceClientService.StreamingMode windowMode, long windowSize, long windowEvery, long windowLimit ) {
      this.windowMode = windowMode;
      this.windowEvery = windowEvery;
      this.windowLimit = windowLimit;
      this.windowSize = windowSize;
    }

    public StreamingParams( String windowMode, long windowSize, long windowEvery, long windowLimit ) {
      this.windowMode = IDataServiceClientService.StreamingMode.valueOf( windowMode );
      this.windowEvery = windowEvery;
      this.windowLimit = windowLimit;
      this.windowSize = windowSize;
    }

    @Override
    public IDataServiceClientService.StreamingMode getWindowMode() {
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
    private IDataServiceClientService.IStreamingParams streamingParams;

    public PushStreamAndSQLReportDataFactory( ConnectionProvider connectionProvider, IDataServiceClientService.IStreamingParams params ) {
      super( connectionProvider,
        params.getWindowMode(), params.getWindowSize(), params.getWindowEvery(), params.getWindowLimit() );
      this.streamingParams = params;
    }

    private IDataServiceClientService.IStreamingParams getStreamingParams() {
      return streamingParams;
    }

    private <T> T unwrap( Wrapper stmt, Class<T> wrapped ) throws Exception {
      if ( stmt.isWrapperFor( wrapped ) ) {
        return stmt.unwrap( wrapped );
      }
      throw new Exception( String.format( "Failed to unwrap '%s' from '$s'", wrapped, stmt ) );
    }

    public void performPushQuery( String query,
                                  ParameterDataRow parameters,
                                  Observer<List<RowMetaAndData>> consumer ) throws Exception {
      java.sql.Connection connection = getConnectionProvider().createConnection( null, null );
      ParametrizationProvider paramProvider = getParamProviderFactory().create( connection );
      String translatedQuery = paramProvider.rewriteQueryForParametrization( connection, query, parameters );
      String[] orderedNames = paramProvider.getPreparedParameterNames();
      if ( orderedNames.length > 0 ) {
        final java.sql.PreparedStatement pstmt = connection.prepareStatement( translatedQuery );
        for ( int i = 0; i < orderedNames.length; i++ ) {
          //prepared statement parameter index start with 1, then 2, ...
          pstmt.setObject( i + 1, parameters.get( orderedNames[ i ] ) );
        }
        IThinPreparedStatement thinStmt = unwrap( pstmt, IThinPreparedStatement.class );
        thinStmt.executePushQuery( getStreamingParams(), consumer );
      } else {
        Statement stmt = connection.createStatement();
        IThinStatement thinStmt = unwrap( stmt, IThinStatement.class );
        thinStmt.executePushQuery( query, getStreamingParams(), consumer );
      }
    }

    private ParametrizationProviderFactory getParamProviderFactory() {
      return new DefaultParametrizationProviderFactory();
    }
  }
}
