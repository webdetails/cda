package pt.webdetails.cda.connections.scripting;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:22:05
 *
 * @author Thomas Morgner.
 */
public class ScriptingConnection extends AbstractConnection
{

  private ScriptingConnectionInfo connectionInfo;

  public ScriptingConnection(final Element connection)
      throws InvalidConnectionException
  {
    super(connection);
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new ScriptingConnectionInfo(connection);
  }

  public String getType()
  {
    return "scripting";
  }

  public ScriptingConnectionInfo getScriptingConnectionInfo()
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

    final ScriptingConnection that = (ScriptingConnection) o;

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
