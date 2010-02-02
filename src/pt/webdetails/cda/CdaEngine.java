package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

/**
 * Main engine class that will answer to calls
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:24:16 PM
 */
public class CdaEngine {


  private static final Log logger = LogFactory.getLog(CdaEngine.class);
  private static CdaEngine _instance;


  public CdaEngine() {
    logger.info("Initializing CdaEngine");
    init();

  }


  private void doQuery() {

    logger.debug("Doing query on Cda - Initializing CdaEngine");

    final CdaEngine engine = CdaEngine.getInstance();

  }


  private void init() {

    // Start ClassicEngineBoot 
    ClassicEngineBoot.getInstance().start();

  }



  public static CdaEngine getInstance() {

    if (_instance == null)
      _instance = new CdaEngine();

    return _instance;
  }
}
