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

package pt.webdetails.cda.settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoader;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IReadAccess;

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

  public static final String DATA_ACCESS_PACKAGE = "pt.webdetails.cda.dataaccess";
  private static final Log logger = LogFactory.getLog(SettingsManager.class);
  private static SettingsManager _instance;
  private LRUMap settingsCache;
  private Map<String, Long> settingsTimestamps;
  private ResourceLoader defaultResourceLoader;

  private static final int MAX_SETTINGS_CACHE_SIZE = 50;

  /**
   * This class controls how the different .cda files will be read
   * and cached.
   */
  public SettingsManager( ResourceLoader defaultResourceLoader ) {

    // TODO - Read the cache size from disk. Eventually move to ehcache, if necessary

    logger.debug("Initializing SettingsManager.");

    settingsCache = new LRUMap(MAX_SETTINGS_CACHE_SIZE);
    settingsTimestamps = new HashMap<String, Long>();
    this.defaultResourceLoader = defaultResourceLoader;
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
  public synchronized CdaSettings parseSettingsFile(final String id)
    throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException, AccessDeniedException {

    //TODO: check permissions!

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
      final ResourceManager resourceManager = getResourceManager();
      final ResourceKey key = this.defaultResourceLoader.createKey( id, null );
      final Resource resource = resourceManager.create(key, null, org.w3c.dom.Document.class);
      final org.w3c.dom.Document document = (org.w3c.dom.Document) resource.getResource();
      final DOMReader saxReader = new DOMReader();
      final Document doc = saxReader.read(document);

      final CdaSettings settings = new CdaSettings(doc, id, resource.getSource());
      addToCache(settings);
      
      return settings;
    } catch (ResourceException re) {
      throw new UnsupportedDataAccessException(re.getMessage(), re);
    }

  }

  public ResourceManager getResourceManager() {
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    resourceManager.registerLoader( this.defaultResourceLoader );
    return resourceManager;
  }

  /**
   * Returns time of last save if id is a file.
   * @param id CdaSettings ID
   * @param savedFileTime 
   * @return null if not a file
   */
  private long getLastSaveTime(final String id) {
    //check if it's a saved file and get its timestamp
    return getRepositoryAccess().getLastModified(id);
  }
  
  private static IReadAccess getRepositoryAccess() {
    return PluginEnvironment.env().getContentAccessFactory().getUserContentAccess("/");
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

  /**
   * Use CdaEngine
   */
  public static synchronized SettingsManager getInstance() {

    if (_instance == null) {
      _instance = new SettingsManager( new CdaResourceLoader( getRepositoryAccess() ));
    }

    return _instance;
  }

  public DataAccessConnectionDescriptor[] getDataAccessDescriptors(boolean refreshCache) {
      
    ArrayList<DataAccessConnectionDescriptor> descriptors = new ArrayList<DataAccessConnectionDescriptor>();
    // First we need a list of all the data accesses. We're getting that from a .properties file, as a comma-separated array.
    
    
    Properties components = CdaEngine.getEnvironment().getCdaComponents();
    String[] dataAccesses = StringUtils.split(StringUtils.defaultString(components.getProperty("dataAccesses")), ",");

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
