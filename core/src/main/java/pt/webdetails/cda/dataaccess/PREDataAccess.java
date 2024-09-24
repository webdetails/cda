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

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.cache.CachingDataFactory;
import org.pentaho.reporting.engine.classic.core.parameters.CompoundDataRow;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

import javax.swing.table.TableModel;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This is the DataAccess implementation for PentahoReportingEngine based queries.
 */
public abstract class PREDataAccess extends SimpleDataAccess {

  //  private static final Log logger = LogFactory.getLog(PREDataAccess.class);

  public PREDataAccess() {
  }


  public PREDataAccess( final Element element ) {
    super( element );
  }


  /**
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public PREDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name, connectionId, query );
  }

  public abstract DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException;

  public DataFactory getDataFactory( final ParameterDataRow parameterDataRow ) throws UnknownConnectionException, InvalidConnectionException {
    return getDataFactory();
  }

  protected static class PREDataSourceQuery implements IDataSourceQuery {

    private TableModel tableModel;
    private CachingDataFactory localDataFactory;

    public PREDataSourceQuery( TableModel tm, CachingDataFactory df ) {
      this.tableModel = tm;
      this.localDataFactory = df;
    }

    @Override
    public TableModel getTableModel() {
      return tableModel;
    }

    @Override
    public void closeDataSource() throws QueryException {
      if ( localDataFactory == null ) {
        return;
      }

      // and at the end, close your table model if it holds on to resources like a resultset
      if ( getTableModel() instanceof CloseableTableModel ) {
        final CloseableTableModel ctm = (CloseableTableModel) getTableModel();
        ctm.close();
      }

      // and finally shut down the data factory to free any connection that may be open.
      localDataFactory.close();

      localDataFactory = null;
    }

  }


  @Override
  protected IDataSourceQuery performRawQuery( final ParameterDataRow parameterDataRow ) throws QueryException {
    try {
      final CachingDataFactory dataFactory = new CachingDataFactory( getDataFactory( parameterDataRow ), false );

      final Configuration configuration = ClassicEngineBoot.getInstance().getGlobalConfig();

      initializeDataFactory( dataFactory, configuration );

      // fire the query. you always get a table model or an exception.

      IDataAccessUtils dataAccessUtils = CdaEngine.getEnvironment().getDataAccessUtils();
      final ReportEnvironmentDataRow environmentDataRow = dataAccessUtils.createEnvironmentDataRow( configuration );


      PREDataSourceQuery queryExecution = null;
      try {
        final CompoundDataRow compoundDataRow = new CompoundDataRow( environmentDataRow, parameterDataRow );
        final TableModel tableModel = dataFactory.queryData( "query", compoundDataRow );

        //  Store this variable so that we can close it later
        queryExecution = new PREDataSourceQuery( tableModel, dataFactory );
      } finally {

        // There was an exception while getting the dataset - need to make sure
        // that the dataFactory is closed
        if ( queryExecution == null ) {
          dataFactory.close();
        }
      }

      return queryExecution;

    } catch ( UnknownConnectionException | InvalidConnectionException e ) {
      throw new QueryException( "Unknown connection", e );
    } catch ( ReportDataFactoryException e ) {
      // break this and pstoellberger will haunt you!
      Throwable lastKnownParent = null;
      boolean oldPrd = false;
      Method[] allMethods = ReportDataFactoryException.class.getMethods();

      for ( Method m : allMethods ) {
        if ( !m.getName().equals( "getParentThrowable" ) ) {
          continue;
        }

        try {
          lastKnownParent = (Throwable) m.invoke( e );
          oldPrd = true;
        } catch ( Exception | Error e1 ) {
          // Exception | Error
        }

        break;
      }

      if ( !oldPrd || lastKnownParent == null ) {
        lastKnownParent = e.getCause();
      }

      if ( lastKnownParent != null ) {
        throw new QueryException( lastKnownParent.getMessage(), lastKnownParent );
      }

      throw new QueryException( e.getMessage(), e );
    }
  }


  public void initializeDataFactory( final DataFactory dataFactory, final Configuration configuration )
    throws ReportDataFactoryException {
    final ResourceManager resourceManager = CdaEngine.getInstance().getSettingsManager().getResourceManager();
    final ResourceKey contextKey = getCdaSettings().getContextKey();

    final ICdaEnvironment environment = CdaEngine.getEnvironment();
    environment.initializeDataFactory( dataFactory, configuration, contextKey, resourceManager );
  }


  @Override
  public List<PropertyDescriptor> getInterface() {
    return super.getInterface();
  }
}
