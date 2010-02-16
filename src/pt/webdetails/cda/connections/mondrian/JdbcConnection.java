package pt.webdetails.cda.connections.mondrian;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DriverDataSourceProvider;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.utils.Util;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:18 PM
 */
public class JdbcConnection extends AbstractConnection implements MondrianConnection
{

  private static final Log logger = LogFactory.getLog(JdbcConnection.class);
  public static final String TYPE = "mondrianJdbc";

  private JdbcConnectionInfo connectionInfo;

  public JdbcConnection(final Element connection) throws InvalidConnectionException
  {

    super(connection);

  }


  @Override
  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {

    connectionInfo = new JdbcConnectionInfo(connection);

  }

  @Override
  public String getType()
  {
    return TYPE;
  }


  @Override
  public DataSourceProvider getInitializedDataSourceProvider() throws InvalidConnectionException
  {


    logger.debug("Creating new jdbc connection");

    final DriverDataSourceProvider connectionProvider = new DriverDataSourceProvider();
    connectionProvider.setDriver(connectionInfo.getDriver());
    connectionProvider.setUrl(connectionInfo.getUrl());


    logger.debug("Opening connection");
    try
    {
      // connectionProvider.createConnection(connectionInfo.getUser(), connectionInfo.getPass());
    }
    catch (Exception e)
    {

      throw new InvalidConnectionException("JdbcConnection: Found Exception: " + Util.getExceptionDescription(e), e);
    }

    logger.debug("Connection opened");

    return connectionProvider;
  }

  public JdbcConnectionInfo getConnectionInfo()
  {
    return connectionInfo;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final JdbcConnection that = (JdbcConnection) o;

    if (connectionInfo != null ? !connectionInfo.equals(that.connectionInfo) : that.connectionInfo != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }
}
