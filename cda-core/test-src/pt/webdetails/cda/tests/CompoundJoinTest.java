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
public class CompoundJoinTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(CompoundJoinTest.class);

  public void testCompoundQuery() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");
    final SettingsManager settingsManager = SettingsManager.getInstance();

    
    URL file = this.getClass().getResource("sample-join.cda");
    File settingsFile = new File(file.toURI());
    final CdaEngine engine = CdaEngine.getInstance();
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.error("Doing query on Cda - Initializing CdaEngine");
    

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("3");
    queryOptions.setOutputType("json");
    // queryOptions.addParameter("status","In Process");

    logger.info("Doing query");
    engine.doQuery(out, cdaSettings, queryOptions);


  }

}