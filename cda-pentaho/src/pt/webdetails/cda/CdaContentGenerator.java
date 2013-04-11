/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;

import pt.webdetails.cda.cache.CacheScheduleManager;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Audited;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.http.PentahoParameterProvider;


public class CdaContentGenerator extends SimpleContentGenerator
{
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";
 


  
  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  @Audited(action = "doQuery")
  public void doQuery(final OutputStream out) throws Exception
  {

    SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession()));     
    
    
    final IParameterProvider requestParams = getRequestParameters();
    DoQueryParameters parameters = new DoQueryParameters(
            (String)requestParams.getParameter("path"), 
            (String)requestParams.getParameter("solution"), 
            (String)requestParams.getParameter("file"));
    
    parameters.setBypassCache(Boolean.parseBoolean(requestParams.getStringParameter("bypassCache","false")));
    parameters.setDataAccessId(requestParams.getStringParameter("dataAccessId", "<blank>"));
    parameters.setOutputIndexId(Integer.parseInt(requestParams.getStringParameter("outputIndexId", "1")));
    parameters.setOutputType(requestParams.getStringParameter("outputType", "json"));
    parameters.setPageSize((int)requestParams.getLongParameter("pageSize", 0));
    parameters.setPageStart((int)requestParams.getLongParameter("pageStart", 0));
    parameters.setPaginateQuery(Boolean.parseBoolean(requestParams.getStringParameter("paginateQuery", "false")));

    Map<String, Object> extraParams = new HashMap<String, Object>();
    Map<String, Object> extraSettings = new HashMap<String, Object>();
    final Iterator<String> params = (Iterator<String>) requestParams.getParameterNames();
    while (params.hasNext())
    {
      final String param = params.next();

      if (param.startsWith(PREFIX_PARAMETER))
      {
        extraParams.put(param.substring(PREFIX_PARAMETER.length()), requestParams.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        extraSettings.put(param.substring(PREFIX_SETTING.length()), requestParams.getStringParameter(param, ""));
      }
    }
    
    parameters.setExtraParams(extraParams);
    
    parameters.setExtraSettings(extraSettings);
    
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
    parameters.setSortBy(sortBy);
    parameters.setWrapItUp(requestParams.getStringParameter("wrapItUp", null) != null);

    CdaCoreService service = new CdaCoreService();
    service.doQuery(out, parameters);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void unwrapQuery(final OutputStream out) throws Exception
  {
    final IParameterProvider requestParams = getRequestParameters();
    final CdaCoreService coreService = new CdaCoreService();
    coreService.unwrapQuery(out, (String)requestParams.getParameter("path"), 
            (String)requestParams.getParameter("solution"), 
            (String)requestParams.getParameter("file"), 
            requestParams.getStringParameter("uuid", null));
    
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listQueries(final OutputStream out) throws Exception
  {
    final IParameterProvider requestParams = getRequestParameters();
    final CdaCoreService service = new CdaCoreService();
    service.listQueries(out, (String)requestParams.getParameter("path"),
            (String)requestParams.getParameter("solution"),
            (String)requestParams.getParameter("file"),
            requestParams.getStringParameter("outputType", "json"));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listParameters(final OutputStream out) throws Exception
  {
    final IParameterProvider requestParams = getRequestParameters();
    CdaCoreService service = new CdaCoreService();
    service.listParameters(out, (String) requestParams.getParameter("path") , 
            (String)requestParams.getParameter("solution"),
            (String)requestParams.getParameter("file"),
            requestParams.getStringParameter("outputType", "json"), requestParams.getStringParameter("dataAccessId", "<blank>") );
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.XML)
  public void getCdaFile(final OutputStream out) throws Exception
  {
    final IParameterProvider requestParams = getRequestParameters();
    CdaCoreService service = new CdaCoreService();
    //TO DO - Add the path, solution, file
    service.getCdaFile(out, requestParams.getStringParameter("path", ""));        
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.PLAIN_TEXT)
  public void writeCdaFile(OutputStream out) throws Exception
  {
    
    final IParameterProvider requestParams = getRequestParameters();
    CdaCoreService service = new CdaCoreService();
    service.writeCdaFile(out, requestParams.getStringParameter("path", ""), 
            (String)requestParams.getParameter("solution"),
            (String)requestParams.getParameter("file"),
            (String) requestParams.getParameter("data"));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getCdaList(final OutputStream out) throws Exception
  {
    
    CdaCoreService service = new CdaCoreService();
    service.getCdaList(out, getRequestParameters().getStringParameter("outputType", "json"));
  }

  @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
  public void clearCache(final OutputStream out) throws Exception
  {
    CdaCoreService service = new CdaCoreService();
    service.clearCache(out);
  }
  
  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void cacheMonitor(final OutputStream out) throws Exception{
    CdaCoreService service = new CdaCoreService();
    
    IParameterProvider parameters = getRequestParameters();
    service.cacheMonitor(out, parameters.getStringParameter("method", ""), 
            new PentahoParameterProvider(parameters));
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




  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void editFile(final OutputStream out) throws Exception
  {
    CdaCoreService coreService = new CdaCoreService();
    coreService.editFile(out, (String)getRequestParameters().getParameter("path"),
                        (String)getRequestParameters().getParameter("solution"),
            (String)getRequestParameters().getParameter("file")
            );
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void previewQuery(final OutputStream out) throws Exception
  {
    CdaCoreService service = new CdaCoreService();
    service.previewQuery(out);
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.CSS)
  public void getCssResource(final OutputStream out) throws Exception
  {
    CdaCoreService service = new CdaCoreService();
    service.getCssResource(out, getRequestParameters().getStringParameter("resource", null));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT)
  public void getJsResource(final OutputStream out) throws Exception
  {
    CdaCoreService service = new CdaCoreService();
    service.getJsResource(out, getRequestParameters().getStringParameter("resource", null));
  }


  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listDataAccessTypes(final OutputStream out) throws Exception
  {
    boolean refreshCache = Boolean.parseBoolean(getRequestParameters().getStringParameter("refreshCache", "false"));
   
    CdaCoreService coreService = new CdaCoreService();
    coreService.listDataAccessTypes(out, refreshCache);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void cacheController(OutputStream out)
  {
      String method = getRequestParameters().getParameter("method").toString();
      String obj = getRequestParameters().getStringParameter("object", "");
      
      CacheScheduleManager.getInstance().handleCall(method,obj, out);
  }

  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void manageCache(final OutputStream out) throws Exception
  {
    CdaCoreService coreService = new CdaCoreService();
    coreService.manageCache(out);            
  }
  
  
  
  


  @Override
  public String getPluginName() {
    return "cda";
  }
}
