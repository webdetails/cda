/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.cache.ICacheScheduleManager;
import pt.webdetails.cda.cache.monitor.CacheMonitorHandler;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cda.IResponseTypeHandler;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;



public class CdaCoreService 
{ 

  private static Log logger = LogFactory.getLog(CdaCoreService.class);
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
  private static final String JSONP_CALLBACK = "callback";
  public static final String ENCODING = "UTF-8";
  private IResponseTypeHandler responseHandler;

  //@Exposed(accessLevel = AccessLevel.PUBLIC)
 // @Audited(action = "doQuery")
  public CdaCoreService(){}
  public CdaCoreService(IResponseTypeHandler responseHandler){this.responseHandler = responseHandler;}
  public void setResponseHandler(IResponseTypeHandler responseHandler){this.responseHandler = responseHandler;}
  public void doQuery(final OutputStream out,DoQueryParameters parameters) throws Exception
  {        
    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String path = parameters.getPath();
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);
    
    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = parameters.getPageSize();
    final long pageStart = parameters.getPageStart();
    final boolean paginate = parameters.isPaginateQuery();
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
    
    queryOptions.setCacheBypass(parameters.isBypassCache());
    
    // Handle the query itself and its output format...
    queryOptions.setOutputType(parameters.getOutputType());
    queryOptions.setDataAccessId(parameters.getDataAccessId());
    try {
      queryOptions.setOutputIndexId(parameters.getOutputIndexId());
    } catch (NumberFormatException e) {
      logger.error("Illegal outputIndexId '" + parameters.getOutputIndexId() + "'" );
    }
    
    final ArrayList<String> sortBy = new ArrayList<String>();
    String[] def =
    {
    };
    for (Object obj : parameters.getSortBy())
    {
      if (!((String) obj).equals(""))
      {
        sortBy.add((String) obj);
      }
    }
    queryOptions.setSortBy(sortBy);


    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters and extra settings prefixed with "setting"
    @SuppressWarnings("unchecked")
    //XXX add parameters and settings to queryoptions
    final Iterator settings = parameters.getExtraSettings().entrySet().iterator();
    while (settings.hasNext())
    {
        Map.Entry<String,Object> pairs = (Map.Entry)settings.next();
      final String name = pairs.getKey();
      final Object parameter = pairs.getValue();
      queryOptions.addSetting(name, (String)parameter);
    }
    final Iterator params = parameters.getExtraParams().entrySet().iterator();
    while (params.hasNext())
    {
        
      Map.Entry<String,Object> pairs = (Map.Entry)params.next();
      final String name = pairs.getKey();
      final Object parameter = pairs.getValue();
      queryOptions.addParameter(name, parameter);

//      if(name.startsWith(PREFIX_PARAMETER)){
//          queryOptions.addParameter(name.substring(PREFIX_PARAMETER.length()), parameter);
//          logger.debug("##########"+name+"##############");
          
      }
/*
      if (param.startsWith(PREFIX_PARAMETER))
      {
        queryOptions.addParameter(param.substring(PREFIX_PARAMETER.length()), requestParams.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        queryOptions.addSetting(param.substring(PREFIX_SETTING.length()), requestParams.getStringParameter(param, ""));
      }*/
      
    

    if(parameters.isWrapItUp()) {
      String uuid = engine.wrapQuery(out, cdaSettings, queryOptions);
      logger.debug("doQuery: query wrapped as " + uuid);
      writeOut(out, uuid);
      return;
    }
    
    // we'll allow for the special "callback" param to be used, and passed as settingcallback to jsonp exports
    if (!parameters.getJsonCallback().equals("<blank>"))
    {
      queryOptions.addSetting(JSONP_CALLBACK, parameters.getJsonCallback());
    }
    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    
    String attachmentName = exporter.getAttachmentName();
    String mimeType = (attachmentName == null) ? null : getMimeType(attachmentName);
    if(StringUtils.isEmpty(mimeType)){
      mimeType = exporter.getMimeType();
    }
    
    if (parameters != null);//XXX + FIXME ==  if (this.parameterProviders != null)  
    {
      setResponseHeaders(mimeType, attachmentName);
    }
    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);

  }

  //@Exposed(accessLevel = AccessLevel.PUBLIC)
  public void unwrapQuery(final OutputStream out,
          final String path, final String solution, final String file, final String uuid)  throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    //final ICommonParameterProvider requestParams = requParam;
    final String relativePath = getRelativePath(path,solution,file);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);
    //String uuid = requestParams.getStringParameter("uuid", null);
    QueryOptions queryOptions = engine.unwrapQuery(uuid);
    if(queryOptions != null) {
      Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
      
      String attachmentName = exporter.getAttachmentName();
      String mimeType = (attachmentName == null) ? null : getMimeType(attachmentName);
      if(StringUtils.isEmpty(mimeType)){
        mimeType = exporter.getMimeType();
      }
      
      if (relativePath!=null && uuid!= null);//XXX  ==  if (this.parameterProviders != null)  
      {
        setResponseHeaders(mimeType, attachmentName);
      }
      engine.doQuery(out, cdaSettings, queryOptions);
    }
    else {
      logger.error("unwrapQuery: uuid " + uuid + " not found.");
    }
    
  }

 // @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listQueries(final OutputStream out,
          final String path,final String solution,final String file, final String outputType) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    //final ICommonParameterProvider requestParams = requParam;
    final String relativePath = getRelativePath(path,solution,file);
    if(StringUtils.isEmpty(relativePath)){
      throw new IllegalArgumentException("No path provided");
    }
    IRepositoryAccess repAccess = CdaEngine.getEnvironment().getRepositoryAccess();
    logger.debug("Do Query: getRelativePath:" + relativePath);
    logger.debug("Do Query: getSolPath:" + repAccess.getSolutionPath(relativePath));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.listQueries(out, cdaSettings, discoveryOptions);
  }

 // @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listParameters(final OutputStream out, 
          final String path, final String solution,final String file, final String outputType,final String dataAccessId) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
   // final ICommonParameterProvider requestParams = requParam;
    final String relativePath = getRelativePath(path,solution,file);
    IRepositoryAccess repAccess = CdaEngine.getEnvironment().getRepositoryAccess();
    logger.debug("Do Query: getRelativePath:" + relativePath);
    logger.debug("Do Query: getSolPath:" + repAccess.getSolutionPath(relativePath));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);
    discoveryOptions.setDataAccessId(dataAccessId);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);

    engine.listParameters(out, cdaSettings, discoveryOptions);
  }


  //@Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.XML)
  public void getCdaFile(final OutputStream out,final String path) throws Exception
  {
    String document = getResourceAsString(StringUtils.replace(path, "///", "/"), FileAccess.READ);// ISolutionRepository.ACTION_UPDATE);//TODO:check
    writeOut(out, document);
  }

 // @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.PLAIN_TEXT)
  public void writeCdaFile(final OutputStream out,final String path,final String solution, final String file, final String data ) throws Exception
  {
    //TODO: Validate the filename in some way, shape or form!
    IRepositoryAccess repository = CdaEngine.getEnvironment().getRepositoryAccess();
    //final ICommonParameterProvider requestParams = requParam;
    // Check if the file exists and we have permissions to write to it
    String relativePath = getRelativePath(path,solution,file);

    if (repository.canWrite(relativePath)&&data!=null)
    { 
      switch(repository.publishFile(relativePath, data.getBytes(ENCODING), true)){
        case OK:
          SettingsManager.getInstance().clearCache();
          writeOut(out, "File saved.");
          break;
        case FAIL:
          writeOut(out, "Save unsuccessful!");
          logger.error("writeCdaFile: saving " + relativePath);
          break;
      }
    }
    else
    {
      throw new AccessDeniedException(relativePath, null);
    }
  }

 // @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getCdaList(final OutputStream out,final String outputType) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);
	ISessionUtils sessionUtils = CdaEngine.getEnvironment().getSessionUtils();
    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.getCdaList(out, discoveryOptions, sessionUtils.getCurrentSession());
  }

 // @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
  public void clearCache(final OutputStream out) throws Exception
  {
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    out.write("Cache cleared".getBytes());
  }
  
 // @Exposed(accessLevel = AccessLevel.ADMIN)
  public void cacheMonitor(final OutputStream out,String method,ICommonParameterProvider requParam) throws Exception{//XXX ICommonParameterProvider?
              
    CacheMonitorHandler.getInstance().handleCall(method,requParam, out);
  }


  public void syncronize(final ICommonParameterProvider pathParams, final OutputStream out) throws Exception
  {
    throw new UnsupportedOperationException("Feature not implemented yet");
//    final SyncronizeCdfStructure syncCdfStructure = new SyncronizeCdfStructure();
//    syncCdfStructure.syncronize(userSession, out, pathParams);
  }


 // @Override
  public Log getLogger()
  {
    return logger;
  }


  private String getRelativePath(final String originalPath, final String solution,final String file) throws UnsupportedEncodingException
  {

    String path = URLDecoder.decode(originalPath, ENCODING);

    if (StringUtils.isEmpty(solution))
    {
      return path;
    }

    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
  }


  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file
    IRepositoryAccess repository = CdaEngine.getEnvironment().getRepositoryAccess();
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

  
  public String getResourceAsString(final String path, FileAccess access) throws IOException, AccessDeniedException{
    IRepositoryAccess repository = CdaEngine.getEnvironment().getRepositoryAccess();
    if(repository.hasAccess(path, access)){
      HashMap<String, String> keys = new HashMap<String, String>();
      //Locale locale = LocaleHelper.getLocale();
      Locale locale = Locale.getDefault(); //FIXME probably not what intended
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

  //@Exposed(accessLevel = AccessLevel.PUBLIC)
  public void editFile(final OutputStream out, final String path,final String solution, final String file) throws Exception 
  {
    IRepositoryAccess repository = CdaEngine.getEnvironment().getRepositoryAccess();
    
    // Check if the file exists and we have permissions to write to it
    String relativePath = getRelativePath(path,solution,file);
    if (repository.canWrite(relativePath))
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

  //@Exposed(accessLevel = AccessLevel.PUBLIC)
  public void previewQuery(final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    writeOut(out, getResourceAsString(previewerPath, FileAccess.EXECUTE));
  }


  //@Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.CSS)
  public void getCssResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  //@Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT)
  public void getJsResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  public void getResource(final OutputStream out, String resource) throws Exception
  {
    //String resource = requParam.getStringParameter("resource", null);
    resource = resource.startsWith("/") ? resource : "/" + resource;
    IRepositoryAccess repAccess =CdaEngine.getEnvironment().getRepositoryAccess();
    IRepositoryFile resFile = repAccess.getSettingsFile(resource, FileAccess.READ);
    if (resFile != null && resFile.exists()) {
    	out.write(resFile.getData());
  	}
  }

/*
  private void getResource(final OutputStream out, final String resource) throws IOException
  {
    IRepositoryAccess repAccess = CdaEngine.getEnvironment().getRepositoryAccess();
    final String path = repAccess.getSolutionPath("system/" + PLUGIN_NAME + resource);//PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + resource); //$NON-NLS-1$ //$NON-NLS-2$

    final File file = new File(path);
    final InputStream in = new FileInputStream(file);
    
    try{
      IOUtils.copy(in, out);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    
  }*/
  
  //@Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listDataAccessTypes(final OutputStream out,final boolean refreshCache) throws Exception
  {
    //boolean refreshCache = Boolean.parseBoolean(requParam.getStringParameter("refreshCache", "false"));
    
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors(refreshCache);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
                output.append(datum.toJSON()).append(",\n");//XXX changed from output.append(datum.toJSON() + ",\n");
      }
      writeOut(out, output.toString().replaceAll(",\n\\z", "\n}"));
    }
  }

  //@Exposed(accessLevel = AccessLevel.PUBLIC)
  public void cacheController(OutputStream out, String method, String obj)//XXX 
  {
	  if (CdaEngine.getEnvironment().supportsCacheScheduler()) {
		  ICacheScheduleManager manager = CdaEngine.getEnvironment().getCacheScheduler();
		  manager.handleCall(method, obj, out);
	  }
  }

  //@Exposed(accessLevel = AccessLevel.ADMIN)
  public void manageCache(final OutputStream out) throws Exception
  {
    writeOut(out, getResourceAsString(CACHE_MANAGER_PATH, FileAccess.EXECUTE));
  }


  public String getPluginName() {
    return "cda";
  }
  
  
  private void writeOut(OutputStream out,String uuid){//XXX needs checking, wich error to log
      
      try{
      out.write(uuid.getBytes());
      }catch(IOException e){
          logger.error("Failed to write to stream");
      }
  }
  private String getMimeType(String attachmentName){//FIXME must be done differently. cda-core --> cpf-core -/-> cpf-pentaho 
      return null;
  }
  private void setResponseHeaders(String mimeType, String attachmentName){//FIXME must be done differently. cda-core --> cpf-core -/-> cpf-pentaho 
       setResponseHeaders(mimeType, 0, attachmentName);
  }

  private void setResponseHeaders(String mimeType){
      setResponseHeaders(mimeType, 0, null);
  }
  //FIXME implement this method?
  private void setResponseHeaders(final String mimeType, final int cacheDuration, final String attachmentName)
    {
        if (responseHandler!=null)
            responseHandler.setResponseHeaders(mimeType, cacheDuration, attachmentName);
      
        
        // Make sure we have the correct mime type
      
      /*    final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
      if (mimeTypeListener != null)
      {
        mimeTypeListener.setMimeType(mimeType);
      }
      

      if (response == null)
      {
        logger.warn("Parameter 'httpresponse' not found!");
        return;
      }
        response.setHeader("Content-Type", mimeType);

      if (attachmentName != null)
      {
        response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
      } // Cache?

      if (cacheDuration > 0)
      {
        response.setHeader("Cache-Control", "max-age=" + cacheDuration);
      }
      else
      {
        response.setHeader("Cache-Control", "max-age=0, no-store");
      }
      */
    }
  
  
}
