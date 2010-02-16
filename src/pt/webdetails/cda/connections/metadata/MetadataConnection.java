package pt.webdetails.cda.connections.metadata;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:10:27
 *
 * @author Thomas Morgner.
 */
public class MetadataConnection extends AbstractConnection
{
  private MetadataConnectionInfo connectionInfo;

  public MetadataConnection(final Element connection)
      throws InvalidConnectionException
  {
    super(connection);
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new MetadataConnectionInfo(connection);
  }

  public String getType()
  {
    return "metadata";
  }

  public MetadataConnectionInfo getMetadataConnectionInfo()
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

    final MetadataConnection that = (MetadataConnection) o;

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
