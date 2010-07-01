package pt.webdetails.cda.tests;

import java.io.File;
import java.io.OutputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.config.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import junit.framework.TestCase;

public class IterableParameterSqlTest extends TestCase {
  private static final Log logger = LogFactory.getLog(SqlTest.class);

  public IterableParameterSqlTest()
  {
    super();
  }

  public IterableParameterSqlTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }


  public void testIterateStatus() throws Exception
  {
  	//Configuration configuration = new Configuration();
    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();

    final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-iterable-sql.cda");
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();
   // QueryOptions queryOptions;

    //net.sf.ehcache.enableShutdownHook
    //System.setProperty("net.sf.ehcache.enableShutdownHook","true");
    
   // Cache cache = AbstractDataAccess.getCache();
    
    for(int i=0; i<2;i++){
    	QueryOptions queryOptions = new QueryOptions();
	    queryOptions.setDataAccessId("1");
	    queryOptions.addParameter("status", "$FOREACH(2,0)");
	    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2003)");
	    queryOptions.getParameter("year").setDefaultValue("2003");
	    queryOptions.setOutputType("csv");
	    // queryOptions.addParameter("status","In Process");
	
	    logger.info("Doing first query");
	    try {
	    engine.doQuery(out, cdaSettings, queryOptions);
	    } catch(Exception e){
	    	throw e;
	    }
    }
    
    //testing orderly shutdown
 //   cache.dispose();
    
//    queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("4");
////    queryOptions.addParameter("status", "$FOREACH(2,0)");
////    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2525)");
////    queryOptions.getParameter("year").setDefaultValue("2004");//this time will fallback to default
//    queryOptions.setOutputType("csv");
//    // queryOptions.addParameter("status","In Process");
//
//    logger.info("Doing second query");
//    try {
//    engine.doQuery(out, cdaSettings, queryOptions);
//    } catch(Exception e){
//    	throw e;
//    }
//    cache.flush();
    
//    queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("1");
//    queryOptions.addParameter("status", "$FOREACH(2,0)");
//    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2525)");
//    queryOptions.getParameter("year").setDefaultValue("2004");//this time will fallback to default
//    queryOptions.setOutputType("csv");
//    // queryOptions.addParameter("status","In Process");
//
//    logger.info("Doing third query");
//    try {
//    engine.doQuery(out, cdaSettings, queryOptions);
//    } catch(Exception e){
//    	throw e;
//    }
//    logger.info("FIN");
  }
}
