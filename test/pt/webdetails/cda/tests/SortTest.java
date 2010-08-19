package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class SortTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(SortTest.class);

  public SortTest()
  {
    super();
  }

  public SortTest(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }

  public void testSqlQuery() throws ExporterException, UnknownDataAccessException, UnsupportedExporterException, QueryException, UnsupportedConnectionException, DocumentException, UnsupportedDataAccessException
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();

    final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-mondrian-compact.cda");
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipped");

    logger.info("Doing query with 2 column sort");
    queryOptions.setDataAccessId("1");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"0D" , "1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with no sort");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with all combinations");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"0D" , "2", "1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with only one sort");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

  }

  /*
  public void testJndiQuery() throws ExporterException, UnknownDataAccessException, UnsupportedExporterException, QueryException, UnsupportedConnectionException, DocumentException, UnsupportedDataAccessException
  {


  // Define an outputStream
  OutputStream out = System.out;

  logger.info("Building CDA settings from sample file");

  final SettingsManager settingsManager = SettingsManager.getInstance();

  final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-mondrian-jndi.cda");
  final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
  logger.debug("Doing query on Cda - Initializing CdaEngine");
  final CdaEngine engine = CdaEngine.getInstance();

  QueryOptions queryOptions = new QueryOptions();
  queryOptions.setDataAccessId("2");
  queryOptions.setOutputType("json");
  queryOptions.addParameter("status", "Shipped");

  logger.info("Doing query");
  engine.doQuery(out, cdaSettings, queryOptions);


  }
   * 
   */
}
