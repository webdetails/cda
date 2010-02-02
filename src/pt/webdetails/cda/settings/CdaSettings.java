package pt.webdetails.cda.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.utils.Util;

import java.util.HashMap;
import java.util.List;

/**
 * CdaSettings class
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:41:44 PM
 */
public class CdaSettings {

  private static final Log logger = LogFactory.getLog(CdaSettings.class);

  private String id;
  private Document doc;
  private Element root;

  private HashMap<String, Connection> connectionsMap;
  private HashMap<String, DataAccessSettings> dataAccessMap;


  public CdaSettings(final Document doc) throws UnsupportedConnectionException {

    this.id = id;
    this.doc = doc;
    this.root = doc.getRootElement();

    connectionsMap = new HashMap<String, Connection>();
    dataAccessMap = new HashMap<String, DataAccessSettings>();

    
    parseDocument();


  }


  private void parseDocument() throws UnsupportedConnectionException {

    // 1 - Parse Connections
    // 2 - Parse DataAccesses

    logger.debug("Creating CdaSettings - parsing document");

    parseConnections();


  }

  private void parseConnections() throws UnsupportedConnectionException {

    final List<Element> connectionList = root.selectNodes("/CDADescriptor/DataSources/Connection");

    for (Element element : connectionList) {

      String id = element.attributeValue("id");
      String type = element.attributeValue("type");

      // Initialize this ConnectionType

      try {

        final String className = "pt.webdetails.cda.connections." +
            type.substring(0, 1).toUpperCase() + type.substring(1, type.length()) + "Connection";

        Class clazz = null;
        clazz = Class.forName(className);

        final Class[] params = {Element.class};
        clazz.getConstructor(params).newInstance(new Object[]{element});

      } catch (Exception e) {
        throw new UnsupportedConnectionException("Error initializing connection class: " + Util.getExceptionDescription(e),e);
      }


    }

  }


  private void addConnection(final Connection connectionSettings) {

    connectionsMap.put(connectionSettings.getId(), connectionSettings);

  }

  private void addDataAccess(final DataAccessSettings dataAccessSettings) {

    dataAccessMap.put(dataAccessSettings.getId(), dataAccessSettings);

  }

  public Connection getConnection(final String id) throws UnknownConnectionException {

    if (!connectionsMap.containsKey(id)) {
      throw new UnknownConnectionException("Unknown connection with id " + id , null);
    }
    return connectionsMap.get(id);
  }

  public DataAccessSettings getDataAccess(final String id) throws UnknownDataAccessException {

    if (!connectionsMap.containsKey(id)) {
      throw new UnknownDataAccessException("Unknown dataAccess with id " + id , null);
    }
    return dataAccessMap.get(id);
  }

}
