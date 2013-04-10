package pt.webdetails.cda.tests;

import java.io.File;
import java.net.URL;

import javax.swing.table.TableModel;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

public class FormulaParamTest extends TestCase {
  private static final Log logger = LogFactory.getLog(SqlFormulaTest.class);

  public void testParam()throws Exception
  {

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-securityParam.cda");
    File settingsFile = new File(file.toURI());
    Assert.assertTrue(settingsFile.exists());
    
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    //set a session parameter
    final String testParamValue = "thisIsAGoodValue";
    
    QueryOptions queryOptions = new QueryOptions();

    queryOptions.setDataAccessId("junitDataAccess");
    TableModel tableModel = cdaSettings.getDataAccess(queryOptions.getDataAccessId()).doQuery(queryOptions);
    Assert.assertEquals(1, tableModel.getRowCount());
    Assert.assertEquals(1, tableModel.getColumnCount());
    String result = (String) tableModel.getValueAt(0, 0);
    Assert.assertEquals(testParamValue, result);
  }
  
}
