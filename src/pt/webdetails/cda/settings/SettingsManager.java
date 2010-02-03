package pt.webdetails.cda.settings;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;

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
  public synchronized CdaSettings parseSettingsFile(final String id, final InputStream in) throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException {

    // Do we have this on cache?

    if (settingsCache.containsKey(id)){
      return (CdaSettings) settingsCache.get(id);
    }

    final SAXReader saxReader = new SAXReader();
    final Document doc = saxReader.read(in);


    final CdaSettings settings = new CdaSettings(doc, id);

    return settings;


  }

  /**
   * Forces removal of settings file from cache. This method must be called
   * when we update the .cda file
   *
   * @param id
   */

  public synchronized void clearEntryFromCache(final String id){

    if(settingsCache.containsKey(id)){
      settingsCache.remove(id);
    }

  }


  public static synchronized SettingsManager getInstance() {

    if (_instance == null)
      _instance = new SettingsManager();

    return _instance;
  }

}
