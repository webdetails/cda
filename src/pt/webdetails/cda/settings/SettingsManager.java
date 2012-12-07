package pt.webdetails.cda.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.UnsupportedOperationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.platform.plugin.RepositoryResourceLoader;
import java.text.MessageFormat;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * This file is responsible to build / keep the different cda settings.
 * <p/>
 * Works mostly with inputStreams
 * <p/>
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:40:12 PM
 */
public class SettingsManager {

  // TODO: These are defined in 
  // org.pentaho.reporting.platform.plugin.RepositoryResourceLoader
  // we should see if there is a way to have plugins use other plugin classes
  public static final String SOLUTION_SCHEMA_NAME = RepositoryResourceLoader.SOLUTION_SCHEMA_NAME; //$NON-NLS-1$
  public static final String SCHEMA_SEPARATOR = RepositoryResourceLoader.SCHEMA_SEPARATOR; //$NON-NLS-1$
  public static final String DATA_ACCESS_PACKAGE = "pt.webdetails.cda.dataaccess";
  private static final Log logger = LogFactory.getLog(SettingsManager.class);
  private static SettingsManager _instance;
  private LRUMap settingsCache;
  private Map<String, Long> settingsTimestamps;
  
  private static final int MAX_SETTINGS_CACHE_SIZE = 50;

  /**
   * This class controls how the different .cda files will be read
   * and cached.
   */
  public SettingsManager() {

    // TODO - Read the cache size from disk. Eventually move to ehcache, if necessary

    logger.debug("Initializing SettingsManager.");

    settingsCache = new LRUMap(MAX_SETTINGS_CACHE_SIZE);
    settingsTimestamps = new HashMap<String, Long>();

  }

  /**
   * @param id The identifier for this settings file (path to file).
   * @return
   * @throws pt.webdetails.cda.dataaccess.UnsupportedDataAccessException
   *
   * @throws org.dom4j.DocumentException
   * @throws pt.webdetails.cda.connections.UnsupportedConnectionException
   *
   */
  public synchronized CdaSettings parseSettingsFile(final String id) throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException {

    // Do we have this on cache?

    if (settingsCache.containsKey(id)) {
      CdaSettings cachedCda = (CdaSettings) settingsCache.get(id);
      
      if(!settingsTimestamps.containsKey(id)){
       //something went very wrong
        logger.error(MessageFormat.format("No cache timestamp found for item {0}, cache bypassed.", id));
      }
      else {
        // Is cache up to date?
        long cachedTime = settingsTimestamps.get(id);
        Long savedFileTime = getLastSaveTime(id);
        
        if (savedFileTime != null && //don't cache on-the-fly items 
            savedFileTime <= cachedTime){
          // Up-to-date, use cache
          return cachedCda; 
        }
      }
    }

    try {
      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      // add the runtime context so that PentahoResourceData class can get access
      // to the solution repo
      final ResourceKey key;

      final HashMap<String, Object> helperObjects = new HashMap<String, Object>();
      key = resourceManager.createKey(SOLUTION_SCHEMA_NAME + SCHEMA_SEPARATOR + id, helperObjects);
      
      RepositoryAccess repositoryAccess = RepositoryAccess.getRepository();
      
      
      //final Resource resource = resourceManager.create(key, null, org.w3c.dom.Document.class);
      /*final org.w3c.dom.Document document = (org.w3c.dom.Document) resource.getResource();
      final DOMReader saxReader = new DOMReader();*/
      final Document doc = repositoryAccess.getResourceAsDocument(id);//saxReader.read(document);

      final CdaSettings settings = new CdaSettings(doc, id, key);//resource.getSource());
      addToCache(settings);
      
      return settings;
      
    } catch (ResourceException re) {
      throw new UnsupportedDataAccessException("Failed: ResourceException", re);
    } catch (IOException e){
      logger.debug("Error while getting repository file "+ e);
      return null;
    }

  }

  /**
   * Returns time of last save if id is a file.
   * @param id CdaSettings ID
   * @param savedFileTime 
   * @return null if not a file
   */
  private Long getLastSaveTime(final String id) {
    //check if it's a saved file and get its timestamp
    if(CdaEngine.getInstance().isStandalone()) {
      File cdaFile = new File(id);
      if(cdaFile.exists()){
        return cdaFile.lastModified();
      }
    }
    else {
      RepositoryFile savedCda = RepositoryAccess.getRepository().getRepositoryFile(id, FileAccess.NONE);
      if(savedCda != null) return savedCda.getLastModifiedDate().getTime();
    }
    return null;
  }
  
  /**
   * (use in synchronized methods)
   * @param settings
   */
  private void addToCache(CdaSettings settings){
    String id = settings.getId();
    settingsCache.put(id, settings);
    settingsTimestamps.put(id, System.currentTimeMillis());
  }

  /**
   * Forces removal of settings file from cache. This method must be called
   * when we update the .cda file
   *
   * @param id
   */
  public synchronized void clearEntryFromCache(final String id) {

    if (settingsCache.containsKey(id)) {
      settingsCache.remove(id);
    }
    if(settingsTimestamps.containsKey(id)){
      settingsTimestamps.remove(id);
    }
  }

  public synchronized void clearCache() {

    logger.info("Cleaning CDA settings cache");
    settingsCache.clear();
    settingsTimestamps.clear();
  }

  public static synchronized SettingsManager getInstance() {

    if (_instance == null) {
      _instance = new SettingsManager();
    }

    return _instance;
  }

  public DataAccessConnectionDescriptor[] getDataAccessDescriptors(boolean refreshCache) throws Exception {

    ArrayList<DataAccessConnectionDescriptor> descriptors = new ArrayList<DataAccessConnectionDescriptor>();
    // First we need a list of all the data accesses. We're getting that from a .properties file, as a comma-separated array.
    final File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/cda/resources/components.properties"));
    final Properties resources = new Properties();
    
    FileInputStream fin = null;
    try{
      fin = new FileInputStream(file);
      resources.load(fin);
    }
    finally {
      IOUtils.closeQuietly(fin);
    }
    String[] dataAccesses = StringUtils.split(StringUtils.defaultString(resources.getProperty("dataAccesses")), ",");

    // We apply some sanity checks to the dataAccesses listed:
    //    1. It can't be abstract,
    //    2. It must inherit from AbstractDataAccess
    // For any class that passes those tests, we get its getDataAccessDescripts() method, and use it to get a description.
    for (String dataAccess : dataAccesses) {
      
      Class<?> clazz = null;
      String className = DATA_ACCESS_PACKAGE + '.' + dataAccess;
      try {
        clazz = Class.forName(className);
      } catch (Exception e) {
        logger.error(MessageFormat.format("Couldn\'t load class {0}!", className));
        continue;
      }
      
      if (Modifier.isAbstract(clazz.getModifiers())) {
        logger.debug(dataAccess + " is abstract: Skipping");
      } else if (AbstractDataAccess.class.isAssignableFrom(clazz)) {
        try {
          @SuppressWarnings("unchecked")
          DataAccessConnectionDescriptor[] descriptor = DataAccessConnectionDescriptor.fromClass((Class<? extends AbstractDataAccess>) clazz);
          descriptors.addAll(Arrays.asList(descriptor));
        } catch (InvocationTargetException e) {
          Throwable cause = e.getTargetException();
          if (cause.getClass() == UnsupportedOperationException.class) {
            logger.warn("DataAccess " + dataAccess + " doesn't support discoverability!");
          } else {
            logger.error("DataAccess " + dataAccess + " did something wrong!");
          }
        } catch (Exception e) {
          logger.error("DataAccess " + dataAccess + " did something wrong!");
        }
      }

    }
    return descriptors.toArray(new DataAccessConnectionDescriptor[descriptors.size()]);
  }
}
