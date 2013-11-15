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


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;

@Path("/cda/api/utils")
/**
 * @deprecated
 */
public class RestEndpoint {
  private static String getEncoding() { return CharsetHelper.getEncoding(); }
  
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
      CdaCoreService coreService = getCoreService();
      if ( wrapItUp ) {
        writeOut( servletResponse.getOutputStream(), coreService.wrapQuery( queryParams ) );
      }
      else {
        coreService.doQuery( queryParams ).writeResponse( servletResponse );
      }
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
      CdaCoreService coreService = getCoreService();
      DoQueryParameters params = new DoQueryParameters(path, solution, file);
      coreService.unwrapQuery(params.getPath(), uuid ).writeResponse( servletResponse );
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
      CdaCoreService coreService = getCoreService();
      DoQueryParameters params = new DoQueryParameters(path, solution, file);
      ExportedQueryResult result = coreService.listQueries( params.getPath(), getSimpleExportOptions( outputType ) );
      result.writeResponse( servletResponse );
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
      CdaCoreService coreService = getCoreService();
      DoQueryParameters params = new DoQueryParameters(path, solution, file);
      ExportedQueryResult result = 
          coreService.listParameters( params.getPath(), dataAccessId, getSimpleExportOptions( outputType ) );
      result.writeResponse( servletResponse );
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
      CdaCoreService coreService = getCoreService();
      ExportedQueryResult result = coreService.getCdaList( getSimpleExportOptions( outputType) );
      result.writeResponse( servletResponse );
  }

  private ExportOptions getSimpleExportOptions(final String outputType ) {
    return new ExportOptions() {

      public String getOutputType() {
        return outputType;
      }

      public Map<String, String> getExtraSettings() {
        return Collections.<String,String>emptyMap();
      }
    };
  }

  @GET
  @Path("/getCssResource")
  @Produces("text/css")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getCssResource(@QueryParam("resource") String resource,
          
                             @Context HttpServletResponse servletResponse, 
                             @Context HttpServletRequest servletRequest) throws Exception
  {
     getCssResource(servletResponse.getOutputStream(), resource);
  }

  @GET
  @Path("/getJsResource")
  @Produces(MimeTypes.JAVASCRIPT)
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public void getJsResource(@QueryParam("resource") String resource,
          
                            @Context HttpServletResponse servletResponse, 
                            @Context HttpServletRequest servletRequest) throws Exception
  {
     getJsResource(servletResponse.getOutputStream(), resource);
  }
  
  @GET
  @Path("/listDataAccessTypes")
  @Produces(MimeTypes.JSON)
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public String listDataAccessTypes(@DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache,
                                  @Context HttpServletResponse servletResponse, 
                                  @Context HttpServletRequest servletRequest) throws Exception
  {
      CdaCoreService coreService = getCoreService();
      return coreService.listDataAccessTypes( refreshCache );
  }

  private CdaCoreService getCoreService() {
    return new CdaCoreService();
  }

  protected void writeOut( OutputStream out, String contents ) throws IOException {
    IOUtils.write( contents, out, getEncoding() );
    out.flush();
  }
  
  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file
    IReadAccess repository = getRepositoryAccess();
    String resourceContents = StringUtils.EMPTY;
    
    if (repository.fileExists(path))
    {
      resourceContents = Util.toString( repository.getFileInputStream(path) );
    }
    else {
      return null;
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

  public void getCssResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  public void getJsResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  private void getResource(final OutputStream out, String resource) throws Exception
  {
    IReadAccess repo = getRepositoryAccess();
    if ( repo.fileExists( resource ) ) {
      InputStream in = null;
      try {
        in = repo.getFileInputStream( resource );
        IOUtils.copy( in, out );
        out.flush();
      }
      finally {
        IOUtils.closeQuietly( in );
      }
      
    }

  }

  private IReadAccess getRepositoryAccess() {
    return PluginEnvironment.env().getContentAccessFactory().getUserContentAccess("/");
  }
}
