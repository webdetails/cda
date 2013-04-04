/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.connections;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.CdaCoreService;

/**
 *
 * @author pdpi
 */
public class ConnectionCatalog {

  public enum ConnectionType {

    SQL, MQL, MDX, OLAP4J, SCRIPTING, NONE, XPATH, KETTLE
  };
  private static ConnectionCatalog _instance;
  private static Log logger = LogFactory.getLog(ConnectionCatalog.class);
  public static final String PLUGIN_DIR = PentahoSystem.getApplicationContext().getSolutionPath("system/" + CdaCoreService.PLUGIN_NAME);
  private HashMap<String, ConnectionInfo> connectionPool;

  public ConnectionCatalog() {
    getConnections();
  }

  private void getConnections() {
    connectionPool = new HashMap<String, ConnectionInfo>();
    File dir = new File(PLUGIN_DIR + "/resources/components/connections");
    FilenameFilter xmlFiles = new FilenameFilter() {

      public boolean accept(File dir, String name) {
        return !name.startsWith(".") && name.endsWith(".xml");
      }
    };
    String[] files = dir.list(xmlFiles);
    if (files != null && files.length > 0) {
      for (String file : files) {
        try {
          Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file, null);
          // To figure out whether the component is generic or has a special implementation,
          // we directly look for the class override in the definition
          String className = XmlDom4JHelper.getNodeText("/Connection/Implementation", doc);
          if (className != null) {
            Connection connection = connectionFromClass(className);
            if (connection != null) {
              String connectionType = XmlDom4JHelper.getNodeText("/Connection/Type", doc);
              ConnectionType ct = ConnectionType.valueOf(connectionType);
              connectionPool.put(connection.getClass().toString(), new ConnectionInfo(ct, connection.getClass()));
            }
          }
        } catch (Exception e) {
          logger.error(e);
        }
      }
    }
  }

  public Connection[] getConnectionsByType(ConnectionType type) {
    ArrayList<Connection> conns = new ArrayList<Connection>();
    for (String key : connectionPool.keySet()) {
      ConnectionInfo conn = connectionPool.get(key);
      if (conn.getType() == type) {
        try {
          conns.add(conn.getImplementation().getConstructor().newInstance());
        } catch (Exception ex) {
          logger.error("Couldn't instantiate " + conn.toString());
        }
      }
    }
    return conns.toArray(new Connection[conns.size()]);
  }

  private Connection connectionFromClass(String className) {
    Connection connection = null;
    try {
      Class<?> cClass = Class.forName(className);
      if (!cClass.isInterface() && Connection.class.isAssignableFrom(cClass)) {
        connection = (Connection) cClass.newInstance();
      }
    } catch (Exception ex) {
      logger.error(ex);
    }
    return connection;
  }

  public synchronized static ConnectionCatalog getInstance(boolean refreshCache) {
    if (_instance == null || refreshCache) {
      _instance = new ConnectionCatalog();
    }
    return _instance;
  }
}

class ConnectionInfo {

  public Class<? extends Connection> getImplementation() {
    return implementation;
  }

  public ConnectionType getType() {
    return type;
  }
  private ConnectionType type;
  private Class<? extends Connection> implementation;

  public ConnectionInfo(ConnectionType type, Class<? extends Connection> implementation) {
    this.type = type;
    this.implementation = implementation;
  }
}
