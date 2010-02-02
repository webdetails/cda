package pt.webdetails.cda.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import java.util.HashMap;

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

  private Document doc;

  private HashMap<String, ConnectionSettings> connectionsMap;
  private HashMap<String, DataAccessSettings> dataAccessMap;


  public CdaSettings(final Document doc) {
    this.doc = doc;

    parseDocument(doc);


  }

  private void parseDocument(final Document doc) {

    logger.debug("Creating CdaSettings - parsing document");

    connectionsMap = new HashMap<String, ConnectionSettings>();
    dataAccessMap = new HashMap<String, DataAccessSettings>();


  }


  private void addConnection(final ConnectionSettings connectionSettings) {

    connectionsMap.put(connectionSettings.getId(), connectionSettings);

  }

  private void addDataAccess(final DataAccessSettings dataAccessSettings) {

    dataAccessMap.put(dataAccessSettings.getId(), dataAccessSettings);

  }

  public ConnectionSettings getConnection(final String id) throws UnknownConnectionException {

    if (!connectionsMap.containsKey(id)){
      throw new UnknownConnectionException("Unknown connection with id " + id);
    }
    return connectionsMap.get(id);
  }

    public DataAccessSettings getDataAccess(final String id) throws UnknownDataAccessException {

    if (!connectionsMap.containsKey(id)){
      throw new UnknownDataAccessException("Unknown dataAccess with id " + id);
    }
    return dataAccessMap.get(id);
  }

}
