package pt.webdetails.cda.connections.olap4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.DriverConnectionProvider;
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
 * Time: 12:47:29
 *
 * @author Thomas Morgner.
 */
public class JdbcConnection extends AbstractConnection implements Olap4JConnection {

  private static final ConnectionType connectionType = ConnectionType.MDX;
  private JdbcConnectionInfo connectionInfo;

  public JdbcConnection(final Element connection)
          throws InvalidConnectionException {
    super(connection);
  }

  public JdbcConnection() {
  }

  @Override
  protected void initializeConnection(final Element connection) throws InvalidConnectionException {
    connectionInfo = new JdbcConnectionInfo(connection);
  }

  public String getType() {
    return "olapJdbc";
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final JdbcConnection that = (JdbcConnection) o;

    if (!connectionInfo.equals(that.connectionInfo)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo.hashCode();
  }

  public OlapConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException {

    final DriverConnectionProvider connectionProvider = new DriverConnectionProvider();
    connectionProvider.setDriver(connectionInfo.getDriver());
    connectionProvider.setUrl(connectionInfo.getUrl());

    try {
      final Connection connection = connectionProvider.createConnection(connectionInfo.getUser(), connectionInfo.getPass());
      connection.close();
    } catch (SQLException e) {

      throw new InvalidConnectionException("JdbcConnection: Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    return connectionProvider;
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.OLAP4J;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
   ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("driver", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("url", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("user", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("password", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }
}
