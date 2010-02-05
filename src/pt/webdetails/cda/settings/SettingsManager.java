package pt.webdetails.cda.settings;

import java.io.File;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;

/**
 * This file is responsible to build / keep the different cda settings.
 * <p/>
 * Works mostly with inputStreams
 * <p/>
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:40:12 PM
 */
public class SettingsManager
{


  private static final Log logger = LogFactory.getLog(SettingsManager.class);
  private static SettingsManager _instance;

  private LRUMap settingsCache;

  /**
   * This class controls how the different .cda files will be read
   * and cached.
   */
  public SettingsManager()
  {

    // TODO - Read the cache size from disk. Eventually move to ehcache, if necessary

    logger.debug("Initializing SettingsManager.");

    settingsCache = new LRUMap(50);

  }


  /**
   * @param id The identifier for this settings file.
   * @throws pt.webdetails.cda.dataaccess.UnsupportedDataAccessException
   * @throws org.dom4j.DocumentException
   * @throws pt.webdetails.cda.connections.UnsupportedConnectionException
   * @return
   */
  public synchronized CdaSettings parseSettingsFile(final String id) throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException
  {

    // Do we have this on cache?

    if (settingsCache.containsKey(id))
    {
      return (CdaSettings) settingsCache.get(id);
    }

    try
    {
      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final Resource resource = resourceManager.createDirectly(new File(id), org.w3c.dom.Document.class);
      final org.w3c.dom.Document document = (org.w3c.dom.Document) resource.getResource();
      final DOMReader saxReader = new DOMReader();
      final Document doc = saxReader.read(document);

      final CdaSettings settings = new CdaSettings(doc, id, resource.getSource());
      settingsCache.put(id, settings);
      return settings;
    }
    catch (ResourceException re)
    {
      throw new UnsupportedDataAccessException("Failed: ResourceException", re);
    }

  }

  /**
   * Forces removal of settings file from cache. This method must be called
   * when we update the .cda file
   *
   * @param id
   */

  public synchronized void clearEntryFromCache(final String id)
  {

    if (settingsCache.containsKey(id))
    {
      settingsCache.remove(id);
    }

  }


  public static synchronized SettingsManager getInstance()
  {

    if (_instance == null)
    {
      _instance = new SettingsManager();
    }

    return _instance;
  }

}
