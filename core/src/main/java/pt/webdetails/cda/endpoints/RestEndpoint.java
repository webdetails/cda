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
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_BYPASS_CACHE;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_DATA_ACCESS_ID;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_JSON_CALLBACK;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_OUTPUT_INDEX_ID;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_OUTPUT_TYPE;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_PAGE_SIZE;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_PAGE_START;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_PAGINATE_QUERY;
import static pt.webdetails.cda.utils.DoQueryParameters.DEFAULT_WRAP_IT_UP;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
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
  public Response doQueryPost( MultivaluedMap<String, String> formParameters,
                               @Context HttpServletRequest request ) {
    try {
      final DoQueryParameters queryParams = getQueryParameters( formParameters );

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
                              @DefaultValue( DEFAULT_OUTPUT_TYPE ) @QueryParam( "outputType" ) String outputType,
                              @DefaultValue( DEFAULT_OUTPUT_INDEX_ID ) @QueryParam( "outputIndexId" ) int outputIndexId,
                              @DefaultValue( DEFAULT_DATA_ACCESS_ID ) @QueryParam( "dataAccessId" ) String dataAccessId,
                              @DefaultValue( DEFAULT_BYPASS_CACHE ) @QueryParam( "bypassCache" ) Boolean bypassCache,
                              @DefaultValue( DEFAULT_PAGINATE_QUERY ) @QueryParam( "paginateQuery" ) Boolean paginateQuery,
                              @DefaultValue( DEFAULT_PAGE_SIZE ) @QueryParam( "pageSize" ) int pageSize,
                              @DefaultValue( DEFAULT_PAGE_START ) @QueryParam( "pageStart" ) int pageStart,
                              @DefaultValue( DEFAULT_WRAP_IT_UP ) @QueryParam( "wrapItUp" ) Boolean wrapItUp,
                              @QueryParam( "sortBy" ) List<String> sortBy,
                              @DefaultValue( DEFAULT_JSON_CALLBACK ) @QueryParam( "jsonCallback" ) String jsonCallback,
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
                                  @DefaultValue( DEFAULT_OUTPUT_TYPE ) @QueryParam( "outputType" ) String outputType,
                                  @DefaultValue( DEFAULT_DATA_ACCESS_ID ) @QueryParam( "dataAccessId" ) String dataAccessId ) {
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
                              @DefaultValue( DEFAULT_OUTPUT_TYPE ) @QueryParam( "outputType" ) String outputType ) {
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
    final int outputIndexId = Integer.valueOf( DEFAULT_OUTPUT_INDEX_ID );
    final int pageSize = Integer.valueOf( DEFAULT_PAGE_SIZE );
    final int pageStart = Integer.valueOf( DEFAULT_PAGE_START );

    final boolean bypassCache = Boolean.valueOf( DEFAULT_BYPASS_CACHE );
    final boolean wrapItUp = Boolean.valueOf( DEFAULT_WRAP_IT_UP );
    final boolean paginateQuery = Boolean.valueOf( DEFAULT_PAGINATE_QUERY );

    return getQueryParameters( path, solution, file, outputType, outputIndexId, dataAccessId, bypassCache,
        paginateQuery, pageSize, pageStart, wrapItUp, Collections.emptyList(), DEFAULT_JSON_CALLBACK,
        Collections.emptyMap(), Collections.emptyMap() );
  }

  private DoQueryParameters getQueryParameters( MultivaluedMap<String, String> formParameters ) {
    final String path = formParameters.getFirst("path" );
    final String solution = formParameters.getFirst( "solution" );
    final String file = formParameters.getFirst( "file" );

    final boolean bypassCache = Boolean.valueOf(
        getFirstOrDefault( formParameters, "bypassCache", DEFAULT_BYPASS_CACHE ) );
    final boolean paginateQuery = Boolean.valueOf(
        getFirstOrDefault( formParameters, "paginateQuery", DEFAULT_PAGINATE_QUERY ) );
    final boolean wrapItUp = Boolean.valueOf( getFirstOrDefault( formParameters, "wrapItUp", DEFAULT_BYPASS_CACHE ) );

    final int outputIndexId = Integer.valueOf( getFirstOrDefault( formParameters, "outputIndexId", DEFAULT_OUTPUT_INDEX_ID ) );
    final int pageSize = Integer.valueOf( getFirstOrDefault( formParameters, "pageSize", DEFAULT_PAGE_SIZE ) );
    final int pageStart = Integer.valueOf( getFirstOrDefault( formParameters, "pageStart", DEFAULT_PAGE_START ) );

    final String outputType = getFirstOrDefault( formParameters, "outputType", DEFAULT_OUTPUT_TYPE );
    final String dataAccessId = getFirstOrDefault( formParameters, "dataAccessId", DEFAULT_DATA_ACCESS_ID );
    final String jsonCallback = getFirstOrDefault( formParameters, "jsonCallback", DEFAULT_JSON_CALLBACK );

    final List<String> sortBy = formParameters.getOrDefault( "sortBy", Collections.emptyList() );

    final Map<String, Object> extraParameters = getParameters( formParameters,
        name -> name.startsWith( PREFIX_PARAMETER ), name -> name.replaceFirst( PREFIX_PARAMETER, "" ) );

    final Map<String, Object> extraSettings = getParameters( formParameters,
        name -> name.startsWith( PREFIX_SETTING ), name -> name.replaceFirst( PREFIX_SETTING, "" ) );

    return getQueryParameters( path, solution, file, outputType, outputIndexId, dataAccessId, bypassCache,
        paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, extraParameters, extraSettings );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                int outputIndexId, String dataAccessId, boolean bypassCache,
                                                boolean paginateQuery, int pageSize, int pageStart,
                                                boolean wrapItUp, List<String> sortBy, String jsonCallback,
                                                HttpServletRequest request ) {
    final Map<String, Object> extraParameters = getParameters( request, RequestParameter::isExtraParameter );
    final Map<String, Object> extraSettings = getParameters( request, RequestParameter::isSettingParameter );

    return getQueryParameters( path, solution, file, outputType, outputIndexId, dataAccessId, bypassCache,
        paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, extraParameters, extraSettings );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                int outputIndexId, String dataAccessId, boolean bypassCache,
                                                boolean paginateQuery, int pageSize, int pageStart,
                                                boolean wrapItUp, List<String> sortBy, String jsonCallback,
                                                Map<String, Object> extraParameters, Map<String, Object> extraSettings ) {
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
    queryParameters.setParameters( extraParameters );
    queryParameters.setExtraSettings( extraSettings );

    return queryParameters;
  }

  private String getFirstOrDefault( MultivaluedMap<String, String> multivaluedMap, String key, String defaultValue ) {
    String stringValue = multivaluedMap.getFirst( key );
    return stringValue != null ? stringValue : defaultValue;
  }

  private Map<String, Object> getParameters( MultivaluedMap<String, String> multivaluedMap,
                                             Predicate<String> parameterType, Function<String, String> transformName ) {
    Map<String, Object> parameters = new HashMap<>();

    for( Map.Entry<String, List<String>> entry : multivaluedMap.entrySet() ) {
      final String parameterName = entry.getKey();

      if( parameterType.test( parameterName ) ) {
        final String[] parameterValues = entry.getValue().toArray( new String[0] );

        final String name = transformName.apply( parameterName );
        final Object value = parameterValues.length > 1 ? parameterValues : parameterValues[ 0 ];
        parameters.put( name, value );
      }
    }

    return parameters;
  }

  Map<String, Object> getParameters( HttpServletRequest request, Predicate<RequestParameter> parameterType ) {
    Map<String, Object> parameters = new HashMap<>();

    final Enumeration<String> parameterNames = request.getParameterNames();
    while ( parameterNames.hasMoreElements() ) {
      final String parameterName = parameterNames.nextElement();
      final RequestParameter requestParameter = new RequestParameter( request, parameterName );

      if ( parameterType.test( requestParameter ) ) {
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
