package pt.webdetails.cda;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
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
  private static final String MIME_TYPE = "text/xml";
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

      final IContentItem contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_TYPE);

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
    setResponseHeaders(mimeType);


    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);

  }

  private void setResponseHeaders(final String mimeType)
  {
    // Make sure we have the correct mime type
    final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    response.setHeader("Content-Type", mimeType);

    // We can't cache this requests
    response.setHeader("Cache-Control", "max-age=0, no-store");
  }


  public void listQueries(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    final CdaEngine engine = CdaEngine.getInstance();

    final String path = getRelativePath(pathParams);
    logger.error("Do Query: getRelativePath:" + path);
    logger.error("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);


    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.listQueries(out, cdaSettings, discoveryOptions);


  }


  public void listParameters(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    final CdaEngine engine = CdaEngine.getInstance();
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();

    final String path = getRelativePath(pathParams);
    logger.error("Do Query: getRelativePath:" + path);
    logger.error("Do Query: getSolPath:" + PentahoSystem.getApplicationContext().getSolutionPath(path));
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);

    // final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    discoveryOptions.setDataAccessId(pathParams.getStringParameter("dataAccessId", "<blank>"));
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.listParameters(out, cdaSettings, discoveryOptions);

  }


  public void getCdaList(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.getCdaList(out, discoveryOptions, userSession);


  }


  public void clearCache(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    setResponseHeaders("text/plain");
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
}
