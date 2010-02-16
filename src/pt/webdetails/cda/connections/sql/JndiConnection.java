package pt.webdetails.cda.connections.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.utils.Util;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:54:14
 *
 * @author Thomas Morgner.
 */
public class JndiConnection extends AbstractConnection implements SqlConnection
{
  private JndiConnectionInfo connectionInfo;

  public JndiConnection(final Element connection)
      throws InvalidConnectionException
  {
    super(connection);
  }

  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException
  {

    final JndiConnectionProvider connectionProvider = new JndiConnectionProvider();
    connectionProvider.setConnectionPath(connectionInfo.getJndi());

    try
    {
      final Connection connection = connectionProvider.createConnection(null, null);
      connection.close();
    }
    catch (SQLException e)
    {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    return connectionProvider;
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new JndiConnectionInfo(connection);
  }

  public String getType()
  {
    return "sqlJndi";
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

    final JndiConnection that = (JndiConnection) o;

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
}
