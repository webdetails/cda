package pt.webdetails.cda.tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaQueryComponent;
/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CdaQueryComponentTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(CdaQueryComponentTest.class);

  public CdaQueryComponentTest()
  {
    super();
  }

  public CdaQueryComponentTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }

  
  public void testCdaQueryComponent() throws Exception {
    CdaQueryComponent component = new CdaQueryComponent();
    final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-sql.cda");
    component.setFile(settingsFile.getAbsolutePath());
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put("dataAccessId", "1");
    inputs.put("paramorderDate", "2003-04-01");
    component.setInputs(inputs);
    
    component.validate();
    component.execute();
    Assert.assertNotNull(component.getResultSet());
    
    Assert.assertEquals(4, component.getResultSet().getColumnCount());
    Assert.assertEquals(3, component.getResultSet().getRowCount());
    
  }
}
