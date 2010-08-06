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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;

import pt.webdetails.cda.cache.CacheManager;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.Util;

@SuppressWarnings("unchecked")
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
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  private static final String CACHEMAN_SOURCE = "/cachemanager/cache.html";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;


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
      // If callbacks is properly setup, we assume we're being called from another plugin
      if (this.callbacks != null && callbacks.size() > 0 && HashMap.class.isInstance(callbacks.get(0)))
      {
        HashMap<String, Object> iface = (HashMap<String, Object>) callbacks.get(0);
        pathParams = parameterProviders.get("path");
        requestParams = parameterProviders.get("request");
        contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_HTML);
        out = (OutputStream) iface.get("output");
        method = (String) iface.get("method");
      }
      else
      { // if not, we handle the request normally
        pathParams = parameterProviders.get("path");
        requestParams = parameterProviders.get("request");
        contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_HTML);
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
      else if ("synchronize".equals(method))
      {
        syncronize(requestParams, out);
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
      else if ("cacheManager".equals(method))
      {
        cacheManager(requestParams, out);
      }
      else if ("manageCache".equals(method))
      {
        manageCache(requestParams, out);
      }
      else if ("cacheController".equals(method))
      {
        cacheController(requestParams, out);
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


  public void doQuery(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String path = getRelativePath(pathParams);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);
    //set a formula context for current session
  	if(userSession != null) cdaSettings.setFormulaContext(new CdaSessionFormulaContext(userSession));

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = pathParams.getLongParameter("pageSize", 0);
    final long pageStart = pathParams.getLongParameter("pageStart", 0);
    final boolean paginate = "true".equals(pathParams.getStringParameter("paginateQuery", "false"));
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
    queryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));
    queryOptions.setDataAccessId(pathParams.getStringParameter("dataAccessId", "<blank>"));
    
//    final ArrayList<Integer> sortBy = new ArrayList<Integer>();
//    Integer[] def = {};
//    for (Object obj : pathParams.getArrayParameter("sortBy", def)) {
//      sortBy.add(Integer.parseInt((String) obj));
//    }
//    queryOptions.setSortBy(sortBy);
    if (pathParams.getStringParameter("sortBy", null) != null)
    {
      logger.warn("sortBy not implemented yet");
    }
    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters
    final Iterator<String> params = (Iterator<String>) pathParams.getParameterNames();
    while (params.hasNext())
    {
      final String param = params.next();

      if (param.startsWith("param"))
      {
        queryOptions.addParameter(param.substring(5), pathParams.getStringParameter(param, ""));
      }
      else if (param.startsWith("setting"))
      {
        queryOptions.addSetting(param.substring(7), pathParams.getStringParameter(param, ""));
      }
    }

    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    String mimeType = exporter.getMimeType();
    String attachmentName = exporter.getAttachmentName();
    setResponseHeaders(mimeType, attachmentName);


    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);

  }


  private void setResponseHeaders(final String mimeType, final String attachmentName)
  {
    // Make sure we have the correct mime type
    final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    response.setHeader("Content-Type", mimeType);

    if (attachmentName != null)
    {
      response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
    }

    // We can't cache this requests
    response.setHeader("Cache-Control", "max-age=0, no-store");
  }


  public void listQueries(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    final CdaEngine engine = CdaEngine.getInstance();

    final String path = getRelativePath(pathParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);


    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);
    engine.listQueries(out, cdaSettings, discoveryOptions);


  }


  public void listParameters(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();

    final String path = getRelativePath(pathParams);
    logger.debug("Do Query: getRelativePath:" + path);
    logger.debug("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));
    discoveryOptions.setDataAccessId(pathParams.getStringParameter("dataAccessId", "<blank>"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);

    engine.listParameters(out, cdaSettings, discoveryOptions);


  }


  public void getCdaFile(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    String document = getResourceAsString(getRelativePath(pathParams), ISolutionRepository.ACTION_UPDATE);
    setResponseHeaders("text/plain", null);
    out.write(document.getBytes("UTF-8"));
  }


  private void writeCdaFile(IParameterProvider pathParams, OutputStream out) throws Exception
  {

    //TODO: Validate the filename in some way, shape or form!
    String[] file = buildFileParameters(getRelativePath(pathParams));
    String rootDir = PentahoSystem.getApplicationContext().getSolutionPath("");

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(pathParams);

    // 1 - is it a new file?
    final boolean resourceExists = solutionRepository.resourceExists(path);

    // 2 - it already exists - let's see if we have permissions
    if (!resourceExists || solutionRepository.getSolutionFile(path, ISolutionRepository.ACTION_UPDATE) != null)
    {

      int status = solutionRepository.publish(rootDir, file[0], file[1], ((String) pathParams.getParameter("data")).getBytes("UTF-8"), true);
      if (status == ISolutionRepository.FILE_ADD_SUCCESSFUL)
      {
        SettingsManager.getInstance().clearCache();
        setResponseHeaders("text/plain", null);
        out.write("File saved".getBytes());
      }
      else
      {
        setResponseHeaders("text/plain", null);
        out.write("Save unsuccessful!".getBytes());
        logger.error("writeCdaFile: saving " + file + " returned " + new Integer(status).toString());

      }
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }


  }


  public void getCdaList(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType, null);
    engine.getCdaList(out, discoveryOptions, userSession);


  }


  public void clearCache(final IParameterProvider pathParams, final OutputStream out) throws Exception
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


  private String getRelativePath(final IParameterProvider pathParams) throws UnsupportedEncodingException
  {

    String path = URLDecoder.decode(pathParams.getStringParameter("path", ""), "UTF-8").replaceAll("//", "/");

    final String solution = pathParams.getStringParameter("solution", "");
    if (StringUtils.isEmpty(solution))
    {

      return path;
    }

    return ActionInfo.buildSolutionPath(
            solution,
            path,
            pathParams.getStringParameter("file", ""));
  }


  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath(path);
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    final StringBuilder resource = new StringBuilder();
    if (solutionRepository.resourceExists(path))
    {
      final InputStream in = solutionRepository.getResourceInputStream(path, true, ISolutionRepository.ACTION_EXECUTE);
      int c;
      while ((c = in.read()) != -1)
      {
        resource.append((char) c);
      }
      in.close();
    }
    else
    {
      resource.append(" ");
    }
    // Make replacement of tokens
    if (tokens != null)
    {

      for (final String key : tokens.keySet())
      {
        final int index = resource.indexOf(key);
        if (index != -1)
        {
          resource.replace(index, index + key.length(), tokens.get(key));
        }
      }

    }


    final String output = resource.toString();

    return output;

  }


  public String getResourceAsString(final String path, int actionOperation) throws IOException, AccessDeniedException
  {

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    // Check if the file exists and we have permissions to write to it
    if (solutionRepository.getSolutionFile(path, actionOperation) != null)
    {
      // Fill key map with locale definition
      HashMap<String, String> keys = new HashMap();
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


  public void editFile(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(pathParams);
    if (solutionRepository.getSolutionFile(path, ISolutionRepository.ACTION_UPDATE) != null)
    {

      final String editorPath = "system/" + PLUGIN_NAME + EDITOR_SOURCE;
      SettingsManager.getInstance().clearCache();
      AbstractDataAccess.clearCache();
      setResponseHeaders("text/html", null);
      out.write(getResourceAsString(editorPath, ISolutionRepository.ACTION_UPDATE).getBytes("UTF-8"));
    }
    else
    {

      setResponseHeaders("text/plain", null);
      out.write("Access Denied".getBytes());
    }


  }


  public void previewQuery(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();
    setResponseHeaders("text/html", null);
    out.write(getResourceAsString(previewerPath, ISolutionRepository.ACTION_EXECUTE).getBytes("UTF-8"));
  }


  public void getCssResource(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();


    if (mimeTypeListener != null)
    {
      mimeTypeListener.setMimeType(MIME_CSS);


    }
    getresource(pathParams, out);


  }


  public void getJsResource(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();


    if (mimeTypeListener != null)
    {
      mimeTypeListener.setMimeType(MIME_JS);


    }
    getresource(pathParams, out);


  }


  public void getresource(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    String resource = pathParams.getStringParameter("resource", null);
    resource = resource.startsWith("/") ? resource : "/" + resource;
    getResource(out, resource);

  }


  private void getResource(final OutputStream out, final String resource) throws IOException
  {


    final String path = PentahoSystem.getApplicationContext().getSolutionPath("system/" + PLUGIN_NAME + resource); //$NON-NLS-1$ //$NON-NLS-2$

    final File file = new File(path);
    final InputStream in = new FileInputStream(file);
    final byte[] buff = new byte[4096];


    int n = in.read(buff);


    while (n != -1)
    {
      out.write(buff, 0, n);
      n = in.read(buff);


    }
    in.close();


  }


  private String[] buildFileParameters(String filePath)
  {
    String[] result =
    {
      "", ""
    };
    String[] file = filePath.split("/");
    String fileName = file[file.length - 1];
    String path = filePath.substring(0, filePath.indexOf(fileName));
    result[0] = path;
    result[1] = fileName;
    return result;
  }


  public void listDataAccessTypes(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors((pathParams.getStringParameter("refreshCache", "false")).equalsIgnoreCase("true"));
    setResponseHeaders(MIME_JSON, null);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
        output.append(datum.toJSON() + ",\n");
      }
      out.write(output.toString().replaceAll(",\n\\z", "\n}").getBytes("UTF-8"));
    }
  }


  private void cacheManager(IParameterProvider requestParams, OutputStream out)
  {
    CacheManager.getInstance().render(requestParams, out);
  }


  private void cacheController(IParameterProvider requestParams, OutputStream out)
  {
    CacheManager.getInstance().handleCall(requestParams, out);
  }


  public void manageCache(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    // Check if the file exists and we have permissions to write to it
    String path = getRelativePath(pathParams);
    if (solutionRepository.getSolutionFile(path, ISolutionRepository.ACTION_UPDATE) != null)
    {

      final String cachemanPath = "system/" + PLUGIN_NAME + CACHEMAN_SOURCE;
      SettingsManager.getInstance().clearCache();
      AbstractDataAccess.clearCache();
      setResponseHeaders("text/html", null);
      out.write(getResourceAsString(cachemanPath, ISolutionRepository.ACTION_UPDATE).getBytes("UTF-8"));
    }
    else
    {

      setResponseHeaders("text/plain", null);
      out.write("Access Denied".getBytes());
    }


  }
  
  //allows access to session parameters within formulas
  private class CdaSessionFormulaContext extends DefaultFormulaContext {
  	Map<String,IParameterProvider> providers;
  	private static final String SECURITY_PREFIX = "security:";
  	private static final String SESSION_PREFIX = "session:";
  	private static final String SYSTEM_PREFIX = "system:";
  	
  	CdaSessionFormulaContext(IPentahoSession session){
  		providers = new HashMap<String, IParameterProvider>();
  		if(session != null){
	  		providers.put(SECURITY_PREFIX, new SecurityParameterProvider(session));
	  		providers.put(SESSION_PREFIX, new PentahoSessionParameterProvider(session));
  		}
  		providers.put(SYSTEM_PREFIX, new SystemSettingsParameterProvider());
  	}
  	
  	@Override public Object resolveReference(final Object name){
    	if(name instanceof String ){
    		String paramName = ((String) name).trim();
    		for(String prefix : providers.keySet()){
    			if(paramName.startsWith(prefix)){
    				paramName = paramName.substring(prefix.length());
    				Object value = providers.get(prefix).getParameter(paramName);
    				if(value instanceof JavaScriptResultSet){//needs special treatment, convert to array
    					JavaScriptResultSet resultSet = (JavaScriptResultSet) value;
    					return convertToArray(resultSet);
    				}
    				return value;
    			}
    		}
    	}
  		return super.resolveReference(name);
  	}

		private Object[] convertToArray(final JavaScriptResultSet resultSet) {
			List result = new ArrayList();
			for(int i =0; i < resultSet.getRowCount();i++){
				for(int j = 0; j < resultSet.getColumnCount(); j++){
					result.add(resultSet.getValueAt(i, j));
				}
			}
			return result.toArray();
		}
	
  }
  
}
