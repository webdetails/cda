package pt.webdetails.cda.connections.olap4j;

import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.DriverConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.OlapConnectionProvider;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.utils.Util;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:47:29
 *
 * @author Thomas Morgner.
 */
public class JdbcConnection extends AbstractConnection implements Olap4JConnection
{
  private JdbcConnectionInfo connectionInfo;

  public JdbcConnection(final Element connection)
      throws InvalidConnectionException
  {
    super(connection);
  }


  @Override
  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new JdbcConnectionInfo(connection);
  }

  public String getType()
  {
    return null;
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

    if (!connectionInfo.equals(that.connectionInfo))
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return connectionInfo.hashCode();
  }

  public OlapConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException
  {

    final DriverConnectionProvider connectionProvider = new DriverConnectionProvider();
    connectionProvider.setDriver(connectionInfo.getDriver());
    connectionProvider.setUrl(connectionInfo.getUrl());

    try
    {
      final Connection connection = connectionProvider.createConnection(connectionInfo.getUser(), connectionInfo.getPass());
      connection.close();
    }
    catch (SQLException e)
    {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    return connectionProvider;
  }
}
