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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.apache.commons.lang.StringUtils;

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

import org.pentaho.platform.web.http.api.resources.AbstractJaxRSResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;


import org.hibernate.Session;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;

@Path("/cda/api/utils")
public class CdaUtils {
  private static final Log logger = LogFactory.getLog(CdaUtils.class);
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
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
  
  
  public CdaUtils() {

  }
  
  @GET
  @Path("/doQuery")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void doQuery(@QueryParam("path") String path, @QueryParam("solution") String solution, @QueryParam("fike") String file,
                      @QueryParam("outputType") String outputType, @QueryParam("dataAccessId") String dataAccessId,
                      @Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest) throws Exception
  {
    //final IParameterProvider requestParams = getRequestParameters();
        
    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String filePath = getRelativePath(solution, path, file);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = 0; //requestParams.getLongParameter("pageSize", 0);
    final long pageStart = 0; //requestParams.getLongParameter("pageStart", 0);
    final boolean paginate = false; //Boolean.parseBoolean(requestParams.getStringParameter("paginateQuery", "false"));
    if (pageSize > 0 || pageStart > 0 || paginate)
    {
      if (pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE)
      {
        throw new ArithmeticException("Paging values too large");
      }
      queryOptions.setPaginate(true);
      queryOptions.setPageSize(pageSize > 0 ? (int) pageSize : paginate ? DEFAULT_PAGE_SIZE : 0);
      queryOptions.setPageStart(pageStart > 0 ? (int) pageStart : paginate ? DEFAULT_START_PAGE : 0);
    }
    
    // Support for bypassCache (we'll maintain the name we use in CDE
    /*if(requestParams.hasParameter("bypassCache")){
      queryOptions.setCacheBypass(Boolean.parseBoolean(requestParams.getStringParameter("bypassCache","false")));
    }*/
    
    // Handle the query itself and its output format...
    if(StringUtils.isEmpty(outputType)){
      outputType = "json";
    } 
    queryOptions.setOutputType(outputType);
    
    if(StringUtils.isEmpty(dataAccessId)){
      dataAccessId = "<blank>";
    } 
    queryOptions.setDataAccessId(dataAccessId);
    
    try {
      queryOptions.setOutputIndexId(-1);//Integer.parseInt(requestParams.getStringParameter("outputIndexId", "1")));
    } catch (NumberFormatException e) {
      //logger.error("Illegal outputIndexId '" + requestParams.getStringParameter("outputIndexId", null) + "'" );
    }
    
    final ArrayList<String> sortBy = new ArrayList<String>();
    String[] def =
    {
    };
    /*for (Object obj : requestParams.getArrayParameter("sortBy", def))
    {
      if (!((String) obj).equals(""))
      {
        sortBy.add((String) obj);
      }
    }
    queryOptions.setSortBy(sortBy);
    */

    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters
    @SuppressWarnings("unchecked")
    final Iterator<String> params = (Iterator<String>) servletRequest.getParameterNames();
    while (params.hasNext())
    {
      final String param = params.next();

      if (param.startsWith(PREFIX_PARAMETER))
      {
        queryOptions.addParameter(param.substring(PREFIX_PARAMETER.length()), servletRequest.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        String value = servletRequest.getParameter(param);
        if (value == null ) value = "";
        queryOptions.addSetting(param.substring(PREFIX_SETTING.length()), value);
      }
    }

    /*
    if(httpServletRequest.getParameter("wrapItUp") != null) {
      String uuid = engine.wrapQuery(out, cdaSettings, queryOptions);
      logger.debug("doQuery: query wrapped as " + uuid);
      writeOut(out, uuid);
      return;
    }
    */
    

    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    
    String attachmentName = exporter.getAttachmentName();
    String mimeType = (attachmentName == null) ? null :null; //: getMimeType(attachmentName);
    if(StringUtils.isEmpty(mimeType)){
      mimeType = exporter.getMimeType();
    }
    
/*    if (httpServletResponse != null)
    {
      setResponseHeaders(mimeType, attachmentName);
    }*/
    
    
    // Finally, pass the query to the engine
    engine.doQuery(servletResponse.getOutputStream(), cdaSettings, queryOptions);

  }
  
  private synchronized Session getSession() throws PluginHibernateException {
    return PluginHibernateUtil.getSession();
  }
  
  private String getRelativePath(String solution, String path, String file) throws UnsupportedEncodingException
  {

    //String path = URLDecoder.decode(requestParams.getStringParameter("path", ""), ENCODING);

    //final String solution = requestParams.getStringParameter("solution", "");
    if (StringUtils.isEmpty(solution))
    {
      return path;
    }
    //final String file = requestParams.getStringParameter("file", "");

    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
  }

/*  private void initialize() throws PluginHibernateException {
    // Get hbm file
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    InputStream in = resLoader.getResourceAsStream(CdaUtils.class, "resources/hibernate/Storage.hbm.xml");

    // Close session and rebuild
    PluginHibernateUtil.closeSession();

    PluginHibernateUtil.getConfiguration().addInputStream(in);
    PluginHibernateUtil.rebuildSessionFactory();
  }
@Exposed(accessLevel = AccessLevel.PUBLIC)
  public void unwrapQuery(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    final IParameterProvider requestParams = getRequestParameters();
    final String path = getRelativePath(requestParams);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);
    String uuid = requestParams.getStringParameter("uuid", null);

    QueryOptions queryOptions = engine.unwrapQuery(uuid);
    if(queryOptions != null) {
      Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
      
      String attachmentName = exporter.getAttachmentName();
      String mimeType = (attachmentName == null) ? null : getMimeType(attachmentName);
      if(StringUtils.isEmpty(mimeType)){
        mimeType = exporter.getMimeType();
      }
      if (this.parameterProviders != null)
      {
        setResponseHeaders(mimeType, attachmentName);
      }
      engine.doQuery(out, cdaSettings, queryOptions);
    }
    else {
      logger.error("unwrapQuery: uuid " + uuid + " not found.");
    }
    
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listQueries(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final IParameterProvider requestParams = getRequestParameters();
    final String path = getRelativePath(requestParams);
    if(StringUtils.isEmpty(path)){
      throw new IllegalArgumentException("No path provided");
    }
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.listQueries(out, cdaSettings, discoveryOptions);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listParameters(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    final IParameterProvider requestParams = getRequestParameters();
    final String path = getRelativePath(requestParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));
    discoveryOptions.setDataAccessId(requestParams.getStringParameter("dataAccessId", "<blank>"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);

    engine.listParameters(out, cdaSettings, discoveryOptions);
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.XML)
  public void getCdaFile(final OutputStream out) throws Exception
  {
    String document = getResourceAsString(StringUtils.replace(getRelativePath(getRequestParameters()), "///", "/"), FileAccess.READ);// ISolutionRepository.ACTION_UPDATE);//TODO:check
    writeOut(out, document);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.PLAIN_TEXT)
  public void writeCdaFile(OutputStream out) throws Exception
  {
    //TODO: Validate the filename in some way, shape or form!

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    final IParameterProvider requestParams = getRequestParameters();
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(requestParams);

    if (repository.canWrite(path))
    { 
      switch(repository.publishFile(path, ((String) requestParams.getParameter("data")).getBytes(ENCODING), true)){
        case OK:
          SettingsManager.getInstance().clearCache();
          writeOut(out, "File saved.");
          break;
        case FAIL:
          writeOut(out, "Save unsuccessful!");
          logger.error("writeCdaFile: saving " + path);
          break;
      }
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getCdaList(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(getRequestParameters().getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.getCdaList(out, discoveryOptions, userSession);
  }

  @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
  public void clearCache(final OutputStream out) throws Exception
  {
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    out.write("Cache cleared".getBytes());
  }
  
  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void cacheMonitor(final OutputStream out){
    CacheMonitorHandler.getInstance().handleCall(getRequestParameters(), out);
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


  private String getRelativePath(final IParameterProvider requestParams) throws UnsupportedEncodingException
  {

    String path = URLDecoder.decode(requestParams.getStringParameter("path", ""), ENCODING);

    final String solution = requestParams.getStringParameter("solution", "");
    if (StringUtils.isEmpty(solution))
    {
      return path;
    }
    final String file = requestParams.getStringParameter("file", "");

    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
  }


  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);    
    String resourceContents = StringUtils.EMPTY;
    
    if (repository.resourceExists(path))
    {
      InputStream in = null;
      try
      {
        in = repository.getResourceInputStream(path, FileAccess.READ);
        resourceContents = IOUtils.toString(in);
      }
      finally 
      {
        IOUtils.closeQuietly(in);
      }
    }
    
    // Make replacement of tokens
    if (tokens != null)
    {
      for (final String key : tokens.keySet())
      { 
        resourceContents = StringUtils.replace(resourceContents, key, tokens.get(key));
      }
    }
    return resourceContents;
  }

  
  public String getResourceAsString(final String path, RepositoryAccess.FileAccess access) throws IOException, AccessDeniedException{
    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    if(repository.hasAccess(path, access)){
      HashMap<String, String> keys = new HashMap<String, String>();
      Locale locale = LocaleHelper.getLocale();
      if (logger.isDebugEnabled())
      {
        logger.debug("Current Pentaho user locale: " + locale.toString());
      }
      keys.put("#{LANGUAGE_CODE}", locale.toString());
      return getResourceAsString(path, keys);
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void editFile(final OutputStream out) throws Exception
  {

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(getRequestParameters());
    if (repository.canWrite(path))
    {
      boolean hasCde = repository.resourceExists("system/pentaho-cdf-dd");
      
      final String editorPath = "system/" + PLUGIN_NAME + (hasCde? EXT_EDITOR_SOURCE : EDITOR_SOURCE);
      writeOut(out, getResourceAsString(editorPath,FileAccess.EXECUTE));
    }
    else
    {
      setResponseHeaders("text/plain");
      out.write("Access Denied".getBytes());
    }


  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void previewQuery(final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    writeOut(out, getResourceAsString(previewerPath, FileAccess.EXECUTE));
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.CSS)
  public void getCssResource(final OutputStream out) throws Exception
  {
    getResource( out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT)
  public void getJsResource(final OutputStream out) throws Exception
  {
    getResource( out);
  }


  public void getResource(final OutputStream out) throws Exception
  {
    String resource = getRequestParameters().getStringParameter("resource", null);
    resource = resource.startsWith("/") ? resource : "/" + resource;
    getResource(out, resource);
  }


  private void getResource(final OutputStream out, final String resource) throws IOException
  {
    final String path = PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + resource); //$NON-NLS-1$ //$NON-NLS-2$

    final File file = new File(path);
    final InputStream in = new FileInputStream(file);
    
    try{
      IOUtils.copy(in, out);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    
  }
  
  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listDataAccessTypes(final OutputStream out) throws Exception
  {
    boolean refreshCache = Boolean.parseBoolean(getRequestParameters().getStringParameter("refreshCache", "false"));
    
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors(refreshCache);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
        output.append(datum.toJSON() + ",\n");
      }
      writeOut(out, output.toString().replaceAll(",\n\\z", "\n}"));
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void cacheController(OutputStream out)
  {
    CacheScheduleManager.getInstance().handleCall(getRequestParameters(), out);
  }

  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void manageCache(final OutputStream out) throws Exception
  {
    writeOut(out, getResourceAsString(CACHE_MANAGER_PATH, FileAccess.EXECUTE));
  }


  @Override
  public String getPluginName() {
    return "cda";
  }

  
  
  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listParameters(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    final IParameterProvider requestParams = getRequestParameters();
    final String path = getRelativePath(requestParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));
    discoveryOptions.setDataAccessId(requestParams.getStringParameter("dataAccessId", "<blank>"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);

    engine.listParameters(out, cdaSettings, discoveryOptions);
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.XML)
  public void getCdaFile(final OutputStream out) throws Exception
  {
    String document = getResourceAsString(StringUtils.replace(getRelativePath(getRequestParameters()), "///", "/"), FileAccess.READ);// ISolutionRepository.ACTION_UPDATE);//TODO:check
    writeOut(out, document);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.PLAIN_TEXT)
  public void writeCdaFile(OutputStream out) throws Exception
  {
    //TODO: Validate the filename in some way, shape or form!

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    final IParameterProvider requestParams = getRequestParameters();
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(requestParams);

    if (repository.canWrite(path))
    { 
      switch(repository.publishFile(path, ((String) requestParams.getParameter("data")).getBytes(ENCODING), true)){
        case OK:
          SettingsManager.getInstance().clearCache();
          writeOut(out, "File saved.");
          break;
        case FAIL:
          writeOut(out, "Save unsuccessful!");
          logger.error("writeCdaFile: saving " + path);
          break;
      }
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getCdaList(final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(getRequestParameters().getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.getCdaList(out, discoveryOptions, userSession);
  }

  @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
  public void clearCache(final OutputStream out) throws Exception
  {
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    out.write("Cache cleared".getBytes());
  }
  
  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void cacheMonitor(final OutputStream out){
    CacheMonitorHandler.getInstance().handleCall(getRequestParameters(), out);
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


  private String getRelativePath(final IParameterProvider requestParams) throws UnsupportedEncodingException
  {

    String path = URLDecoder.decode(requestParams.getStringParameter("path", ""), ENCODING);

    final String solution = requestParams.getStringParameter("solution", "");
    if (StringUtils.isEmpty(solution))
    {
      return path;
    }
    final String file = requestParams.getStringParameter("file", "");

    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
  }


  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);    
    String resourceContents = StringUtils.EMPTY;
    
    if (repository.resourceExists(path))
    {
      InputStream in = null;
      try
      {
        in = repository.getResourceInputStream(path, FileAccess.READ);
        resourceContents = IOUtils.toString(in);
      }
      finally 
      {
        IOUtils.closeQuietly(in);
      }
    }
    
    // Make replacement of tokens
    if (tokens != null)
    {
      for (final String key : tokens.keySet())
      { 
        resourceContents = StringUtils.replace(resourceContents, key, tokens.get(key));
      }
    }
    return resourceContents;
  }

  
  public String getResourceAsString(final String path, RepositoryAccess.FileAccess access) throws IOException, AccessDeniedException{
    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    if(repository.hasAccess(path, access)){
      HashMap<String, String> keys = new HashMap<String, String>();
      Locale locale = LocaleHelper.getLocale();
      if (logger.isDebugEnabled())
      {
        logger.debug("Current Pentaho user locale: " + locale.toString());
      }
      keys.put("#{LANGUAGE_CODE}", locale.toString());
      return getResourceAsString(path, keys);
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void editFile(final OutputStream out) throws Exception
  {

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(getRequestParameters());
    if (repository.canWrite(path))
    {
      boolean hasCde = repository.resourceExists("system/pentaho-cdf-dd");
      
      final String editorPath = "system/" + PLUGIN_NAME + (hasCde? EXT_EDITOR_SOURCE : EDITOR_SOURCE);
      writeOut(out, getResourceAsString(editorPath,FileAccess.EXECUTE));
    }
    else
    {
      setResponseHeaders("text/plain");
      out.write("Access Denied".getBytes());
    }


  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void previewQuery(final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    writeOut(out, getResourceAsString(previewerPath, FileAccess.EXECUTE));
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.CSS)
  public void getCssResource(final OutputStream out) throws Exception
  {
    getResource( out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT)
  public void getJsResource(final OutputStream out) throws Exception
  {
    getResource( out);
  }


  public void getResource(final OutputStream out) throws Exception
  {
    String resource = getRequestParameters().getStringParameter("resource", null);
    resource = resource.startsWith("/") ? resource : "/" + resource;
    getResource(out, resource);
  }


  private void getResource(final OutputStream out, final String resource) throws IOException
  {
    final String path = PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + resource); //$NON-NLS-1$ //$NON-NLS-2$

    final File file = new File(path);
    final InputStream in = new FileInputStream(file);
    
    try{
      IOUtils.copy(in, out);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    
  }
  
  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listDataAccessTypes(final OutputStream out) throws Exception
  {
    boolean refreshCache = Boolean.parseBoolean(getRequestParameters().getStringParameter("refreshCache", "false"));
    
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors(refreshCache);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
        output.append(datum.toJSON() + ",\n");
      }
      writeOut(out, output.toString().replaceAll(",\n\\z", "\n}"));
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void cacheController(OutputStream out)
  {
    CacheScheduleManager.getInstance().handleCall(getRequestParameters(), out);
  }

  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void manageCache(final OutputStream out) throws Exception
  {
    writeOut(out, getResourceAsString(CACHE_MANAGER_PATH, FileAccess.EXECUTE));
  }


  @Override
  public String getPluginName() {
    return "cda";
  }
*/
}
