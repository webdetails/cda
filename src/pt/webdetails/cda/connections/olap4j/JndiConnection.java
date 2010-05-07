package pt.webdetails.cda.connections.olap4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.OlapConnectionProvider;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.utils.Util;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:59:15
 *
 * @author Thomas Morgner.
 */
public class JndiConnection extends AbstractConnection implements Olap4JConnection {

  private JndiConnectionInfo connectionInfo;

  public JndiConnection(final Element connection)
          throws InvalidConnectionException {
    super(connection);
  }

  public JndiConnection() {
  }

  public OlapConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException {

    final JndiConnectionProvider connectionProvider = new JndiConnectionProvider();
    connectionProvider.setConnectionPath(connectionInfo.getJndi());
    connectionProvider.setUsername(connectionInfo.getUser());
    connectionProvider.setPassword(connectionInfo.getPass());
    
    try {
      final Connection connection = connectionProvider.createConnection(null, null);
      connection.close();
    } catch (SQLException e) {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    return connectionProvider;
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException {
    connectionInfo = new JndiConnectionInfo(connection);
  }

  public String getType() {
    return "olapJndi";
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final JndiConnection that = (JndiConnection) o;

    if (!connectionInfo.equals(that.connectionInfo)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo.hashCode();
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.OLAP4J;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
   ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("jndi", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }

  public String getRoleField()
  {
    return connectionInfo.getRoleField();
  }

  public String getUserField()
  {
    return connectionInfo.getUserField();
  }

  public String getPasswordField()
  {
    return connectionInfo.getPasswordField();
  }
}
