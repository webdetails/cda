package pt.webdetails.cda;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.Util;

/**
 * Main class to test and execute the CDA in standalone mode
 * User: pedro
 * Date: Feb 1, 2010
 * Time: 12:30:41 PM
 */
public class CdaExecutor
{

  private static final Log logger = LogFactory.getLog(CdaExecutor.class);
  private static CdaExecutor _instance;


  public CdaExecutor()
  {

    logger.debug("Initializing CdaExecutor");


  }

  public static void main(final String[] args)
  {


    final CdaExecutor cdaExecutor = CdaExecutor.getInstance();

    cdaExecutor.doQuery();

  }

  private void doQuery()
  {


    try
    {

      logger.info("Building CDA settings from sample file");

      final SettingsManager settingsManager = SettingsManager.getInstance();

      final File settingsFile = new File("samples/sample.cda");
      final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());

      logger.debug("Doing query on Cda - Initializing CdaEngine");
      final CdaEngine engine = CdaEngine.getInstance();

      QueryOptions queryOptions = new QueryOptions();
      queryOptions.setDataAccessId("1");
      queryOptions.addParameter("orderDate", "2003-04-01");
      // queryOptions.addParameter("status","In Process");

      logger.info("Doing first query");
      engine.doQuery(cdaSettings, queryOptions);

      logger.info("Doing query with different parameters");
      queryOptions = new QueryOptions();
      queryOptions.setDataAccessId("1");
      queryOptions.addParameter("orderDate", "2004-01-01");
      engine.doQuery(cdaSettings, queryOptions);

      // Querying 2nd time to test cache
      logger.info("Doing query using the initial parameters - Cache should be used");
      queryOptions = new QueryOptions();
      queryOptions.setDataAccessId("1");
      queryOptions.addParameter("orderDate", "2003-04-01");
      engine.doQuery(cdaSettings, queryOptions);

      // Querying 2nd time to test cache
      logger.info("Doing query again to see if cache expires");
      try
      {
        Thread.sleep(10000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
      engine.doQuery(cdaSettings, queryOptions);


    }
    catch (DocumentException e)
    {
      logger.fatal("Unable to parse settings dom: " + Util.getExceptionDescription(e));
    }
    catch (UnsupportedConnectionException e)
    {
      logger.fatal("ConnectionException " + Util.getExceptionDescription(e));
    }
    catch (UnsupportedDataAccessException e)
    {
      logger.fatal("DataAccessException " + Util.getExceptionDescription(e));
    }
    catch (UnknownDataAccessException e)
    {
      logger.fatal("DataAccess id not found " + Util.getExceptionDescription(e));
    }
    catch (QueryException e)
    {
      logger.fatal("QueryException " + Util.getExceptionDescription(e));
    }


  }


  public static synchronized CdaExecutor getInstance()
  {

    if (_instance == null)
    {
      _instance = new CdaExecutor();
    }

    return _instance;
  }

}
