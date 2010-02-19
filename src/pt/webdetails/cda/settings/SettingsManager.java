package pt.webdetails.cda.settings;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.platform.plugin.RepositoryResourceLoader;
import pt.webdetails.cda.CdaEngine;
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

  // TODO: These are defined in 
  // org.pentaho.reporting.platform.plugin.RepositoryResourceLoader
  // we should see if there is a way to have plugins use other plugin classes
  public static final String SOLUTION_SCHEMA_NAME = RepositoryResourceLoader.SOLUTION_SCHEMA_NAME; //$NON-NLS-1$
  public static final String SCHEMA_SEPARATOR = RepositoryResourceLoader.SCHEMA_SEPARATOR; //$NON-NLS-1$


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
   * @return
   * @throws pt.webdetails.cda.dataaccess.UnsupportedDataAccessException
   *
   * @throws org.dom4j.DocumentException
   * @throws pt.webdetails.cda.connections.UnsupportedConnectionException
   *
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
      // add the runtime context so that PentahoResourceData class can get access
      // to the solution repo
      final ResourceKey key;
      if (CdaEngine.getInstance().isStandalone())
      {
        key = resourceManager.createKey(new File(id));
      }
      else
      {
        final HashMap helperObjects = new HashMap();
        key = resourceManager.createKey(SOLUTION_SCHEMA_NAME + SCHEMA_SEPARATOR + id, helperObjects);
      }
      final Resource resource = resourceManager.create(key, null, org.w3c.dom.Document.class);
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

  public synchronized void clearCache()
  {

    logger.info("Cleaning CDA settings cache");
    settingsCache.clear();

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
