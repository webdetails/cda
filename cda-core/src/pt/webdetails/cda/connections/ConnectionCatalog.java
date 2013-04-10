/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cpf.repository.IRepositoryFile;
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
  private HashMap<String, ConnectionInfo> connectionPool;

  public ConnectionCatalog() {
    getConnections();
  }

  private void getConnections() {
    connectionPool = new HashMap<String, ConnectionInfo>();

    List<IRepositoryFile> files = CdaEngine.getEnvironment().getComponentsFiles();
    
    if (files != null && files.size() > 0) {
      for (IRepositoryFile file : files) {
        ByteArrayInputStream bais = null;
        try {
          bais = new ByteArrayInputStream(file.getData());
          SAXReader reader = new SAXReader();//XXX check
          Document doc = reader.read(bais);
          // To figure out whether the component is generic or has a special implementation,
          // we directly look for the class override in the definition
          Node implementation = doc.selectSingleNode("/Connection/Implementation");
          Node type = doc.selectSingleNode("/Connection/Type");
          String className = implementation.getText();//XmlDom4JHelper.getNodeText("/Connection/Implementation", doc);
          if (className != null) {
            Connection connection = connectionFromClass(className);
            if (connection != null) {
              String connectionType = type.getText();//XmlDom4JHelper.getNodeText("/Connection/Type", doc);
              ConnectionType ct = ConnectionType.valueOf(connectionType);
              connectionPool.put(connection.getClass().toString(), new ConnectionInfo(ct, connection.getClass()));
            }
          }
        } catch (Exception e) {
          logger.error(e);
        } finally {
          if (bais != null)
            try {
              bais.close();
            } catch (IOException ioe) {}
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
