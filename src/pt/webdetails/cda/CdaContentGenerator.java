package pt.webdetails.cda;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

@SuppressWarnings("unchecked")
public class CdaContentGenerator extends BaseContentGenerator
{

  private static Log logger = LogFactory.getLog(CdaContentGenerator.class);
  public static final String PLUGIN_NAME = "pentaho-cda";
  private static final long serialVersionUID = 1L;
  private static final String MIME_TYPE = "text/html";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;

  public CdaContentGenerator()
  {
  }

  @Override
  public void createContent() throws Exception
  {

    final IParameterProvider pathParams = parameterProviders.get("path");
    final IParameterProvider requestParams = parameterProviders.get("request");

    final IContentItem contentItem = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_TYPE);

    final OutputStream out = contentItem.getOutputStream(null);

    try
    {

      final Class[] params = {IParameterProvider.class, OutputStream.class};

      final String method = pathParams.getStringParameter("path", null).replace("/", "").toLowerCase();

      try
      {
        final Method mthd = this.getClass().getMethod(method, params);
        mthd.invoke(this, requestParams, out);
      }
      catch (NoSuchMethodException e)
      {
        logger.error(Messages.getErrorString("DashboardDesignerContentGenerator.ERROR_001_INVALID_METHOD_EXCEPTION") + " : " + method);
      }
    }
    catch (Exception e)
    {
      final String message = e.getCause() != null ? e.getCause().getClass().getName() + " - " + e.getCause().getMessage() : e.getClass().getName() + " - " + e.getMessage();
      logger.error(message);
    }

  }

  public void doquery(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(getRelativePath(pathParams));

    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = pathParams.getLongParameter("pageSize", 0);
    final long pageStart = pathParams.getLongParameter("pageStart", 0);
    final boolean paginate = pathParams.getStringParameter("paginate", "false").equals("true") ? true : false;
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
    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);

  }


  public void listqueries(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    final CdaEngine engine = CdaEngine.getInstance();

    final String path = PentahoSystem.getApplicationContext().getSolutionPath(getRelativePath(pathParams)).replaceAll("//+", "/");

    // final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);


    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    engine.listQueries(out, cdaSettings, discoveryOptions);

  }


  public void listparameters(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {


    final CdaEngine engine = CdaEngine.getInstance();
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();

    final String path = PentahoSystem.getApplicationContext().getSolutionPath(getRelativePath(pathParams)).replaceAll("//+", "/");

    // final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);
    discoveryOptions.setDataAccessId(pathParams.getStringParameter("dataAccessId", "<blank>"));
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    engine.listParameters(out, cdaSettings, discoveryOptions);

  }


  public void getcdalist(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));

    engine.getCdaList(out, discoveryOptions, userSession);


  }


  public void clearcache(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {

    SettingsManager.getInstance().clearCache();


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
    return ActionInfo.buildSolutionPath(
        pathParams.getStringParameter("solution", ""),
        pathParams.getStringParameter("path", ""),
        pathParams.getStringParameter("file", ""));
  }
}
