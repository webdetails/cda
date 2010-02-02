package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Main class to test and execute the CDA in standalone mode
 * User: pedro
 * Date: Feb 1, 2010
 * Time: 12:30:41 PM
 */
public class CdaExecutor {

  private static final Log logger = LogFactory.getLog(CdaExecutor.class);
  private static CdaExecutor _instance;


  public CdaExecutor() {
    
    logger.debug("Initializing CdaExecutor");



  }

  public static void main(String[] args) {


    final CdaExecutor cdaExecutor = CdaExecutor.getInstance();

    cdaExecutor.doQuery();

  }

  private void doQuery() {

    logger.debug("Doing query on Cda - Initializing CdaEngine");

    CdaEngine engine = CdaEngine.getInstance();


  }


  public static CdaExecutor getInstance() {

    if (_instance == null)
      _instance = new CdaExecutor();

    return _instance;
  }

}
