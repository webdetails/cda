package pt.webdetails.cda.settings;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import pt.webdetails.cda.connections.UnsupportedConnectionException;

import java.io.InputStream;

/**
 *
 * This file is responsible to build / keep the different cda settings.
 *
 * Works mostly with inputStreams 
 *
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:40:12 PM
 */
public class SettingsManager {


  private static final Log logger = LogFactory.getLog(SettingsManager.class);
  private static SettingsManager _instance;

  private LRUMap settingsCache;

  /**
   * This class controls how the different .cda files will be read
   * and cached.
   */
  public SettingsManager() {
    
    // TODO - Read the cache size from disk. Eventually move to ehcache, if necessary

    logger.debug("Initializing SettingsManager.");

    settingsCache = new LRUMap(50);

  }


  /**
   *
   * @param id The identifier for this settings file.
   * @param in InputStream where the file is located
   */
  public CdaSettings parseSettingsFile(String id, InputStream in) throws DocumentException, UnsupportedConnectionException {

    // Do we have this on cache?

    if (settingsCache.containsKey(id)){
      return (CdaSettings) settingsCache.get(id);
    }

    final SAXReader saxReader = new SAXReader();
    Document doc = saxReader.read(in);


    CdaSettings settings = new CdaSettings(id, doc);

    return settings;


  }

  /**
   * Forces removal of settings file from cache. This method must be called
   * when we update the .cda file
   *
   * @param id
   */

  public void clearEntryFromCache(String id){

    if(settingsCache.containsKey(id)){
      settingsCache.remove(id);
    }

  }


  public static SettingsManager getInstance() {

    if (_instance == null)
      _instance = new SettingsManager();

    return _instance;
  }

}
