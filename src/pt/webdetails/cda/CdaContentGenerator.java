package pt.webdetails.cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

@SuppressWarnings("unchecked")
public class CdaContentGenerator extends BaseContentGenerator
{

  private static Log logger = LogFactory.getLog(CdaContentGenerator.class);
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String MIME_HTML = "text/xml";
  private static final String MIME_CSS = "text/css";
  private static final String MIME_JS = "text/javascript";
  private static final String EDITOR_SOURCE = "/editor/editor.html";
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
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
  public void createContent() throws Exception
  {
    final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse"); //$NON-NLS-1$ //$NON-NLS-2$
    try
    {
      final IParameterProvider pathParams = parameterProviders.get("path");
      final IParameterProvider requestParams = parameterProviders.get("request");

      final IContentItem contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_HTML);

      final OutputStream out = contentItem.getOutputStream(null);


      final String pathString = pathParams.getStringParameter("path", null);

      final String method = extractMethod(pathString);
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

      logger.error("Failed to execute", e);
    }

  }

  public void doQuery(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String path = getRelativePath(pathParams);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = pathParams.getLongParameter("pageSize", 0);
    final long pageStart = pathParams.getLongParameter("pageStart", 0);
    final boolean paginate = "true".equals(pathParams.getStringParameter("paginate", "false"));
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
    final ArrayList<Integer> sortBy = new ArrayList<Integer>();
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
    }

    String mimeType = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType()).getMimeType();
    String attachmentName = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType()).getAttachmentName();
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
    String document = getResourceAsString(getRelativePath(pathParams));
    setResponseHeaders("text/plain", null);
    out.write(document.getBytes());
  }

  private void writeCdaFile(IParameterProvider pathParams, OutputStream out) throws Exception
  {
    //throw new UnsupportedOperationException("Not yet implemented");

    //TODO: Validate the filename in some way, shape or form!
    String[] file = buildFileParameters(getRelativePath(pathParams));
    String rootDir = PentahoSystem.getApplicationContext().getSolutionPath("");

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    int status = solutionRepository.publish(rootDir, file[0], file[1], ((String) pathParams.getParameter("data")).getBytes("UTF-8"), true);
    if (status == ISolutionRepository.FILE_ADD_SUCCESSFUL)
    {
      solutionRepository.synchronizeSolutionWithSolutionSource(userSession);
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

  private String getRelativePath(final IParameterProvider pathParams)
  {
    final String solution = pathParams.getStringParameter("solution", "");
    if (StringUtils.isEmpty(solution))
    {
      return pathParams.getStringParameter("path", "");
    }

    return ActionInfo.buildSolutionPath(
        solution,
        pathParams.getStringParameter("path", ""),
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
      final InputStream in = solutionRepository.getResourceInputStream(path, true);
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

  public String getResourceAsString(final String path) throws IOException
  {

    return getResourceAsString(path, null);

  }

  public void editFile(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    final String editorPath = "system/" + PLUGIN_NAME + EDITOR_SOURCE;
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();
    setResponseHeaders("text/html", null);
    out.write(getResourceAsString(editorPath).getBytes());

  }


  public void previewQuery(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    final String previewerPath = "system/" + PLUGIN_NAME + PREVIEWER_SOURCE;
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();
    setResponseHeaders("text/html", null);
    out.write(getResourceAsString(previewerPath).getBytes());

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
    getResource(
        out, resource);


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
    String[] result = {"", ""};
    String[] file = filePath.split("/");
    String fileName = file[file.length - 1];
    String path = filePath.substring(0, filePath.indexOf(fileName));
    result[0] = path;
    result[1] = fileName;
    return result;
  }
}
