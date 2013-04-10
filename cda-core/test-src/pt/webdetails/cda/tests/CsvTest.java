package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.CsvExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CsvTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(CsvTest.class);


  public void testCsvExport() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-sql.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2003-04-01");
    // queryOptions.addParameter("status","In Process");

    logger.info("Doing csv export");
    queryOptions.setOutputType("csv");
    queryOptions.addSetting(CsvExporter.CSV_SEPARATOR_SETTING, ",");
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("Doing xml export");
    queryOptions.setOutputType("xml");
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("Doing json export");
    queryOptions.setOutputType("json");
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("Doing xsl export");
    queryOptions.setOutputType("xls");
    engine.doQuery(out, cdaSettings, queryOptions);


  }
}
