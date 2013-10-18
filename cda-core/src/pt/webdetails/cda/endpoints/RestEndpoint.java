/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.endpoints;


import java.io.OutputStream;
import java.io.IOException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;


import java.util.List;

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
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cda.ResponseTypeHandler;

@Path("/cda/api/utils")
public class RestEndpoint {
  //private static final Log logger = LogFactory.getLog(CdaUtils.class);
  //private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
//  public static final String PLUGIN_NAME = "cda";
//  private static final long serialVersionUID = 1L;
  //private static final String EDITOR_SOURCE = "/editor/editor.html";
  //private static final String EXT_EDITOR_SOURCE = "/editor/editor-cde.html";
  //private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  //private static final String CACHE_MANAGER_PATH = "/cachemanager/cache.html";
  //private static final int DEFAULT_PAGE_SIZE = 20;
  //private static final int DEFAULT_START_PAGE = 0;
  //private static final String PREFIX_PARAMETER = "param";
  //private static final String PREFIX_SETTING = "setting";
  public static final String ENCODING = "UTF-8";
  
  
  private static Log logger = LogFactory.getLog(RestEndpoint.class);
  
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
      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
      coreService.doQuery(servletResponse.getOutputStream(), queryParams);
      
      
      
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
      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
      coreService.unwrapQuery(servletResponse.getOutputStream(), path, solution, file, uuid);
      
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
      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
      coreService.listQueries(servletResponse.getOutputStream(), path,solution,file, outputType);
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
      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
      coreService.listParameters(servletResponse.getOutputStream(), path,solution,file, outputType, dataAccessId);
      
      
  }
  
  
//  @GET
//  @Path("/getCdaFile")
//  @Produces("text/xml")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
//  public void getCdaFile(@QueryParam("path") String path, 
//                         @QueryParam("solution") String solution, 
//                         @QueryParam("file") String file,
//                            
//                         @Context HttpServletResponse servletResponse, 
//                         @Context HttpServletRequest servletRequest) throws Exception
//  {
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.getCdaFile(servletResponse.getOutputStream(), path,new ResponseTypeHandler(servletResponse));
//      
//  }
  
//  
//  @GET
//  @Path("/writeCdaFile")
//  @Produces("text/plain")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
//  public void writeCdaFile(@QueryParam("path") String path, 
//                           @QueryParam("solution") String solution, 
//                           @QueryParam("file") String file,
//                           @QueryParam("data") String data,
//                            
//                           @Context HttpServletResponse servletResponse, 
//                           @Context HttpServletRequest servletRequest) throws Exception
//  {  
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.writeCdaFile(servletResponse.getOutputStream(), path,solution,file, data);
//  }
  
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
      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
      coreService.getCdaList(servletResponse.getOutputStream(), outputType);
  }

//  @GET
//  @Path("/clearCache")
//  @Produces("text/plain")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
//  public void clearCache(@Context HttpServletResponse servletResponse, 
//                         @Context HttpServletRequest servletRequest) throws Exception
//  {
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.clearCache(servletResponse.getOutputStream());
//  }
  
//  @GET
//  @Path("/cacheMonitor")
//  @Produces("text/plain")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
//  public void cacheMonitor(@QueryParam("method") String method,
//                           @Context HttpServletResponse servletResponse, 
//                           @Context HttpServletRequest servletRequest) throws Exception{
//      Map parameters = servletRequest.getParameterMap();
//      ICommonParameterProvider requParam= new CommonParameterProvider();
//      Iterator it = parameters.entrySet().iterator();
//      while(it.hasNext())
//      {
//          Map.Entry e = (Map.Entry)it.next();
//          requParam.put((String)e.getKey(), e.getValue());
//      }
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.cacheMonitor(servletResponse.getOutputStream(), method, requParam);
//  }
  
  
//  @GET
//  @Path("/editFile")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
//  public void editFile(@QueryParam("path") String path, 
//                       @QueryParam("solution") String solution, 
//                       @QueryParam("file") String file,
//                       
//                       @Context HttpServletResponse servletResponse, 
//                       @Context HttpServletRequest servletRequest) throws Exception
//  {
//      CdaCoreService coreService = new CdaCoreService(new ResponseTypeHandler(servletResponse));
//      coreService.editFile(servletResponse.getOutputStream(), path,solution,file,new ResponseTypeHandler(servletResponse));
//   }

//  @GET
//  @Path("/previewQuery")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
//  public void previewQuery(@Context HttpServletResponse servletResponse, 
//                           @Context HttpServletRequest servletRequest) throws Exception
//  {
//    //TODO: moved out!
//    logger.error("previewer has been moved, blame tgf!");
//  }

  @GET
  @Path("/getCssResource")
  @Produces("text/css")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCssResource(@QueryParam("resource") String resource,
          
                             @Context HttpServletResponse servletResponse, 
                             @Context HttpServletRequest servletRequest) throws Exception
  {
      CdaCoreService coreService = new CdaCoreService();
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
      CdaCoreService coreService = new CdaCoreService();
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
      CdaCoreService coreService = new CdaCoreService();
      coreService.listDataAccessTypes(servletResponse.getOutputStream(), refreshCache);
  }

//  @GET
//  @Path("/cacheController")
//  @Produces("text/plain")
//  @Consumes({ APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED })
//  public void cacheController(@QueryParam("method") String method,
//                              @QueryParam("object") String object,
//                              @QueryParam("id") String id,
//  
//                              @Context HttpServletResponse servletResponse, 
//                              @Context HttpServletRequest servletRequest) throws IOException
//  {
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.cacheController(servletResponse.getOutputStream(), method, id);
//  }

  @GET
  @Path("/manageCache")
  @Produces("text/plain")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void manageCache(@Context HttpServletResponse servletResponse, 
                          @Context HttpServletRequest servletRequest) throws Exception
  {
    //FIXME
    throw new NotImplementedException("no cache manager yet");
//      CdaCoreService coreService = new CdaCoreService();
//      coreService.manageCache(servletResponse.getOutputStream(),new ResponseTypeHandler(servletResponse));
  }
  
//  //XXX could use this getRelativePath instead of the one in CoreService?
//  private String getRelativePath(String solution, String path, String file) throws UnsupportedEncodingException
//  {
//    if (StringUtils.isEmpty(solution))
//    {
//      return path;
//    }
//
//    return StringUtils.join(new String[] {solution, path, file}, "/" ).replaceAll("//", "/");
//  }
  
  protected void writeOut(OutputStream out, String contents) throws IOException {
      IOUtils.write(contents, out, getEncoding());
      out.flush();
    }
    
}
