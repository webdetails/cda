package pt.webdetails.cda.dataaccess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Mar 25, 2010
 * Time: 5:06:02 PM
 */
public class DataAccessConnectionDescriptor {

  private static final Log logger = LogFactory.getLog(SettingsManager.class);

  private String name;
  private String dataAccessType;
  private ArrayList<PropertyDescriptor> dataAccessInfo;
  private ArrayList<PropertyDescriptor> connectionInfo;

  public DataAccessConnectionDescriptor() {
    dataAccessInfo = new ArrayList<PropertyDescriptor>();
    connectionInfo = new ArrayList<PropertyDescriptor>();
  }

  public DataAccessConnectionDescriptor(final String name) {
    this();
    setName(name);
  }

  public void addDataAccessProperty(PropertyDescriptor p) {
    dataAccessInfo.add(p);
  }

  public void addConnectionProperty(PropertyDescriptor p) {
    connectionInfo.add(p);
  }

  public void addDataAccessProperty(Collection<PropertyDescriptor> p) {
    dataAccessInfo.addAll(p);
  }

  public void addConnectionProperty(Collection<PropertyDescriptor> p) {
    connectionInfo.addAll(p);
  }

  public ArrayList<PropertyDescriptor> getDescriptors() {

    ArrayList<PropertyDescriptor> collapsedInfo = new ArrayList<PropertyDescriptor>();
    collapsedInfo.addAll(connectionInfo);
    collapsedInfo.addAll(dataAccessInfo);

    return collapsedInfo;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String toJSON() {
    StringBuilder output = new StringBuilder();
    if (dataAccessInfo.size() > 0) {
      output.append("\"" + name + "\": {\n");
      if (connectionInfo.size() > 0) {
        output.append("\t\"connection\": {\n");
        for (PropertyDescriptor prop : connectionInfo) {
          output.append("\t\t\"" + prop.getName() + "\": {\"type\": \"" + prop.getType() + "\"},\n");
        }
        output.append("\t},\n");
      }
      for (PropertyDescriptor prop : dataAccessInfo) {
        output.append("\t\"" + prop.getName() + "\": {\"type\": \"" + prop.getType() + "\"},\n");
      }
      output.append("}");
    }
    return output.toString().replaceAll(",\n}", "\n}").replaceAll(",\n\t}", "\n\t}");
  }

  public static DataAccessConnectionDescriptor[] fromClass(Class dataAccess) throws Exception {
    ArrayList<DataAccessConnectionDescriptor> descriptors = new ArrayList<DataAccessConnectionDescriptor>();
    AbstractDataAccess sample = (AbstractDataAccess) dataAccess.newInstance();
    Connection[] conns = sample.getAvailableConnections();
    if (conns.length > 0) {
      ArrayList<PropertyDescriptor> props = sample.getInterface();
      for (Connection conn : conns) {
        try {
          String name = sample.getType() + (!(conn.getGenericType().equals(ConnectionType.NONE)) ? ("_" + conn.getType()) : "");
          DataAccessConnectionDescriptor descriptor = new DataAccessConnectionDescriptor(name);
          descriptor.addDataAccessProperty(props);
          descriptor.addConnectionProperty(conn.getProperties());
          descriptors.add(descriptor);
        } catch (UnsupportedOperationException e) {
          logger.warn("Failed to generate a descriptor for " + sample.getType() + "_" + conn.getType());
        }
      }
    }
    return descriptors.toArray(new DataAccessConnectionDescriptor[descriptors.size()]);
  }
}
