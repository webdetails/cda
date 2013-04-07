/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.table.TableModel;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.parameters.CompoundDataRow;
import org.pentaho.reporting.engine.classic.core.cache.CachingDataFactory;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.CdaEngine;

/**
 * This is the DataAccess implementation for PentahoReportingEngine based queries.
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 2:18:19 PM
 */
public abstract class PREDataAccess extends SimpleDataAccess
{

//  private static final Log logger = LogFactory.getLog(PREDataAccess.class);

  public PREDataAccess()
  {
  }


  public PREDataAccess(final Element element)
  {

    super(element);

  }


  /**
   * 
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public PREDataAccess(String id, String name, String connectionId, String query)
  {
    super(id, name, connectionId, query);
  }


  public abstract DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException;

  
  protected static class PREDataSourceQuery implements IDataSourceQuery{

    private TableModel tableModel;
    private CachingDataFactory localDataFactory;

    public PREDataSourceQuery(TableModel tm, CachingDataFactory df){
      this.tableModel = tm;
      this.localDataFactory = df;
    }
    
    @Override
    public TableModel getTableModel() {
      return tableModel;
    }

    @Override
    public void closeDataSource() throws QueryException {
      if (localDataFactory == null)
      {
        return;
      }

      // and at the end, close your tablemodel if it holds on to resources like a resultset
      if (getTableModel() instanceof CloseableTableModel)
      {
        final CloseableTableModel ctm = (CloseableTableModel) getTableModel();
        ctm.close();
      }

      // and finally shut down the datafactory to free any connection that may be open.
      localDataFactory.close();

      localDataFactory = null;
    }
    
  }
  

  @Override
  protected IDataSourceQuery performRawQuery(final ParameterDataRow parameterDataRow) throws QueryException
  {
    
    boolean threadVarSet = false;
    
    try
    {

      final CachingDataFactory dataFactory = new CachingDataFactory(getDataFactory(), false);

      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final ResourceKey contextKey = getCdaSettings().getContextKey();


      final Configuration configuration = ClassicEngineBoot.getInstance().getGlobalConfig();
      dataFactory.initialize(configuration, resourceManager, contextKey,
              new LibLoaderResourceBundleFactory(resourceManager, contextKey, Locale.getDefault(), TimeZone.getDefault()));

      dataFactory.open();

      
      PREDataSourceQuery queryExecution = null;
      
      try {
        // fire the query. you always get a tablemodel or an exception.

        final ReportEnvironmentDataRow environmentDataRow;
        IDataAccessUtils dataAccessUtils = (IDataAccessUtils)CdaEngine.getInstance().getBeanFactory().getBean("IDataAccessUtils");
        environmentDataRow = dataAccessUtils.createEnvironmentDataRow(configuration);

        final TableModel tm = dataFactory.queryData("query",
                new CompoundDataRow(environmentDataRow, parameterDataRow));

        //  Store this variable so that we can close it later
        queryExecution = new PREDataSourceQuery(tm, dataFactory);
      } finally {
        //There was an exception while getting the dataset - need to make sure 
        //that the dataFactory is closed
        if (queryExecution == null) {
          dataFactory.close();
        }
      }
      
      return queryExecution;

    }
    catch (UnknownConnectionException e)
    {
      throw new QueryException("Unknown connection", e);
    }
    catch (InvalidConnectionException e)
    {
      throw new QueryException("Unknown connection", e);
    }
    catch (ReportDataFactoryException e)
    {
      throw new QueryException(e.getMessage(), e);
//              + ((e.getParentThrowable() == null) ? "" : ("; Parent exception: " + e.getParentThrowable().getMessage())) + "\n" +
//              ((e.getParentThrowable() != null && e.getParentThrowable().getCause() != null)?e.getParentThrowable().getCause().getMessage() :"") + "\n"
//              , e);
    }
//    finally
//    {
//      //leave thread variable as it was
//      if(threadVarSet) SolutionReposHelper.setSolutionRepositoryThreadVariable(null);
//    }

  }

  @Override
  public ArrayList<PropertyDescriptor> getInterface()
  {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    return properties;
  }
}
