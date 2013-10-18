/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.connections;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
/**
 *
 * @author pdpi
 */
public class ConnectionCatalog {

  public enum ConnectionType {

    SQL, MQL, MDX, OLAP4J, SCRIPTING, NONE, XPATH, KETTLE
  };
  
  private static final String CONN_PATH = "resources/components/connections";
  
  private static ConnectionCatalog _instance;
  private static Log logger = LogFactory.getLog(ConnectionCatalog.class);
  private HashMap<String, ConnectionInfo> connectionPool;

  public ConnectionCatalog() {
    getConnections();
  }

  private void getConnections() {
    connectionPool = new HashMap<String, ConnectionInfo>();

    IReadAccess connectionsReader = CdaEngine.getRepo().getPluginSystemReader(CONN_PATH);
    List<IBasicFile> files = connectionsReader.listFiles("", RepositoryHelper.getSimpleExtensionFilter("xml"));
    if (files != null && files.size() > 0) {
      for (IBasicFile file : files) {
        InputStream in = null;
        try {
          in = file.getContents();
          SAXReader reader = new SAXReader();
          Document doc = reader.read(in);
          // To figure out whether the component is generic or has a special implementation,
          // we directly look for the class override in the definition
          Node implementation = doc.selectSingleNode("/Connection/Implementation");
          Node type = doc.selectSingleNode("/Connection/Type");
          String className = implementation.getText();
          if (className != null) {
            Connection connection = connectionFromClass(className);
            if (connection != null) {
              String connectionType = type.getText();
              ConnectionType ct = ConnectionType.valueOf(connectionType);
              connectionPool.put(connection.getClass().toString(), new ConnectionInfo(ct, connection.getClass()));
            }
          }
        } catch (Exception e) {
          logger.error(e);
        } finally {
          IOUtils.closeQuietly( in );
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
