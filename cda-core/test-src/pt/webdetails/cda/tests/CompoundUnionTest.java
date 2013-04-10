package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CompoundUnionTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(CompoundUnionTest.class);

  public void testCompoundQuery() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");
    final CdaEngine engine = CdaEngine.getInstance();
    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-union.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.error("Doing query on Cda - Initializing CdaEngine");

    QueryOptions queryOptions = new QueryOptions();
    //queryOptions.addParameter("year","2005");
    queryOptions.setDataAccessId("3");
    queryOptions.setOutputType("json");
    // queryOptions.addParameter("status","In Process");

    logger.info("Doing query");
    engine.doQuery(out, cdaSettings, queryOptions);


  }

}