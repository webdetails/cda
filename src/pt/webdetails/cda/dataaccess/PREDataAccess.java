package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.states.CachingDataFactory;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

import javax.swing.table.TableModel;
import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This is the DataAccess implementation for PentahoReportingEngine based queries.
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 2:18:19 PM
 */
public abstract class PREDataAccess extends SimpleDataAccess {

  private static final Log logger = LogFactory.getLog(PREDataAccess.class);


  public PREDataAccess(final Element element) {
    super(element);


  }


  public abstract DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException;

  @Override
  public TableModel queryData() throws QueryException {

    try {

      CachingDataFactory dataFactory = new CachingDataFactory(getDataFactory());

      ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final ResourceKey contextKey = resourceManager.createKey(new File(getCdaSettings().getId()));



      dataFactory.initialize(ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager,
              contextKey,
              new LibLoaderResourceBundleFactory(resourceManager, contextKey, Locale.getDefault(), TimeZone.getDefault()));

      // you may give it some real parameters via the constructor of parameter-datarow ...
      final ParameterDataRow parameters = new ParameterDataRow();
      // fire the query. you always get a tablemodel or an exception.
      final TableModel tableModel = dataFactory.queryData("Query 1", parameters);

      // process your data as you like

      // and at the end, close your tablemodel if it holds on to resources like a resultset
      if (tableModel instanceof CloseableTableModel) {
        CloseableTableModel ctm = (CloseableTableModel) tableModel;
        ctm.close();
      }

      // and finally shut down the datafactory to free any connection that may be open.
      dataFactory.close();



    } catch (UnknownConnectionException e) {
      throw new QueryException("Unknown connection", e);
    } catch (InvalidConnectionException e) {
      throw new QueryException("Unknown connection", e);
    } catch (ResourceKeyCreationException e) {
      throw new QueryException("ResourceKeyCreateException", e);
    }
    catch (ReportDataFactoryException e)
    {
      throw new QueryException("ResourceKeyCreateException", e);
    }


    logger.fatal("Not Implemented Yet!!");
    return null;  //To change body of implemented methods use File | Settings | File Templates.


  }

}
