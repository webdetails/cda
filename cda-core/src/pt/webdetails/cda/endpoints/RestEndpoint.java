/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.endpoints;


import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;


import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.http.CommonParameterProvider;
import pt.webdetails.cpf.http.ICommonParameterProvider;

@Path("/cda/api/utils")
public class RestEndpoint {
  //private static final Log logger = LogFactory.getLog(CdaUtils.class);
  //private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  //private static final String EDITOR_SOURCE = "/editor/editor.html";
  //private static final String EXT_EDITOR_SOURCE = "/editor/editor-cde.html";
  //private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  //private static final String CACHE_MANAGER_PATH = "/cachemanager/cache.html";
  //private static final int DEFAULT_PAGE_SIZE = 20;
  //private static final int DEFAULT_START_PAGE = 0;
  //private static final String PREFIX_PARAMETER = "param";
  //private static final String PREFIX_SETTING = "setting";
  public static final String ENCODING = "UTF-8";
  
  
  
  private CdaCoreService coreService;
  
  public RestEndpoint() {
        this.coreService = new CdaCoreService();//XXX init the coreService here?  
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
                      @DefaultValue("<blank>") @QueryParam("jsonCallback") String jsonCallback,
                      
                      @Context HttpServletResponse servletResponse, 
                      @Context HttpServletRequest servletRequest) throws Exception
  {
      
      DoQueryParameters queryParams = new DoQueryParameters(path, solution, file);
      queryParams.setBypassCache(bypassCache);
      queryParams.setDataAccessId(dataAccessId);
      queryParams.setOutputIndexId(outputIndexId);
      queryParams.setOutputType(outputType);
      queryParams.setPageSize(pageSize);
      queryParams.setPageStart(pageStart);
      queryParams.setPaginateQuery(paginateQuery);
      queryParams.setSortBy(sortBy);
      queryParams.setWrapItUp(wrapItUp);
      queryParams.setJsonCallback(jsonCallback);
      coreService.doQuery(servletResponse, queryParams);
      
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
      coreService.unwrapQuery(servletResponse, path, solution, file, uuid);
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
      
      coreService.listQueries(servletResponse, path,solution,file, outputType);
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
      coreService.listParameters(servletResponse, path,solution,file, outputType, dataAccessId);
      
      
  }
  
  
  @GET
  @Path("/getCdaFile")
  @Produces("text/xml")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCdaFile(@QueryParam("path") String path, 
                         @QueryParam("solution") String solution, 
                         @QueryParam("file") String file,
                            
                         @Context HttpServletResponse servletResponse, 
                         @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.getCdaFile(servletResponse.getOutputStream(), path);
      
  }
  
  
  @GET
  @Path("/writeCdaFile")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void writeCdaFile(@QueryParam("path") String path, 
                           @QueryParam("solution") String solution, 
                           @QueryParam("file") String file,
                           @QueryParam("data") String data,
                            
                           @Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception
  {  
  coreService.writeCdaFile(servletResponse.getOutputStream(), path,solution,file, data);
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
      coreService.getCdaList(servletResponse, outputType);
  }

  @GET
  @Path("/clearCache")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void clearCache(@Context HttpServletResponse servletResponse, 
                         @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.clearCache(servletResponse.getOutputStream());
  }
  
  @GET
  @Path("/cacheMonitor")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void cacheMonitor(@QueryParam("method") String method,
                           @Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception{
      Map parameters = servletRequest.getParameterMap();
      ICommonParameterProvider requParam= new CommonParameterProvider();
      Iterator it = parameters.entrySet().iterator();
      while(it.hasNext())
      {
          Map.Entry e = (Map.Entry)it.next();
          requParam.put((String)e.getKey(), e.getValue());
      }
      coreService.cacheMonitor(servletResponse.getOutputStream(), method, requParam);
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
      coreService.editFile(servletResponse, path,solution,file);
   }

  @GET
  @Path("/previewQuery")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void previewQuery(@Context HttpServletResponse servletResponse, 
                           @Context HttpServletRequest servletRequest) throws Exception
  {
       coreService.previewQuery(servletResponse.getOutputStream());
  }

  @GET
  @Path("/getCssResource")
  @Produces("text/css")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCssResource(@QueryParam("resource") String resource,
          
                             @Context HttpServletResponse servletResponse, 
                             @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.getCssResource(servletResponse.getOutputStream(), resource);
  }

  @GET
  @Path("/getJsResource")
  @Produces("text/javascript")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getJsResource(@QueryParam("resource") String resource,
          
                            @Context HttpServletResponse servletResponse, 
                            @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.getJsResource(servletResponse.getOutputStream(), resource);
  }
  
  @GET
  @Path("/listDataAccessTypes")
  @Produces("application/json")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void listDataAccessTypes(@DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache,
          
                                  @Context HttpServletResponse servletResponse, 
                                  @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.listDataAccessTypes(servletResponse.getOutputStream(), refreshCache);
  }

  @GET
  @Path("/cacheController")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
  public void cacheController(@QueryParam("method") String method,
                              @QueryParam("object") String object,
                              @QueryParam("id") String id,
  
                              @Context HttpServletResponse servletResponse, 
                              @Context HttpServletRequest servletRequest) throws IOException
  {
      coreService.cacheController(servletResponse.getOutputStream(), method, id);
  }

  @GET
  @Path("/manageCache")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void manageCache(@Context HttpServletResponse servletResponse, 
                          @Context HttpServletRequest servletRequest) throws Exception
  {
      coreService.manageCache(servletResponse.getOutputStream());
  }
  
  //XXX could use this getRelativePath instead of the one in CoreService?
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
