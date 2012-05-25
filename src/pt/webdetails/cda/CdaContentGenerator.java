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

import java.util.UUID;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
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
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cpf.audit.CpfAuditHelper;
import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;


public class CdaContentGenerator extends BaseContentGenerator
{

  private static Log logger = LogFactory.getLog(CdaContentGenerator.class);
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String MIME_HTML = "text/xml";
  private static final String MIME_CSS = "text/css";
  private static final String MIME_JS = "text/javascript";
  private static final String MIME_JSON = "application/json";
  private static final String EDITOR_SOURCE = "/editor/editor.html";
  private static final String EXT_EDITOR_SOURCE = "/editor/editor-cde.html";
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  private static final String CACHEMAN_SOURCE = "/cachemanager/cache.html";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";
  private static final String ENCODING = "UTF-8";

  public CdaContentGenerator()
  {
  }


  private String extractMethod(final String pathString)
  {
    if (StringUtils.isEmpty(pathString))
    {
      return null;
    }
    final String pathWithoutSlash = pathString.substring(1);
    if (pathWithoutSlash.indexOf('/') > -1)
    {
      return null;
    }
    final int queryStart = pathWithoutSlash.indexOf('?');
    if (queryStart < 0)
    {
      return pathWithoutSlash;
    }
    return pathWithoutSlash.substring(0, queryStart);
  }


  @Override
  public void createContent()
  {
    HttpServletResponse response = null;
    final IParameterProvider pathParams;
    final IParameterProvider requestParams;
    final IContentItem contentItem;
    final OutputStream out;
    final String method;
    final String pathString;
    try
    {
      
      pathParams = parameterProviders.get("path");
      requestParams = parameterProviders.get("request");
      contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_HTML);
      
      // If callbacks is properly setup, we assume we're being called from another plugin
      if (this.callbacks != null && callbacks.size() > 0 && HashMap.class.isInstance(callbacks.get(0)))
      {
        HashMap<String, Object> iface = (HashMap<String, Object>) callbacks.get(0);
        out = (OutputStream) iface.get("output");
        method = (String) iface.get("method");
      }
      else
      { // if not, we handle the request normally
        out = contentItem.getOutputStream(null);
        pathString = pathParams.getStringParameter("path", null);
        method = extractMethod(pathString);
        response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (method == null)
      {
        logger.error(("DashboardDesignerContentGenerator.ERROR_001_INVALID_METHOD_EXCEPTION") + " : " + method);
        if (response != null)
        {
          response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return;
      }
      if ("doQuery".equals(method))
      {
        doQuery(requestParams, out);
      }
      else if ("previewQuery".equals(method))
      {
        previewQuery(requestParams, out);
      }
      else if ("listQueries".equals(method))
      {
        listQueries(requestParams, out);
      }
      else if ("getCdaList".equals(method))
      {
        getCdaList(requestParams, out);
      }
      else if ("listParameters".equals(method))
      {
        listParameters(requestParams, out);
      }
      else if ("clearCache".equals(method))
      {
        clearCache(requestParams, out);
      }
      else if ("getCdaFile".equals(method))
      {
        getCdaFile(requestParams, out);
      }
      else if ("writeCdaFile".equals(method))
      {
        writeCdaFile(requestParams, out);
      }
      else if ("editFile".equals(method))
      {
        editFile(requestParams, out);
      }
      else if ("getJsResource".equals(method))
      {
        getJsResource(requestParams, out);
      }
      else if ("getCssResource".equals(method))
      {
        getCssResource(requestParams, out);
      }
      else if ("listDataAccessTypes".equals(method))
      {
        listDataAccessTypes(requestParams, out);
      }
      else if ("manageCache".equals(method))
      {
        manageCache(requestParams, out);
      }
      else if ("cacheController".equals(method))
      {
        cacheController(requestParams, out);
      }
      else if(StringUtils.equals(method, "cacheMonitor"))
      {
        CacheMonitorHandler.getInstance().handleCall(requestParams, out);
      }
      else
      {
        if (response != null)
        {
          response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        logger.error(("DashboardDesignerContentGenerator.ERROR_001_INVALID_METHOD_EXCEPTION") + " : " + method);
        return;
      }
    }
    catch (Exception e)
    {
      if (response != null)
      {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }

      logger.error("Failed to execute: " + Util.getExceptionDescription(e), e);
    }

  }


  public void doQuery(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    
    final long start = System.currentTimeMillis();        
    UUID uuid = CpfAuditHelper.startAudit(PLUGIN_NAME, "doQuery", getObjectName(), this.userSession, this, requestParams);       
    
        
    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String path = getRelativePath(requestParams);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = requestParams.getLongParameter("pageSize", 0);
    final long pageStart = requestParams.getLongParameter("pageStart", 0);
    final boolean paginate = "true".equals(requestParams.getStringParameter("paginateQuery", "false"));
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
    
    // Handle the query itself and its output format...
    queryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));
    queryOptions.setDataAccessId(requestParams.getStringParameter("dataAccessId", "<blank>"));
    queryOptions.setOutputIndexId(Integer.parseInt(requestParams.getStringParameter("outputIndexId", "1")));
    
    final ArrayList<String> sortBy = new ArrayList<String>();
    String[] def =
    {
    };
    for (Object obj : requestParams.getArrayParameter("sortBy", def))
    {
      if (!((String) obj).equals(""))
      {
        sortBy.add((String) obj);
      }
    }
    queryOptions.setSortBy(sortBy);


    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters
    @SuppressWarnings("unchecked")
    final Iterator<String> params = (Iterator<String>) requestParams.getParameterNames();
    while (params.hasNext())
    {
      final String param = params.next();

      if (param.startsWith(PREFIX_PARAMETER))
      {
        queryOptions.addParameter(param.substring(PREFIX_PARAMETER.length()), requestParams.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        queryOptions.addSetting(param.substring(PREFIX_SETTING.length()), requestParams.getStringParameter(param, ""));
      }
    }

    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    String mimeType = exporter.getMimeType();
    String attachmentName = exporter.getAttachmentName();

    if (this.parameterProviders != null)
    {
      setResponseHeaders(mimeType, attachmentName);
    }

    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);
    CpfAuditHelper.endAudit(PLUGIN_NAME,"doQuery", getObjectName(), this.userSession, this, start, uuid, System.currentTimeMillis());    

  }


  private void setResponseHeaders(final String mimeType, final String attachmentName)
  {
    // Make sure we have the correct mime type
    final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    if (response == null)
    {
      return;
    }

    response.setHeader("Content-Type", mimeType);

    if (attachmentName != null)
    {
      response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
    }

    // We can't cache this request
    response.setHeader("Cache-Control", "max-age=0, no-store");
  }


  public void listQueries(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final String path = getRelativePath(requestParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);
    engine.listQueries(out, cdaSettings, discoveryOptions);
  }


  public void listParameters(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final String path = getRelativePath(requestParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));
    discoveryOptions.setDataAccessId(requestParams.getStringParameter("dataAccessId", "<blank>"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);

    engine.listParameters(out, cdaSettings, discoveryOptions);
  }


  public void getCdaFile(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    String document = getResourceAsString(StringUtils.replace(getRelativePath(requestParams), "///", "/"), FileAccess.READ);// ISolutionRepository.ACTION_UPDATE);//TODO:check
    setResponseHeaders("text/plain", null);
    out.write(document.getBytes(ENCODING));
  }


  private void writeCdaFile(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    //TODO: Validate the filename in some way, shape or form!

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(requestParams);

    if (repository.canWrite(path))
    { 
      switch(repository.publishFile(path, ((String) requestParams.getParameter("data")).getBytes(ENCODING), true)){
        case OK:
          SettingsManager.getInstance().clearCache();
          setResponseHeaders("text/plain", null);
          out.write("File saved".getBytes());
          break;
        case FAIL:
          setResponseHeaders("text/plain", null);
          out.write("Save unsuccessful!".getBytes());
          logger.error("writeCdaFile: saving " + path);
          break;
      }
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }


  public void getCdaList(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(requestParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);
    engine.getCdaList(out, discoveryOptions, userSession);
  }


  public void clearCache(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    setResponseHeaders("text/plain", null);
    out.write("Cache cleared".getBytes());
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


  public void editFile(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(requestParams);
    if (repository.canWrite(path))
    {
      boolean hasCde = repository.resourceExists("system/pentaho-cdf-dd");
      
      final String editorPath = "system/" + PLUGIN_NAME + (hasCde? EXT_EDITOR_SOURCE : EDITOR_SOURCE);
      setResponseHeaders("text/html", null);
      out.write(getResourceAsString(editorPath,FileAccess.EXECUTE).getBytes(ENCODING));
    }
    else
    {
      setResponseHeaders("text/plain", null);
      out.write("Access Denied".getBytes());
    }


  }


  public void previewQuery(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    setResponseHeaders("text/html", null);
    out.write(getResourceAsString(previewerPath, FileAccess.READ).getBytes(ENCODING));
  }


  public void getCssResource(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
    if (mimeTypeListener != null)
    {
      mimeTypeListener.setMimeType(MIME_CSS);
    }
    getresource(requestParams, out);
  }


  public void getJsResource(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
    if (mimeTypeListener != null)
    {
      mimeTypeListener.setMimeType(MIME_JS);
    }
    getresource(requestParams, out);
  }


  public void getresource(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    String resource = requestParams.getStringParameter("resource", null);
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
  

  public void listDataAccessTypes(final IParameterProvider requestParameters, final OutputStream out) throws Exception
  {
    boolean refreshCache = Boolean.parseBoolean(requestParameters.getStringParameter("refreshCache", "false"));
    
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors(refreshCache);
    setResponseHeaders(MIME_JSON, null);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
        output.append(datum.toJSON() + ",\n");
      }
      out.write(output.toString().replaceAll(",\n\\z", "\n}").getBytes(ENCODING));
    }
  }


  private void cacheController(IParameterProvider requestParams, OutputStream out)
  {
    CacheScheduleManager.getInstance().handleCall(requestParams, out);
  }


  public void manageCache(final IParameterProvider requestParams, final OutputStream out) throws Exception
  {
    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);
    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(requestParams);
    if (repository.hasAccess(path, FileAccess.EDIT))
    {
      final String cachemanPath = "system/" + PLUGIN_NAME + CACHEMAN_SOURCE;
      setResponseHeaders("text/html", null);
      out.write(getResourceAsString(cachemanPath, FileAccess.EDIT).getBytes(ENCODING));
    }
    else
    {
      setResponseHeaders("text/plain", null);
      out.write("Access Denied".getBytes());
    }
  }
}
