/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package pt.webdetails.cda.endpoints;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
  public Response doQueryPost( MultivaluedMap<String, String> formParameters ) {
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
                              @QueryParam( "outputType" ) String outputType,
                              @QueryParam( "outputIndexId" ) Integer outputIndexId,
                              @QueryParam( "dataAccessId" ) String dataAccessId,
                              @QueryParam( "bypassCache" ) Boolean bypassCache,
                              @QueryParam( "paginateQuery" ) Boolean paginateQuery,
                              @QueryParam( "pageSize" ) Integer pageSize,
                              @QueryParam( "pageStart" ) Integer pageStart,
                              @QueryParam( "wrapItUp" ) Boolean wrapItUp,
                              @QueryParam( "sortBy" ) List<String> sortBy,
                              @QueryParam( "jsonCallback" ) String jsonCallback,
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
                               @QueryParam( "outputType" ) String outputType ) {
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
                                  @QueryParam( "outputType" ) String outputType,
                                  @QueryParam( "dataAccessId" ) String dataAccessId ) {
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
                              @QueryParam( "outputType" ) String outputType ) {
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
  public Response listDataAccessTypes( @QueryParam( "refreshCache" ) boolean refreshCache ) {
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
    return getQueryParameters( path, solution, file, null, null );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType ) {
    return getQueryParameters( path, solution, file, outputType, null );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                String dataAccessId ) {
    return getQueryParameters( path, solution, file, outputType, null, dataAccessId, null,
        null, null, null, null, null, null,
        null, null );
  }

  private DoQueryParameters getQueryParameters( MultivaluedMap<String, String> formParameters ) {
    final String path = formParameters.getFirst( "path" );
    final String solution = formParameters.getFirst( "solution" );
    final String file = formParameters.getFirst( "file" );

    final Boolean bypassCache = getFirstBoolean( formParameters, "bypassCache" );
    final Boolean paginateQuery = getFirstBoolean( formParameters, "paginateQuery" );
    final Boolean wrapItUp = getFirstBoolean( formParameters, "wrapItUp" );

    final Integer outputIndexId = getFirstInteger( formParameters, "outputIndexId" );
    final Integer pageSize = getFirstInteger( formParameters, "pageSize" );
    final Integer pageStart = getFirstInteger( formParameters, "pageStart" );

    final String outputType = formParameters.getFirst( "outputType" );
    final String dataAccessId = formParameters.getFirst( "dataAccessId" );
    final String jsonCallback = formParameters.getFirst( "jsonCallback" );

    final List<String> sortBy = formParameters.get( "sortBy" );

    final Map<String, Object> extraParameters = getParameters( formParameters, PREFIX_PARAMETER );
    final Map<String, Object> extraSettings = getParameters( formParameters, PREFIX_SETTING );

    return getQueryParameters( path, solution, file, outputType, outputIndexId, dataAccessId, bypassCache,
        paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, extraParameters, extraSettings );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                Integer outputIndexId, String dataAccessId, Boolean bypassCache,
                                                Boolean paginateQuery, Integer pageSize, Integer pageStart,
                                                Boolean wrapItUp, List<String> sortBy, String jsonCallback,
                                                HttpServletRequest request ) {
    final Map<String, Object> extraParameters = getParameters( request, PREFIX_PARAMETER );
    final Map<String, Object> extraSettings = getParameters( request, PREFIX_SETTING );

    return getQueryParameters( path, solution, file, outputType, outputIndexId, dataAccessId, bypassCache,
        paginateQuery, pageSize, pageStart, wrapItUp, sortBy, jsonCallback, extraParameters, extraSettings );
  }

  private DoQueryParameters getQueryParameters( String path, String solution, String file, String outputType,
                                                Integer outputIndexId, String dataAccessId, Boolean bypassCache,
                                                Boolean paginateQuery, Integer pageSize, Integer pageStart,
                                                Boolean wrapItUp, List<String> sortBy, String jsonCallback,
                                                Map<String, Object> extraParameters, Map<String, Object> extraSettings ) {
    DoQueryParameters queryParameters = new DoQueryParameters( path, solution, file );

    if ( bypassCache != null ) {
      queryParameters.setBypassCache( bypassCache );
    }

    if ( dataAccessId != null ) {
      queryParameters.setDataAccessId( dataAccessId );
    }

    if ( outputIndexId != null ) {
      queryParameters.setOutputIndexId( outputIndexId );
    }

    if ( outputType != null ) {
      queryParameters.setOutputType( outputType );
    }

    if ( pageSize != null ) {
      queryParameters.setPageSize( pageSize );
    }

    if ( pageStart != null ) {
      queryParameters.setPageStart( pageStart );
    }

    if ( paginateQuery != null ) {
      queryParameters.setPaginateQuery( paginateQuery );
    }

    if ( sortBy != null && !sortBy.isEmpty() ) {
      queryParameters.setSortBy( sortBy );
    }

    if ( wrapItUp != null ) {
      queryParameters.setWrapItUp( wrapItUp );
    }

    if ( jsonCallback != null ) {
      queryParameters.setJsonCallback( jsonCallback );
    }

    if ( extraParameters != null && !extraParameters.isEmpty() ) {
      queryParameters.setParameters( extraParameters );
    }

    if ( extraSettings != null && !extraSettings.isEmpty() ) {
      queryParameters.setExtraSettings( extraSettings );
    }

    return queryParameters;
  }

  private Boolean getFirstBoolean( MultivaluedMap<String, String> multivaluedMap, String key ) {
    String value = multivaluedMap.getFirst( key );
    if ( value == null ) {
      return null;
    }

    return Boolean.valueOf( value );
  }

  private Integer getFirstInteger( MultivaluedMap<String, String> multivaluedMap, String key ) {
    String value = multivaluedMap.getFirst( key );
    if ( value == null ) {
      return null;
    }

    return Integer.valueOf( value );
  }

  Map<String, Object> getParameters( MultivaluedMap<String, String> multivaluedMap, String parameterType ) {
    Map<String, Object> parameters = new HashMap<>();

    multivaluedMap.forEach( ( parameterName, parameterValueList ) -> {
      if ( parameterName.startsWith( parameterType ) ) {
        final String[] parameterValues = parameterValueList.toArray( new String[ 0 ] );

        final String name = parameterName.replaceFirst( parameterType, "" );
        final Object value = parameterValues.length > 1 ? parameterValues : parameterValues[ 0 ];
        parameters.put( name, value );
      }
    } );

    return parameters;
  }

  Map<String, Object> getParameters( HttpServletRequest request, String parameterType ) {
    Map<String, Object> parameters = new HashMap<>();

    final Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.forEach( ( parameterName, parameterValues ) -> {
      if ( parameterName.startsWith( parameterType ) ) {

        final String name = parameterName.replaceFirst( parameterType, "" );
        final Object value = parameterValues.length > 1 ? parameterValues : parameterValues[ 0 ];
        parameters.put( name, value );
      }
    } );

    return parameters;
  }
}
