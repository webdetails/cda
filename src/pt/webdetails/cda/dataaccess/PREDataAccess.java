package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.parameters.CompoundDataRow;
import org.pentaho.reporting.engine.classic.core.states.CachingDataFactory;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.platform.plugin.PentahoReportEnvironment;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * This is the DataAccess implementation for PentahoReportingEngine based queries.
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 2:18:19 PM
 */
public abstract class PREDataAccess extends SimpleDataAccess
{

  private static final Log logger = LogFactory.getLog(PREDataAccess.class);
  private TableModel tableModel;
  private CachingDataFactory localDataFactory;

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
  public PREDataAccess(String id, String name, String connectionId, String query){
  	super(id, name, connectionId, query);
  }

  public abstract DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException;

  @Override
  protected TableModel performRawQuery(final ParameterDataRow parameterDataRow) throws QueryException
  {
    try
    {

      final CachingDataFactory dataFactory = new CachingDataFactory(getDataFactory());

      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final ResourceKey contextKey = getCdaSettings().getContextKey();


      final Configuration configuration = ClassicEngineBoot.getInstance().getGlobalConfig();
      dataFactory.initialize(configuration, resourceManager, contextKey,
              new LibLoaderResourceBundleFactory(resourceManager, contextKey, Locale.getDefault(), TimeZone.getDefault()));

      dataFactory.open();
      // fire the query. you always get a tablemodel or an exception.

      final ReportEnvironmentDataRow environmentDataRow;
      if (CdaEngine.getInstance().isStandalone())
      {
        environmentDataRow = new ReportEnvironmentDataRow(new DefaultReportEnvironment(configuration));
      }
      else
      {
        environmentDataRow = new ReportEnvironmentDataRow(new PentahoReportEnvironment(configuration));
      }

      final TableModel tm = dataFactory.queryData("query",
              new CompoundDataRow(environmentDataRow, parameterDataRow));

      // Store this variable so that we can close it later
      setLocalDataFactory(dataFactory);
      setTableModel(tm);
      return tm;

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
      throw new QueryException("ReportDataFactoryException : " + e.getMessage()
  				+ ((e.getParentThrowable() == null) ? "" : ("; Parent exception: " + e.getParentThrowable().getMessage())), e);
    }


  }

  public void closeDataSource() throws QueryException
  {

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
    getLocalDataFactory().close();

    localDataFactory = null;
  }

  public TableModel getTableModel()
  {
    return tableModel;
  }

  public void setTableModel(final TableModel tableModel)
  {
    this.tableModel = tableModel;
  }

  public CachingDataFactory getLocalDataFactory()
  {
    return localDataFactory;
  }

  public void setLocalDataFactory(final CachingDataFactory localDataFactory)
  {
    this.localDataFactory = localDataFactory;
  }
  /*
  public static ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors() {
  ArrayList<DataAccessConnectionDescriptor> descriptor = new ArrayList<DataAccessConnectionDescriptor>();
  DataAccessConnectionDescriptor proto = new DataAccessConnectionDescriptor();
  proto.addDataAccessProperty(new PropertyDescriptor("Query",PropertyDescriptor.TYPE.STRING,PropertyDescriptor.SOURCE.DATAACCESS));
  descriptor.add(proto);
  return descriptor;
  }
   */

  @Override
  public ArrayList<PropertyDescriptor> getInterface()
  {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    return properties;
  }
}
