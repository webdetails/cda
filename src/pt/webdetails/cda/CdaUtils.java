/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IPluginManager;

import org.hibernate.Session;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cda.utils.PluginHibernateUtil;

@Path("/cda/api")
public class CdaUtils {
  private static final Log logger = LogFactory.getLog(CdaUtils.class);
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String EDITOR_SOURCE = "/editor/editor.html";
  private static final String EXT_EDITOR_SOURCE = "/editor/editor-cde.html";
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  private static final String CACHE_MANAGER_PATH = "/cachemanager/cache.html";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";
  public static final String ENCODING = "UTF-8";
  
  
  public CdaUtils() {

  }
  
  protected static String getEncoding() { return ENCODING; }
  
  @GET
  @Path("/doQuery")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void doQuery(@QueryParam("path") String path, 
                      @QueryParam("solution") String solution, 
                      @QueryParam("file") String file,
                      @DefaultValue("json") @QueryParam("outputType") String outputType, 
                      @DefaultValue("1") @QueryParam("outputIndexId") int outputIndexId, 
                      @DefaultValue("<blank>") @QueryParam("dataAccessId") String dataAccessId, 
                      @DefaultValue("false") @QueryParam("bypassCache") Boolean bypassCache, 
                      @DefaultValue("false") @QueryParam("paginateQuery") Boolean paginateQuery, 
                      @DefaultValue("0") @QueryParam("pageSize") int pageSize,
                      @DefaultValue("0") @QueryParam("pageStart") int pageStart, 
                      @DefaultValue("false") @QueryParam("wrapItUp") Boolean wrapItUp, 
                      @QueryParam("sortBy") List<String> sortBy, 
                      
                      @Context HttpServletResponse servletResponse, 
                      @Context HttpServletRequest servletRequest) throws Exception
  {
    //final IParameterProvider requestParams = getRequestParameters();
        
    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    //final String filePath = getRelativePath(solution, path, file);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    if (pageSize > 0 || pageStart > 0 || paginateQuery)
    {
      if (pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE)
      {
        throw new ArithmeticException("Paging values too large");
      }
      queryOptions.setPaginate(true);
      queryOptions.setPageSize(pageSize > 0 ? (int) pageSize : paginateQuery ? DEFAULT_PAGE_SIZE : 0);
      queryOptions.setPageStart(pageStart > 0 ? (int) pageStart : paginateQuery ? DEFAULT_START_PAGE : 0);
    }
    
    // Support for bypassCache (we'll maintain the name we use in CDE
    queryOptions.setCacheBypass(bypassCache);
    
    // Handle the query itself and its output format...
    queryOptions.setOutputType(outputType);
    queryOptions.setDataAccessId(dataAccessId);
    
    try {
      queryOptions.setOutputIndexId(outputIndexId);
    } catch (NumberFormatException e) {
      logger.error("Illegal outputIndexId '" + outputIndexId + "'" );
    }
    
    final ArrayList<String> sort = new ArrayList<String>();    
    for (String string : sortBy)
    {
      if (!((String) string).equals(""))
      {
        sort.add((String) string);
      }
    }
    queryOptions.setSortBy(sort);
    

    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters
    @SuppressWarnings("unchecked")
    final Enumeration enumeration = servletRequest.getParameterNames();
    while (enumeration.hasMoreElements())
    {
      final String param = (String)enumeration.nextElement();

      if (param.startsWith(PREFIX_PARAMETER))
      {
        queryOptions.addParameter(param.substring(PREFIX_PARAMETER.length()), servletRequest.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        String value = servletRequest.getParameter(param);
        if (value == null ){
          value = "";
        }
        queryOptions.addSetting(param.substring(PREFIX_SETTING.length()), value);
      }
    }

    
    if(wrapItUp) {
      String uuid = engine.wrapQuery(servletResponse.getOutputStream(), cdaSettings, queryOptions);
      logger.debug("doQuery: query wrapped as " + uuid);
      writeOut(servletResponse.getOutputStream(), uuid);
      return;
    }
    

    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    
    String attachmentName = exporter.getAttachmentName();
    String mimeType = mimeType = exporter.getMimeType();
    
    if(mimeType != null){
      servletResponse.setHeader("Content-Type", mimeType);
    }
     
    if(attachmentName != null){
      servletResponse.setHeader("content-disposition", "attachment; filename=" + attachmentName);
    }
      

    // Finally, pass the query to the engine
    engine.doQuery(servletResponse.getOutputStream(), cdaSettings, queryOptions);

  }
  
  @GET
  @Path("/unwrapQuery")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void unwrapQuery(@QueryParam("path") String path, 
                          @QueryParam("solution") String solution, 
                          @QueryParam("file") String file,
                          @QueryParam("uuid") String uuid,
                          @Context HttpServletResponse servletResponse, 
                          @Context HttpServletRequest servletRequest) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    //final String filePath = getRelativePath(path, solution, file);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    QueryOptions queryOptions = engine.unwrapQuery(uuid);
    if(queryOptions != null) {
      Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
      
      String attachmentName = exporter.getAttachmentName();
      String mimeType = mimeType = exporter.getMimeType();
      
      if(mimeType != null){
        servletResponse.setHeader("Content-Type", mimeType);
      }
    
      if(attachmentName != null){
        servletResponse.setHeader("content-disposition", "attachment; filename=" + attachmentName);
      }
        
      engine.doQuery(servletResponse.getOutputStream(), cdaSettings, queryOptions);
    }
    else {
      logger.error("unwrapQuery: uuid " + uuid + " not found.");
    }
    
  }
  
  @GET
  @Path("/listQueries")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void listQueries(@QueryParam("path") String path, 
                          @QueryParam("solution") String solution, 
                          @QueryParam("file") String file,
                          @DefaultValue("json") @QueryParam("outputType") String outputType, 
                          
                          @Context HttpServletResponse servletResponse, 
                          @Context HttpServletRequest servletRequest) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    //final String filePath = getRelativePath(path, solution, file);
    if(StringUtils.isEmpty(path)){
      throw new IllegalArgumentException("No path provided");
    }
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    if(mimeType != null) {
      servletResponse.setHeader("Content-Type", mimeType);
    }
      
    engine.listQueries(servletResponse.getOutputStream(), cdaSettings, discoveryOptions);
  }
  
  
  @GET
  @Path("/listParameters")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void listParameters(@QueryParam("path") String path, 
                             @QueryParam("solution") String solution, 
                              @QueryParam("file") String file,
                              @DefaultValue("json") @QueryParam("outputType") String outputType, 
                              @DefaultValue("<blank>") @QueryParam("dataAccessId") String dataAccessId, 
                            
                              @Context HttpServletResponse servletResponse, 
                              @Context HttpServletRequest servletRequest) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    //final String filePath = getRelativePath(path, solution, file);
    logger.debug("Do Query: getSolPath:" + path);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);
    discoveryOptions.setDataAccessId(dataAccessId);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    if(mimeType != null)
    {
      servletResponse.setHeader("Content-Type", mimeType);
    }

    engine.listParameters(servletResponse.getOutputStream(), cdaSettings, discoveryOptions);
  }
  
  
  @GET
  @Path("/getCdaFile")
  @Produces("text/xml")
  @Consumes({ WILDCARD })
  public void getCdaFile(@QueryParam("path") String path, 
                         @QueryParam("solution") String solution, 
                         @QueryParam("file") String file,
                            
                         @Context HttpServletResponse servletResponse, 
                         @Context HttpServletRequest servletRequest) throws Exception
  {
    String document = RepositoryAccess.getRepository().getResourceAsString(StringUtils.replace(path, "///", "/"), FileAccess.READ);
    writeOut(servletResponse.getOutputStream(), document);
  }
  
  
  @POST
  @Path("/writeCdaFile")
  @Produces("text/plain")
  @Consumes({ WILDCARD })
  public void writeCdaFile(@FormParam("path") String path, 
                          @FormParam("solution") String solution, 
                          @FormParam("file") String file,
                          @FormParam("data") String data,
                            
                           @Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception
  {  
    //TODO: Validate the filename in some way, shape or form!
    RepositoryAccess repository = RepositoryAccess.getRepository(getPentahoSession());
    // Check if the file exists and we have permissions to write to it
    
    if(data == null){
      data = repository.getResourceAsString(path, FileAccess.READ);
      
      //writeOut(servletResponse.getOutputStream(), "Save unsuccessful!");
      //logger.error("writeCdaFile: no data to save provided " + path);
      //return;
    }

    //if (repository.canWrite(path))
    //{ 
      switch(repository.publishFile(path, data.getBytes(ENCODING), true)){
        case OK:
          SettingsManager.getInstance().clearCache();
          writeOut(servletResponse.getOutputStream(), "File saved.");
          break;
        case FAIL:
          writeOut(servletResponse.getOutputStream(), "Save unsuccessful!");
          logger.error("writeCdaFile: saving " + path);
          break;
      }
    //}
    //else
    //{
    //  throw new AccessDeniedException(path, null);
    //}
  }
  
  @GET
  @Path("/getCdaList")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCdaList(@QueryParam("path") String path, 
                         @QueryParam("solution") String solution, 
                         @QueryParam("file") String file,
                         @DefaultValue("json") @QueryParam("outputType") String outputType, 
                            
                         @Context HttpServletResponse servletResponse, 
                         @Context HttpServletRequest servletRequest) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    if(mimeType != null){
      servletResponse.setHeader("Content-Type", mimeType);
    }
      
    engine.getCdaList(servletResponse.getOutputStream(), discoveryOptions, getPentahoSession());
  }

  @GET
  @Path("/clearCache")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void clearCache(@Context HttpServletResponse servletResponse, 
                         @Context HttpServletRequest servletRequest) throws Exception
  {
    // Check if user is admin
    Boolean accessible = SecurityHelper.getInstance().isPentahoAdministrator(getPentahoSession());
    if(!accessible){
      String msg = "Method clearCache not exposed or user does not have required permissions."; 
      logger.error(msg);
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
    }
    
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    servletResponse.getOutputStream().write("Cache cleared".getBytes());
  }
  
  @GET
  @Path("/cacheMonitor")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void cacheMonitor(@Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception{
    Boolean accessible = SecurityHelper.getInstance().isPentahoAdministrator(getPentahoSession());
    if(!accessible){
      String msg = "Method cacheMonitor not exposed or user does not have required permissions."; 
      logger.error(msg);
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
    }
   
    CacheMonitorHandler.getInstance().handleCall(servletRequest, servletResponse);
  }
  
  
  @GET
  @Path("/editFile")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void editFile(@QueryParam("path") String path, 
                       @QueryParam("solution") String solution, 
                       @QueryParam("file") String file,
                       
                       @Context HttpServletResponse servletResponse, 
                       @Context HttpServletRequest servletRequest) throws Exception
  {

    RepositoryAccess repository = RepositoryAccess.getRepository(getPentahoSession());
    
    // Check if the file exists and we have permissions to write to it
    //String filePath = getRelativePath(path, solution, file);
    //if (repository.canWrite(filePath))
    //{
      IPluginResourceLoader pluginResourceLoader = PentahoSystem.get(IPluginResourceLoader.class);
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
      ClassLoader cdeClassLoader = pluginManager.getClassLoader("cde");
      
      final String editorPath;
      
      if(cdeClassLoader != null){
        editorPath = EXT_EDITOR_SOURCE;
      } else {
        editorPath = EDITOR_SOURCE;
      }
    
      ClassLoader classLoader = pluginManager.getClassLoader(PLUGIN_NAME);
      InputStream inputStream = pluginResourceLoader.getResourceAsStream(classLoader, editorPath);
      
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer);
      
      writeOut(servletResponse.getOutputStream(), writer.toString());
    //}
    /*else
    {

      servletResponse.setHeader("Content-Type", "text/plain");
      
      servletResponse.getOutputStream().write("Access Denied".getBytes());
      servletResponse.getOutputStream().flush();
    }*/
  }

  @GET
  @Path("/previewQuery")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void previewQuery(@Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception
  {
    IPluginResourceLoader pluginResourceLoader = PentahoSystem.get(IPluginResourceLoader.class);
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
    ClassLoader classLoader = pluginManager.getClassLoader(PLUGIN_NAME);
    
    
    final String previewerPath = PREVIEWER_SOURCE;
    InputStream inputStream = pluginResourceLoader.getResourceAsStream(classLoader, previewerPath);
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer);
    
    
    writeOut(servletResponse.getOutputStream(), writer.toString());
  }

  @GET
  @Path("/getCssResource")
  @Produces("text/css")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCssResource(@QueryParam("resource") String resource,
          
                             @Context HttpServletResponse servletResponse, 
                             @Context HttpServletRequest servletRequest) throws Exception
  {
    RepositoryAccess repository = RepositoryAccess.getRepository(getPentahoSession());
    InputStream in = repository.getResourceInputStream(resource, FileAccess.READ);
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(in, writer);
    
    writeOut(servletResponse.getOutputStream(), writer.toString());
  }

  @GET
  @Path("/getJsResource")
  @Produces("text/javascript")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getJsResource(@QueryParam("resource") String resource,
          
                            @Context HttpServletResponse servletResponse, 
                            @Context HttpServletRequest servletRequest) throws Exception
  {
    RepositoryAccess repository = RepositoryAccess.getRepository(getPentahoSession());
    InputStream in = repository.getResourceInputStream(resource, FileAccess.READ);
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(in, writer);
    
    writeOut(servletResponse.getOutputStream(), writer.toString());
  }
  
  @GET
  @Path("/listDataAccessTypes")
  @Produces("application/json")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void listDataAccessTypes(@DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache,
          
                                  @Context HttpServletResponse servletResponse, 
                                  @Context HttpServletRequest servletRequest) throws Exception
  {
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().getDataAccessDescriptors(refreshCache);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
        output.append(datum.toJSON() + ",\n");
      }
      writeOut(servletResponse.getOutputStream(), output.toString().replaceAll(",\n\\z", "\n}"));
    }
  }

  @GET
  @Path("/cacheController")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void cacheController(@QueryParam("method") String method,
                              @QueryParam("object") String object,
                              @QueryParam("id") String id,
  
                              @Context HttpServletResponse servletResponse, 
                              @Context HttpServletRequest servletRequest)
  {
    if(!method.isEmpty() && method != null){
      CacheScheduleManager cacheScheduleManager = CacheScheduleManager.getInstance();
      
      String upperCaseMethod = method.toUpperCase();
      
      try {
        if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.CHANGE){
          cacheScheduleManager.change(object, servletResponse.getOutputStream());
        } else if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.RELOAD){
          cacheScheduleManager.load(id, servletResponse.getOutputStream());
        } else if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.LIST){
          cacheScheduleManager.list(servletResponse.getOutputStream());
        } else if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.EXECUTE){
          cacheScheduleManager.execute(id, servletResponse.getOutputStream());
        } else if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.DELETE){
          cacheScheduleManager.delete(id);
        } else if(CacheScheduleManager.functions.valueOf(upperCaseMethod) == CacheScheduleManager.functions.IMPORT){
          cacheScheduleManager.importQueries(object, servletResponse.getOutputStream());
        } else {
          logger.error("Method called to cache controller is unknown");
        }
      } catch(Exception ex){
        logger.error("Error while calling CacheScheduleManager method "+method, ex);
      }
      
    } else {
      logger.error("Method called to cache controller is empty");
    }
    
    
  }

  @GET
  @Path("/manageCache")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void manageCache(@Context HttpServletResponse servletResponse, 
                          @Context HttpServletRequest servletRequest) throws Exception
  {
    // Check if user is admin
    Boolean accessible = SecurityHelper.getInstance().isPentahoAdministrator(getPentahoSession());
    if(!accessible){
      String msg = "Method manageCache not exposed or user does not have required permissions."; 
      logger.error(msg);
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
    }
    
    IPluginResourceLoader pluginResourceLoader = PentahoSystem.get(IPluginResourceLoader.class);
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
    ClassLoader classLoader = pluginManager.getClassLoader(PLUGIN_NAME);
    
    final String cacheManagerPath = CACHE_MANAGER_PATH;
    InputStream inputStream = pluginResourceLoader.getResourceAsStream(classLoader, cacheManagerPath);
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer);
    writeOut(servletResponse.getOutputStream(), writer.toString());
  }

  
  
  private synchronized Session getSession() throws PluginHibernateException {
    return PluginHibernateUtil.getSession();
  }
  
  private synchronized IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }
  
  private String getRelativePath(String solution, String path, String file) throws UnsupportedEncodingException
  {
    if (StringUtils.isEmpty(solution))
    {
      return path;
    }

    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
  }
  
  protected void writeOut(OutputStream out, String contents) throws IOException {
      IOUtils.write(contents, out, getEncoding());
      out.flush();
    }


}
