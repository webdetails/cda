/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository.IContentItem;

import pt.webdetails.cda.cache.CacheScheduleManager;
import pt.webdetails.cda.cache.monitor.CacheMonitorHandler;
import pt.webdetails.cda.dataaccess.AbstractDataAccess; 
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Audited;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;


public class CdaContentGenerator extends SimpleContentGenerator
{

  private static Log logger = LogFactory.getLog(CdaContentGenerator.class);
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String EDITOR_SOURCE = "/editor/editor.html";
  private static final String EXT_EDITOR_SOURCE = "/editor/editor-cde.html";
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  private static final String CACHE_MANAGER_PATH = "system/" + PLUGIN_NAME + "/cachemanager/cache.html";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";
  public static final String ENCODING = "UTF-8";
  
  
  @Override
  public void createContent() throws Exception {
    final OutputStream out;
    final IContentItem contentItem;
    final IParameterProvider pathParams;
    
    CdaUtils utils = new CdaUtils();
    utils.previewQuery(getResponse(), getRequest());
  }

  public void syncronize(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    throw new UnsupportedOperationException("Feature not implemented yet");
//    final SyncronizeCdfStructure syncCdfStructure = new SyncronizeCdfStructure();
//    syncCdfStructure.syncronize(userSession, out, pathParams);
  }


  @Override
  public Log getLogger()
  {
    return logger;
  }

  @Override
  public String getPluginName() {
    return "cda";
  }
}
