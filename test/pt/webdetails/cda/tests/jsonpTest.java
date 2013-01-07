package pt.webdetails.cda.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 15, 2010 Time: 7:53:13 PM
 */
public class jsonpTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(jsonpTest.class);


  public jsonpTest()
  {
    super();
  }


  public jsonpTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }


  public void testJsonPQuery() throws ExporterException, UnknownDataAccessException, UnsupportedExporterException, QueryException, UnsupportedConnectionException, DocumentException, UnsupportedDataAccessException
  {


    // Define an outputStream
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();

    final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-sql.cda");
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2003-04-01");
    queryOptions.setOutputType(ExporterEngine.OutputType.JSON);
    // queryOptions.addParameter("status","In Process");

    logger.info("Doing Json query");
    engine.doQuery(out, cdaSettings, queryOptions);
    assertEquals(out.toString(), "{\"queryInfo\":{\"totalRows\":\"3\"},\"resultset\":[[\"Shipped\",2003,3090389.1499999976,3.0903891499999976],[\"Shipped\",2004,4750205.889999998,4.750205889999998],[\"Shipped\",2005,1513074.4600000002,1.5130744600000002]],\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"STATUS\"},{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"Year\"},{\"colIndex\":2,\"colType\":\"Numeric\",\"colName\":\"PRICE\"},{\"colIndex\":3,\"colType\":\"Numeric\",\"colName\":\"PriceInK\"}]}");
    
    
    logger.info("Doing Jsonp query");
    queryOptions.addSetting("callback", "callbackFun");

    // Flush and do another test
    out.reset();
    

    engine.doQuery(out, cdaSettings, queryOptions);
    assertEquals(out.toString(), "callbackFun({\"queryInfo\":{\"totalRows\":\"3\"},\"resultset\":[[\"Shipped\",2003,3090389.1499999976,3.0903891499999976],[\"Shipped\",2004,4750205.889999998,4.750205889999998],[\"Shipped\",2005,1513074.4600000002,1.5130744600000002]],\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"STATUS\"},{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"Year\"},{\"colIndex\":2,\"colType\":\"Numeric\",\"colName\":\"PRICE\"},{\"colIndex\":3,\"colType\":\"Numeric\",\"colName\":\"PriceInK\"}]});");


    logger.info("Output:" + out.toString());





  }
}
