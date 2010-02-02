package pt.webdetails.cda.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:18 PM
 */
public class JdbcConnection extends AbstractConnection {

  private static final Log logger = LogFactory.getLog(JdbcConnection.class);

  public JdbcConnection(final Element connection) {

    super(connection);


  }


  @Override
  protected void initializeConnection(Element connection) {

    logger.debug("Creating new jdbc connection");

    logger.warn("TODO - initializeConnection not done yet");

  }


  @Override
  public ConnectionProvider getConnectionProvider() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
