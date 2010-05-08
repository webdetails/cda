package pt.webdetails.cda.connections.xpath;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 13:49:12
 *
 * @author Thomas Morgner.
 */
public class XPathConnection extends AbstractConnection
{
  private XPathConnectionInfo connectionInfo;

  public XPathConnection(final Element connection)
      throws InvalidConnectionException
  {
    super(connection);
  }

  public XPathConnection()
  {
  }

  public ConnectionCatalog.ConnectionType getGenericType()
  {
    return ConnectionCatalog.ConnectionType.XPATH;
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new XPathConnectionInfo(connection);
  }

  public String getType()
  {
    return "xpath";
  }

  public String getXqueryDataFile()
  {
    if (connectionInfo == null)
    {
      throw new IllegalStateException();
    }
    return connectionInfo.getXqueryDataFile();
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

    final XPathConnection that = (XPathConnection) o;

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
