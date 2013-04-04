package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.table.TableModel;

import junit.framework.Assert;
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

public class FormulaParamTest extends TestCase {
  private static final Log logger = LogFactory.getLog(SqlTestFormula.class);

  public FormulaParamTest()
  {
    super();
  }

  public FormulaParamTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }
  
  public void testParam()throws Exception
  {
    // Define an outputStream
  
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-securityParam.cda");
    File settingsFile = new File(file.toURI());
    Assert.assertTrue(settingsFile.exists());
    
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    //set a session parameter
    final String testParamValue = "thisIsAGoodValue";
    
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();
    
    QueryOptions queryOptions = new QueryOptions();

    queryOptions.setDataAccessId("junitDataAccess");
    TableModel tableModel = cdaSettings.getDataAccess(queryOptions.getDataAccessId()).doQuery(queryOptions);
    Assert.assertEquals(1, tableModel.getRowCount());
    Assert.assertEquals(1, tableModel.getColumnCount());
    String result = (String) tableModel.getValueAt(0, 0);
    Assert.assertEquals(testParamValue, result);
  }
  
}
