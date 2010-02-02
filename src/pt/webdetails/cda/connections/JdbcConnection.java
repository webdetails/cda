package pt.webdetails.cda.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.utils.Util;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:18 PM
 */
public class JdbcConnection extends AbstractConnection {

  private static final Log logger = LogFactory.getLog(JdbcConnection.class);

  private ConnectionProvider connectionProvider;

  public JdbcConnection(final Element connection) throws InvalidConnectionException {

    super(connection);

  }


  @Override
  protected void initializeConnection(Element connection) throws InvalidConnectionException {

    JdbcConnectionInfo connectionInfo = new JdbcConnectionInfo(connection);

    logger.debug("Creating new jdbc connection");

    DriverConnectionProvider connectionProvider = new DriverConnectionProvider();
    connectionProvider.setDriver(connectionInfo.getDriver());
    connectionProvider.setUrl(connectionInfo.getUrl());

    logger.debug("Opening connection");
    try {
      connectionProvider.createConnection(connectionInfo.getUser(), connectionInfo.getPass());
    } catch (SQLException e) {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    logger.debug("Connection opened");

  }


  @Override
  public ConnectionProvider getConnectionProvider() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }


}
