package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Main class to test and execute the CDA in standalone mode
 * User: pedro
 * Date: Feb 1, 2010
 * Time: 12:30:41 PM
 */
public class CdaExecutor {

  private static final Log logger = LogFactory.getLog(CdaExecutor.class);
  private static CdaExecutor _instance;


  public CdaExecutor() {

    logger.debug("Initializing CdaExecutor");


  }

  public static void main(final String[] args) {


    final CdaExecutor cdaExecutor = CdaExecutor.getInstance();

    cdaExecutor.doQuery();

  }

  private void doQuery() {


    try {

      logger.info("Building CDA settings from sample file");

      final SettingsManager settingsManager = SettingsManager.getInstance();

      final File settingsFile = new File("samples/sample.cda");
      final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());

      logger.debug("Doing query on Cda - Initializing CdaEngine");
      final CdaEngine engine = CdaEngine.getInstance();
      engine.doQuery(cdaSettings,"1");

      
    } catch (DocumentException e) {
      logger.fatal("Unable to parse settings dom: " + Util.getExceptionDescription(e));
    } catch (UnsupportedConnectionException e) {
      logger.fatal("ConnectionException " + Util.getExceptionDescription(e));
    } catch (UnsupportedDataAccessException e) {
      logger.fatal("DataAccessException " + Util.getExceptionDescription(e));
    } catch (UnknownDataAccessException e) {
      logger.fatal("DataAccess id not found " + Util.getExceptionDescription(e));
    } catch (QueryException e) {
      logger.fatal("QueryException " + Util.getExceptionDescription(e));
    }


  }


  public static synchronized CdaExecutor getInstance() {

    if (_instance == null)
      _instance = new CdaExecutor();

    return _instance;
  }

}
