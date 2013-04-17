package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import net.sf.ehcache.CacheManager;


import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.BeforeClass;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.CoreBeanFactory;
import pt.webdetails.cda.DefaultCdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CompoundJoinTest
{

  private static final Log logger = LogFactory.getLog(CompoundJoinTest.class);

  @BeforeClass
  public static void setUp() throws InitializationException{
      CoreBeanFactory cbf = new CoreBeanFactory("cda.standalone.spring.xml");
      DefaultCdaEnvironment env = new DefaultCdaEnvironment(cbf);
      CdaEngine.init(env);
  }
  
  @Test
  public void testCompoundQuery() throws Exception
  {
try{
    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");
    final SettingsManager settingsManager = SettingsManager.getInstance();
   // String fileString = CdaEngine.getEnvironment().getRepositoryAccess().getResourceAsString("sample-join.cda");
    
   // Assert.assertNotNull(fileString);
    
    URL file = this.getClass().getResource("sample-join.cda");
    //Assert.assertNotNull(file);//could get file  XXX currently failing
    
    File settingsFile = new File(file.toURI());
    
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.error("Doing query on Cda - Initializing CdaEngine");
    

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("3");
    queryOptions.setOutputType("json");
    //queryOptions.addParameter("status","In Process");
    logger.info("Doing query");

    CdaEngine.getInstance().doQuery(out, cdaSettings, queryOptions);
}catch(Exception e){
    e.printStackTrace();
    Assert.fail();
}


  }

}