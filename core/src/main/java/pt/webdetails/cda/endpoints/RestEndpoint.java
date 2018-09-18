/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package pt.webdetails.cda.endpoints;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_DATA_ACCESS_ID;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_OUTPUT_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * TODO: Remove deprecated tag?
 * @deprecated
 */
@Path( "/cda/api/utils" )
public class RestEndpoint {
  static final String PREFIX_PARAMETER = "param";
  static final String PREFIX_SETTING = "setting";

  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  private static String getEncoding() {
    return CharsetHelper.getEncoding();
  }

  @POST
  @Path( "/doQuery" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public Response doQueryPost( MultivaluedMap<String, String> multivaluedMap

/*    @FormParam( "path" ) String path,
                               @FormParam( "solution" ) String solution,
                               @FormParam( "file" ) String file,
                               @DefaultValue( "json" ) @FormParam( "outputType" ) String outputType,
                               @DefaultValue( "1" ) @FormParam( "outputIndexId" ) int outputIndexId,
                               @DefaultValue( "<blank>" ) @FormParam( "dataAccessId" ) String dataAccessId,
                               @DefaultValue( "false" ) @FormParam( "bypassCache" ) Boolean bypassCache,
                               @DefaultValue( "false" ) @FormParam( "paginateQuery" ) Boolean paginateQuery,
                               @DefaultValue( "0" ) @FormParam( "pageSize" ) int pageSize,
                               @DefaultValue( "0" ) @FormParam( "pageStart" ) int pageStart,
                               @DefaultValue( "false" ) @FormParam( "wrapItUp" ) Boolean wrapItUp,
                               @FormParam( "sortBy" ) List<String> sortBy,
                               @DefaultValue( "<blank>" ) @FormParam( "jsonCallback" ) String jsonCallback,
                               @Context HttpServletRequest request
                               */
) {
    try {
      /*
      final DoQueryParameters queryParams = getQueryParameters( path, solution, file, outputType, outputIndexId,
              dataAccessId, bypassCache, paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, request );
              */
      final DoQueryParameters queryParams = getQueryParameters( multivaluedMap );

      return this.doQuery( queryParams );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  @GET
  @Path( "/doQuery" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public Response doQueryGet( @QueryParam( "path" ) String path,
                              @QueryParam( "solution" ) String solution,
                              @QueryParam( "file" ) String file,
                              @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType,
                              @DefaultValue( "1" ) @QueryParam( "outputIndexId" ) int outputIndexId,
                              @DefaultValue( "<blank>" ) @QueryParam( "dataAccessId" ) String dataAccessId,
                              @DefaultValue( "false" ) @QueryParam( "bypassCache" ) Boolean bypassCache,
                              @DefaultValue( "false" ) @QueryParam( "paginateQuery" ) Boolean paginateQuery,
                              @DefaultValue( "0" ) @QueryParam( "pageSize" ) int pageSize,
                              @DefaultValue( "0" ) @QueryParam( "pageStart" ) int pageStart,
                              @DefaultValue( "false" ) @QueryParam( "wrapItUp" ) Boolean wrapItUp,
                              @QueryParam( "sortBy" ) List<String> sortBy,
                              @DefaultValue( "<blank>" ) @QueryParam( "jsonCallback" ) String jsonCallback,
                              @Context HttpServletRequest request ) {
    try {
      final DoQueryParameters queryParams = getQueryParameters( path, solution, file, outputType, outputIndexId,
              dataAccessId, bypassCache, paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, request );

      return this.doQuery( queryParams );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  private Response doQuery( DoQueryParameters queryParameters ) throws Exception {
    final CdaCoreService coreService = getCoreService();

    if ( queryParameters.isWrapItUp() ) {
      return buildOkResponse( coreService.wrapQuery( queryParameters ) );
    } else {
      return buildOkResponse( coreService.doQuery( queryParameters ) );
    }
  }

  @GET
  @Path( "/unwrapQuery" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response unwrapQuery( @QueryParam( "path" ) String path,
                               @QueryParam( "solution" ) String solution,
                               @QueryParam( "file" ) String file,
                               @QueryParam( "uuid" ) String uuid ) {
    try {
      final DoQueryParameters parameters = getQueryParameters( path, solution, file );
      final ExportedQueryResult result = this.unwrapQuery( parameters, uuid );

      return buildOkResponse( result );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  private ExportedQueryResult unwrapQuery( DoQueryParameters parameters, String uuid ) throws Exception {
    final String cdaSettingsPath = parameters.getPath();

    final CdaCoreService coreService = getCoreService();
    return coreService.unwrapQuery( cdaSettingsPath, uuid );
  }

  @GET
  @Path( "/listQueries" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public Response listQueries( @QueryParam( "path" ) String path,
                               @QueryParam( "solution" ) String solution,
                               @QueryParam( "file" ) String file,
                               @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType ) {
    try {
      final DoQueryParameters parameters = getQueryParameters( path, solution, file, outputType );
      final ExportedQueryResult result = this.listQueries( parameters );

      return buildOkResponse( result );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  private ExportedQueryResult listQueries( DoQueryParameters parameters ) throws Exception {
    final String cdaSettingsPath = parameters.getPath();
    final ExportOptions exportOptions = getSimpleExportOptions( parameters.getOutputType() );

    final CdaCoreService coreService = getCoreService();
    return coreService.listQueries( cdaSettingsPath, exportOptions );
  }

  @GET
  @Path( "/listParameters" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  public Response listParameters( @QueryParam( "path" ) String path,
                                  @QueryParam( "solution" ) String solution,
                                  @QueryParam( "file" ) String file,
                                  @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType,
                                  @DefaultValue( "<blank>" ) @QueryParam( "dataAccessId" ) String dataAccessId ) {
    try {
      final DoQueryParameters parameters = getQueryParameters( path, solution, file, outputType, dataAccessId );
      final ExportedQueryResult result = this.listParameters( parameters );

      return buildOkResponse( result );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  private ExportedQueryResult listParameters( DoQueryParameters queryParameters ) throws Exception {
    final String cdaSettingsPath = queryParameters.getPath();
    final String dataAccessId = queryParameters.getDataAccessId();
    final ExportOptions exportOptions = getSimpleExportOptions( queryParameters.getOutputType() );

    final CdaCoreService coreService = getCoreService();
    return coreService.listParameters( cdaSettingsPath, dataAccessId, exportOptions );
  }

  @GET
  @Path( "/getCdaList" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getCdaList( @QueryParam( "path" ) String path,
                              @QueryParam( "solution" ) String solution,
                              @QueryParam( "file" ) String file,
                              @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType ) {
    try {
      final DoQueryParameters queryParameters = getQueryParameters( path, solution, file, outputType );
      final ExportedQueryResult cdaListResult = this.getCdaList( queryParameters );

      return buildOkResponse( cdaListResult );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }
  }

  private ExportedQueryResult getCdaList( DoQueryParameters queryParameters ) throws UnsupportedExporterException {
    final ExportOptions exportOptions = getSimpleExportOptions( queryParameters.getOutputType() );

    final CdaCoreService coreService = getCoreService();
    return coreService.getCdaList( exportOptions );
  }

  private ExportOptions getSimpleExportOptions( final String outputType ) {
    return new ExportOptions() {

      public String getOutputType() {
        return outputType;
      }

      public Map<String, String> getExtraSettings() {
        return Collections.emptyMap();
      }
    };
  }

  @GET
  @Path( "/getCssResource" )
  @Produces( MimeTypes.CSS )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getCssResource( @QueryParam( "resource" ) String resource ) {
    try {
      final String cssResource = getResource( resource );

      return buildOkResponse( cssResource, MimeTypes.CSS );
    } catch ( IOException ioe ) {
      return buildServerErrorResponse();
    }
  }

  @GET
  @Path( "/getJsResource" )
  @Produces( MimeTypes.JAVASCRIPT )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getJsResource( @QueryParam( "resource" ) String resource ) {
    try {
      final String jsResource = getResource( resource );

      return buildOkResponse( jsResource, MimeTypes.JAVASCRIPT );
    } catch ( IOException ioe ) {
      return buildServerErrorResponse();
    }
  }

  @GET
  @Path( "/listDataAccessTypes" )
  @Produces( MimeTypes.JSON )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response listDataAccessTypes( @DefaultValue( "false" ) @QueryParam( "refreshCache" ) Boolean refreshCache ) {
    try {
      final CdaCoreService coreService = getCoreService();
      final String result = coreService.listDataAccessTypes( refreshCache );

      return buildOkResponse( result, MimeTypes.JSON );
    } catch ( Exception ex ) {
      return buildServerErrorResponse();
    }

  }

  private CdaCoreService getCoreService() {
    return new CdaCoreService();
  }

  private String getResource( String resource ) throws IOException {
    final IReadAccess repositoryAccess = getRepositoryAccess();

    if ( !repositoryAccess.fileExists( resource ) ) {
      throw new FileNotFoundException( "Resource '" + resource + "' not found." );
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    InputStream resourceInputStream = null;
    try {
      resourceInputStream = repositoryAccess.getFileInputStream( resource );
      IOUtils.copy( resourceInputStream, out );
    } finally {
      IOUtils.closeQuietly( resourceInputStream );
    }

    return new String( out.toByteArray(), getEncoding() );
  }

  private Response buildOkResponse( String result ) {
    return buildOkResponse( result, MimeTypes.PLAIN_TEXT );
  }

  private Response buildOkResponse( String result, String contentType ) {
    final String contentTypeHeader = contentType + ";charset=" + getEncoding();

    return Response.ok( result )
            .header( HEADER_CONTENT_TYPE, contentTypeHeader )
            .build();
  }

  private Response buildOkResponse( ExportedQueryResult result ) throws ExporterException {
    final String contentType = result.getContentType();
    final String contentDisposition = result.getContentDisposition();

    return Response.ok( result.asString() )
            .header( HEADER_CONTENT_TYPE, contentType )
            .header( HEADER_CONTENT_DISPOSITION, contentDisposition )
            .build();
  }

  private Response buildServerErrorResponse() {
    return Response.serverError().build();
  }

  private IReadAccess getRepositoryAccess() {
    return PluginEnvironment.env().getContentAccessFactory().getUserContentAccess( "/" );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file ) {
    return getQueryParameters( path, solution, file, DEFAULT_OUTPUT_TYPE, DEFAULT_DATA_ACCESS_ID );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType ) {
    return getQueryParameters( path, solution, file, outputType, DEFAULT_DATA_ACCESS_ID );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                String dataAccessId ) {
    DoQueryParameters queryParameters = new DoQueryParameters( path, solution, file );

    queryParameters.setOutputType( outputType );
    queryParameters.setDataAccessId( dataAccessId );

    return queryParameters;
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                int outputIndexId, String dataAccessId, Boolean bypassCache,
                                                Boolean paginateQuery, int pageSize, int pageStart,
                                                Boolean wrapItUp, List<String> sortBy, String jsonCallback,
                                                HttpServletRequest request ) {
    DoQueryParameters queryParameters = new DoQueryParameters( path, solution, file );

    queryParameters.setBypassCache( bypassCache );
    queryParameters.setDataAccessId( dataAccessId );
    queryParameters.setOutputIndexId( outputIndexId );
    queryParameters.setOutputType( outputType );
    queryParameters.setPageSize( pageSize );
    queryParameters.setPageStart( pageStart );
    queryParameters.setPaginateQuery( paginateQuery );
    queryParameters.setSortBy( sortBy );
    queryParameters.setWrapItUp( wrapItUp );
    queryParameters.setJsonCallback( jsonCallback );

    final Map<String, Object> extraParameters = getParameters( request, RequestParameter::isExtraParameter );
    queryParameters.setParameters( extraParameters );

    final Map<String, Object> extraSettings = getParameters( request, RequestParameter::isSettingParameter );
    queryParameters.setExtraSettings( extraSettings );

    return queryParameters;
  }


  private DoQueryParameters getQueryParameters( MultivaluedMap<String, String> multivaluedMap ) {
    String path = multivaluedMap.getFirst("path" );
    String solution = multivaluedMap.getFirst( "solution" );
    String file = multivaluedMap.getFirst( "file" );

    DoQueryParameters queryParameters = new DoQueryParameters( path, solution, file );

    boolean bypassCache = new Boolean( getValueOrDefault( multivaluedMap, "bypassCache", "false" ) );
    String dataAccessId = getValueOrDefault( multivaluedMap, "dataAccessId", "<blank>" );
    int outputIndexId = new Integer( getValueOrDefault( multivaluedMap, "outputIndexId", "1" ) );
    String outputType = getValueOrDefault( multivaluedMap, "outputType", "json" );
    int pageSize = new Integer( getValueOrDefault( multivaluedMap, "pageSize", "0" ) );
    int pageStart = new Integer( getValueOrDefault( multivaluedMap, "pageStart", "0" ) );
    boolean paginateQuery = new Boolean( getValueOrDefault( multivaluedMap, "paginateQuery", "false" ) );
    List<String> sortBy = multivaluedMap.get( "sortBy" );
    sortBy = sortBy != null ? sortBy : new ArrayList<>();
    boolean wrapItUp = new Boolean( getValueOrDefault( multivaluedMap, "wrapItUp", "false" ) );
    String jsonCallback = getValueOrDefault( multivaluedMap, "jsonCallback", "<blank>" );

    queryParameters.setBypassCache( bypassCache );
    queryParameters.setDataAccessId( dataAccessId );
    queryParameters.setOutputIndexId( outputIndexId );
    queryParameters.setOutputType( outputType );
    queryParameters.setPageSize( pageSize );
    queryParameters.setPageStart( pageStart );
    queryParameters.setPaginateQuery( paginateQuery );
    queryParameters.setSortBy( sortBy );
    queryParameters.setWrapItUp( wrapItUp );
    queryParameters.setJsonCallback( jsonCallback );

    final Map<String, Object> extraParameters = getParameters( multivaluedMap, name -> name.startsWith( PREFIX_PARAMETER ) );
    queryParameters.setParameters( extraParameters );

    final Map<String, Object> extraSettings = getParameters( multivaluedMap, name -> name.startsWith( PREFIX_SETTING ) );
    queryParameters.setExtraSettings( extraSettings );

    return queryParameters;
  }

  private String getValueOrDefault( MultivaluedMap<String, String> multivaluedMap, String key, String defaultValue ) {
    String stringValue = multivaluedMap.getFirst( key );
    return stringValue != null ? stringValue : defaultValue;
  }

  Map<String, Object> getParameters( MultivaluedMap<String, String> multivaluedMap, Predicate<String> predicate ) {
    Map<String, Object> parameters = new HashMap<>();

    for( Map.Entry<String, List<String>> entry : multivaluedMap.entrySet() ) {
      if( predicate.test( entry.getKey() )) {
        final String[] values = entry.getValue().toArray(new String[0]);
        parameters.put( entry.getKey(), values.length > 1 ? values : values[ 0 ]);
      }
    }

    return parameters;
  }


  Map<String, Object> getParameters( HttpServletRequest request, Predicate<RequestParameter> predicate ) {
    Map<String, Object> parameters = new HashMap<>();

    final Enumeration<String> parameterNames = request.getParameterNames();
    while ( parameterNames.hasMoreElements() ) {
      final String parameterName = parameterNames.nextElement();
      final RequestParameter requestParameter = new RequestParameter( request, parameterName );

      if ( predicate.test( requestParameter ) ) {
        parameters.put( requestParameter.getName(), requestParameter.getValue() );
      }
    }

    return parameters;
  }

  final class RequestParameter {

    private HttpServletRequest request;

    private String name;
    private Object value;

    private boolean isExtraParameter;
    private boolean isSettingParameter;

    RequestParameter( HttpServletRequest request, String name ) {
      this.request = request;

      this.isExtraParameter = name.startsWith( PREFIX_PARAMETER );
      this.isSettingParameter = name.startsWith( PREFIX_SETTING );

      this.name = name;
      this.value = null;
    }

    String getName() {
      if ( this.isExtraParameter ) {
        return this.name.replaceFirst( PREFIX_PARAMETER, "" );
      }

      if ( this.isSettingParameter ) {
        return this.name.replaceFirst( PREFIX_SETTING, "" );
      }

      return this.name;
    }

    Object getValue() {
      if ( value != null ) {
        return value;
      }

      return ( value = getRequestParameterValue() );
    }

    boolean isExtraParameter() {
      return this.isExtraParameter;
    }

    boolean isSettingParameter() {
      return this.isSettingParameter;
    }

    private Object getRequestParameterValue() {
      final String[] values = this.request.getParameterValues( this.name );

      final boolean hasMultipleValues = values.length > 1;
      return hasMultipleValues ? values : values[ 0 ];
    }
  }
}
