package pt.webdetails.cda.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.settings.CdaSettings;
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
  public static final String TYPE = "jdbc";

  private CdaSettings cdaSettings;
  private ConnectionProvider connectionProvider;
  private JdbcConnectionInfo connectionInfo;

  public JdbcConnection(final Element connection) throws InvalidConnectionException {

    super(connection);

  }


  @Override
  protected void initializeConnection(final Element connection) throws InvalidConnectionException {

    connectionInfo = new JdbcConnectionInfo(connection);

  }

  @Override
  public String getType() {
    return TYPE;
  }


  @Override
  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException {


    logger.debug("Creating new jdbc connection");

    final DriverConnectionProvider connectionProvider = new DriverConnectionProvider();
    connectionProvider.setDriver(connectionInfo.getDriver());
    connectionProvider.setUrl(connectionInfo.getUrl());
    logger.debug("Opening connection");
    try {
      connectionProvider.createConnection(connectionInfo.getUser(), connectionInfo.getPass());
    } catch (SQLException e) {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    logger.debug("Connection opened");

    return connectionProvider;
  }

  @Override
  public void setCdaSettings(CdaSettings cdaSettings) {
    this.cdaSettings = cdaSettings;
  }


}
