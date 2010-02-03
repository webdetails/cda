package pt.webdetails.cda.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

import java.sql.SQLException;
import java.util.logging.Level;

import static org.apache.commons.logging.LogFactory.getLog;


/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Jan 28, 2010
 * Time: 12:27:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlQueryTest {

  private static final Log logger = LogFactory.getLog(SqlQueryTest.class);
  
  private static SqlQueryTest _instance;

  public SqlQueryTest() {

    init();
  }

  private void makeTest() {

    logger.info("Starting main test");

    logger.debug("Initializing db connection - jdbc");

    try {

      final DriverConnectionProvider jdbcConnectionProvider = new DriverConnectionProvider();
      jdbcConnectionProvider.setDriver("com.mysql.jdbc.Driver");
      jdbcConnectionProvider.setUrl("jdbc:mysql://localhost:3306/metrics?defaultFetchSize=500&useCursorFetch=true");

      logger.debug("Opening connection");
      jdbcConnectionProvider.createConnection("root", "pedro");

      logger.info("Connection opened");


          
    } catch (SQLException e) {

      logger.fatal(
          "Error creating connection: " + e.getClass().getName() + " - " + e.getMessage());
      //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }


  }

  private void init() {

    logger.debug("Initializing SqlQueryTest");

    ClassicEngineBoot.getInstance().start();

  }


  private static SqlQueryTest getInstance() {

    if (_instance == null)
      _instance = new SqlQueryTest();

    return _instance;
  }


  public static void main(final String[] args) {

    final SqlQueryTest sqlQueryTest = SqlQueryTest.getInstance();

    sqlQueryTest.makeTest();


  }

}