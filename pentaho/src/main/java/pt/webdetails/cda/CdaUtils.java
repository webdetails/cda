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

package pt.webdetails.cda;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.services.CacheManager;
import pt.webdetails.cda.services.Editor;
import pt.webdetails.cda.services.ExtEditor;
import pt.webdetails.cda.services.MondrianSchemaFlushService;
import pt.webdetails.cda.services.Previewer;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.CdaSettingsReadException;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.AuditHelper;
import pt.webdetails.cda.utils.AuditHelper.QueryAudit;
import pt.webdetails.cda.utils.CorsUtil;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cda.utils.Messages;
import pt.webdetails.cda.utils.QueryParameters;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.messaging.JsonGeneratorSerializable;
import pt.webdetails.cpf.messaging.JsonResult;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.JsonHelper;
import pt.webdetails.cpf.utils.MimeTypes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;

@Path( "/{plugin}/api" )
public class CdaUtils {
  private static final Log logger = LogFactory.getLog( CdaUtils.class );
  private static final String DOQUERY_GETSOLPATH = "Do Query: getSolPath:";

  // TODO: safer to get from repos?
  private static final Pattern CDA_PATH = Pattern.compile( "^[^:]*([^/]+)[^?]*" );

  private QueryParameters queryParametersUtil;

  public CdaUtils() {
    this.queryParametersUtil = new QueryParameters();
  }

  public CdaUtils( QueryParameters queryParametersUtil ) {
    this.queryParametersUtil = queryParametersUtil;
  }

  public void setQueryParametersUtil( QueryParameters queryParametersUtil ) {
    this.queryParametersUtil = queryParametersUtil;
  }

  public QueryParameters getQueryParametersUtil() {
    return queryParametersUtil;
  }

  protected static String getEncoding() {
    return CharsetHelper.getEncoding();
  }

  // TODO: wildcard for exported types?
  @GET
  @Path( "/doQuery" )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput doQueryGet( @Context UriInfo uriInfo,
                                     @Context HttpServletRequest servletRequest,
                                     @Context HttpServletResponse servletResponse ) {
    setCorsHeaders( servletRequest, servletResponse );

    return doQuery( uriInfo.getQueryParameters(), servletResponse );
  }

  @POST
  @Path( "/doQuery" )
  @Consumes( APPLICATION_FORM_URLENCODED )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput doQueryPost( MultivaluedMap<String, String> formParams,
                                      @Context HttpServletRequest servletRequest,
                                      @Context HttpServletResponse servletResponse ) {
    setCorsHeaders( servletRequest, servletResponse );

    MultivaluedMap<String, String> params = formParams;
    if ( formParams.size() == 0 ) {
      // CDA-72: CAS http filters will clear out formParams - try to get data from the request parameter Map
      params = getParameterMapFromRequest( servletRequest );
    }

    return doQuery( params, servletResponse );
  }

  @VisibleForTesting
  protected ExportedQueryResult doQueryInternal( DoQueryParameters parameters ) throws Exception {
    CdaCoreService core = getCdaCoreService();

    return core.doQuery( parameters );
  }

  public StreamingOutput doQuery( MultivaluedMap<String, String> params,
                                  HttpServletResponse servletResponse ) {

    AuditHelper auditHelper = new AuditHelper( CdaUtils.class, getPentahoSession() );

    final String path = params.get( "path" ).get( 0 );

    try ( QueryAudit qa = auditHelper.startQuery( path, getParameterProvider( params ) ) ) {
      final DoQueryParameters parameters = queryParametersUtil.getDoQueryParameters( params );
      if ( parameters.isWrapItUp() ) {
        return wrapQuery( parameters );
      }

      final ExportedQueryResult result = doQueryInternal( parameters );
      result.writeHeaders( servletResponse );

      return toStreamingOutput( result );
    } catch ( Exception ex ) {
      throw new WebApplicationException( ex, Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  private IParameterProvider getParameterProvider( MultivaluedMap<String, String> params ) {
    return new SimpleParameterProvider( params );
  }

  private StreamingOutput wrapQuery( DoQueryParameters parameters ) throws Exception {
    final String uuid = getCdaCoreService().wrapQuery( parameters );

    return out -> IOUtils.write( uuid, out );
  }

  @GET
  @Path( "/unwrapQuery" )
  @Produces()
  public Response unwrapQuery( @QueryParam( "path" ) String path,
                           @QueryParam( "uuid" ) String uuid,
                           @Context HttpServletResponse servletResponse,
                           @Context HttpServletRequest servletRequest ) {
    try {

      ExportedQueryResult result = getCdaCoreService().unwrapQuery( path, uuid );

      setCorsHeaders( servletRequest, servletResponse );
      result.writeHeaders( servletResponse );

      StreamingOutput streamingOutput = output -> {
        try {
          result.writeOut( output );
        } catch ( ExporterException e ) {
          logger.error( e );
          throw new WebApplicationException( e, Response.Status.INTERNAL_SERVER_ERROR );
        }
      };

      return Response.ok( streamingOutput ).type( servletResponse.getContentType() ).build();

    } catch ( Exception e ) {
      logger.error( e );
      throw new WebApplicationException( e, Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET
  @Path( "/listQueries" )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput listQueries( @QueryParam( "path" ) String path,
                                      @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType,
                                      @Context HttpServletResponse servletResponse ) {
    if ( StringUtils.isEmpty( path ) ) {
      throw new IllegalArgumentException( "No path provided" );
    }

    logger.debug( DOQUERY_GETSOLPATH + PentahoSystem.getApplicationContext().getSolutionPath( path ) );

    ExportedQueryResult result;
    try {
      result = getCdaCoreService().listQueries( path, getSimpleExportOptions( outputType ) );
    } catch ( Exception e ) {
      logger.error( e );
      throw new WebApplicationException( e );
    }

    return toStreamingOutput( result );
  }

  private StreamingOutput toStreamingOutput( final ExportedQueryResult result ) {
    return out -> {
      try {
        result.writeOut( out );
      } catch ( ExporterException e ) {
        throw new WebApplicationException( e );
      }
    };
  }

  private StreamingOutput toStreamingOutput( final JsonGeneratorSerializable json ) {
    return out -> JsonHelper.writeJson( json, out );
  }

  private StreamingOutput toErrorResult( final Exception ex ) {
    logger.error( ex.getLocalizedMessage(), ex );

    return out -> JsonHelper.writeJson( new JsonResult( false, ex.getLocalizedMessage() ), out );
  }

  private ExportOptions getSimpleExportOptions( final String outputType ) {
    return getSimpleExportOptions( outputType,  Collections.emptyMap() );
  }

  private ExportOptions getSimpleExportOptions( final String outputType, final Map<String, String> extraSettings ) {
    return new ExportOptions() {

      public String getOutputType() {
        return outputType;
      }

      public Map<String, String> getExtraSettings() {
        return extraSettings;
      }
    };
  }

  @GET
  @Path( "/listParameters" )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput listParameters( @QueryParam( "path" ) String path,
                                         @QueryParam( "dataAccessId" ) String dataAccessId,
                                         @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType ) {
    logger.debug( DOQUERY_GETSOLPATH + path );

    try {
      final ExportOptions exportOptions = getSimpleExportOptions( outputType );
      final ExportedQueryResult result = getCdaCoreService().listParameters( path, dataAccessId, exportOptions );

      return toStreamingOutput( result );
    } catch ( Exception e ) {
      logger.error( e );
      throw new WebApplicationException( e );
    }
  }

  private void setExporterHeaders( final Exporter exporter, final ResponseBuilder responseBuilder ) {
    String mimeType = exporter.getMimeType();
    if ( mimeType != null ) {
      responseBuilder.header( "Content-Type", mimeType );
    }

    String attachmentName = exporter.getAttachmentName();
    if ( attachmentName != null ) {
      responseBuilder.header( "content-disposition", "attachment; filename=" + attachmentName );
    }
  }

  @GET
  @Path( "/getCdaFile" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput getCdaFile( @QueryParam( "path" ) String path ) {
    String filePath = StringUtils.replace( path, "///", "/" );

    JsonGeneratorSerializable json;
    try {
      String document = getEditor().getFile( filePath );
      if ( document != null ) {
        json = new JsonResult( true, document );
      } else {
        json = new JsonResult( false, "Unable to read file." );
      }
    } catch ( Exception e ) {
      return toErrorResult( e );
    }

    return toStreamingOutput( json );
  }

  @GET
  @Path( "/canEdit" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput canEdit( @QueryParam( "path" ) String path ) {
    boolean canEdit = getEditor().canEdit( path );
    return toStreamingOutput( new JsonResult( true, JsonHelper.toJson( canEdit ) ) );
  }

  private Editor getEditor() {
    return new Editor();
  }

  @POST
  @Path( "/writeCdaFile" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput writeCdaFile( @FormParam( "path" ) String path,
                                       @FormParam( "data" ) String data ) {
    // TODO: Validate the filename in some way, shape or form!
    if ( data == null ) {
      logger.error( "writeCdaFile: no data to save provided " + path );
      return toStreamingOutput( new JsonResult( false, "No Data!" ) );
    }

    try {
      return toStreamingOutput( new JsonResult( getEditor().writeFile( path, data ), path ) );
    } catch ( Exception e ) {
      return toErrorResult( e );
    }
  }

  @GET
  @Path( "/getCdaList" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getCdaList( @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType )
      throws UnsupportedExporterException {

    final CdaEngine engine = CdaEngine.getInstance();

    final ExportOptions exportOptions = getSimpleExportOptions( outputType, null );
    TableExporter exporter = engine.getExporter( exportOptions );

    StreamingOutput streamingOutput = output -> {
      try {
        exporter.export( output, engine.getCdaList() );
      } catch ( ExporterException e ) {
        logger.error( e );
      }
    };

    ResponseBuilder responseBuilder =  Response.ok( streamingOutput );

    setExporterHeaders( exporter, responseBuilder );

    return responseBuilder.build();
  }

  @GET
  @Path( "/clearCache" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String clearCache( @Context HttpServletResponse servletResponse,
                            @Context HttpServletRequest servletRequest ) throws IOException {
    String msg = "Cache Cleared Successfully";

    // Check if user is admin
    boolean accessible = SystemUtils.canAdminister();
    if ( !accessible ) {
      msg = "Method clearCache not exposed or user does not have required permissions.";

      logger.error( msg );
      servletResponse.sendError( HttpServletResponse.SC_FORBIDDEN, msg );
      return msg;
    }

    try {
      CdaEngine.getInstance().getSettingsManager().clearCache();
      AbstractDataAccess.clearCache();
    } catch ( Exception cce ) {
      msg = "Method clearCache failed while trying to execute.";

      logger.error( msg, cce );
      servletResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg );
    }

    return msg;
  }

  /**
   * Flushes mondrian schema used by MDX connection(s)
   * @param path CDA file path
   * @param connectionId MDX connection (optional)
   * @return schemas flushed
   */
  @GET
  @Path( "flushMondrianSchema" )
  @Produces( MediaType.TEXT_PLAIN )
  public Response clearCatalogSchema(
      @QueryParam( "path" ) String path,
      @QueryParam( "connectionId" ) String connectionId ) {
    try {
      CdaSettings cda = CdaEngine.getInstance().getSettingsManager().parseSettingsFile( path );
      MondrianSchemaFlushService flusher = new MondrianSchemaFlushService();
      return Response.ok( flusher.flushCdaMondrianCache( cda, connectionId ) ).build();
    } catch ( CdaSettingsReadException | AccessDeniedException | UnknownConnectionException e ) {
      logger.error( e.getMessage(), e );
      return Response.status( Response.Status.BAD_REQUEST ).entity( e.getLocalizedMessage() ).build();
    } catch ( InvalidConnectionException e ) {
      logger.error( e.getMessage(), e );
      return Response.serverError().entity( e.getLocalizedMessage() ).build();
    }
  }

  @GET
  @Path( "/editFile" )
  @Produces( MimeTypes.HTML )
  public String editFile( @QueryParam( "path" ) String path ) throws IOException {
    if ( StringUtils.isEmpty( path ) ) {
      throw new WebApplicationException( 400 );
    }

    if ( !CdaEngine.getEnvironment().canCreateContent() ) {
      return Messages.getString( "CdaUtils.ERROR_ACCESS_DENIED" );
    }

    return getExtEditor().getMainEditor();
  }

  @GET
  @Path( "/extEditor" )
  @Produces( MimeTypes.HTML )
  public String getExtEditor( @QueryParam( "path" ) String path ) throws IOException {
    if ( StringUtils.isEmpty( path ) ) {
      throw new WebApplicationException( 400 );
    }

    return getExtEditor().getExtEditor();
  }

  /**
   * called by content generator
   */
  public void editFile( String path, @Context HttpServletResponse servletResponse ) throws IOException {
    final String editFileUrl = PluginEnvironment.env().getUrlProvider().getPluginBaseUrl() + "editFile?path=" + path;

    servletResponse.sendRedirect( editFileUrl );
  }

  public void previewQuery( String path, @Context HttpServletResponse servletResponse ) throws IOException {
    final String previewQueryUrl = PluginEnvironment.env().getUrlProvider()
            .getPluginBaseUrl() + "previewQuery?path=" + path;

    servletResponse.sendRedirect( previewQueryUrl );
  }

  private CacheManager getCacheManager() {
    return new CacheManager( PluginEnvironment.env().getUrlProvider(), CdaEngine.getEnvironment().getRepo() );
  }

  @GET
  @Path( "/previewQuery" )
  @Produces( MimeTypes.HTML )
  public String previewQuery( @Context HttpServletRequest servletRequest ) throws Exception {
    String path = getPath( servletRequest );

    try {
      checkFileExists( path );
    } catch ( Exception e ) {
      logger.error( "Error on trying to read: " + path, e );
      throw e;
    }

    return getPreviewer().previewQuery( path );
  }

  @VisibleForTesting
  void checkFileExists( String path ) throws CdaSettingsReadException, AccessDeniedException {
    CdaEngine.getInstance().getSettingsManager().parseSettingsFile( path );
  }

  private String getPath( HttpServletRequest servletRequest ) {
    String path = servletRequest.getParameter( "path" );
    if ( !StringUtils.isEmpty( path ) ) {
      return path;
    }

    String uri = servletRequest.getRequestURI();
    Matcher pathFinder = CDA_PATH.matcher( uri );
    if ( pathFinder.lookingAt() ) {
      path = pathFinder.group( 1 );

      return path.replaceAll( ":", "/" );
    }

    return null;
  }

  @VisibleForTesting
  Previewer getPreviewer() {
    return new Previewer( PluginEnvironment.env().getUrlProvider(), CdaEngine.getEnvironment().getRepo() );
  }

  private ExtEditor getExtEditor() {
    return new ExtEditor( PluginEnvironment.env().getUrlProvider(), CdaEngine.getEnvironment().getRepo() );
  }

  /**
   * For CDE discovery
   */
  @GET
  @Path( "/listDataAccessTypes" )
  @Produces( APPLICATION_JSON )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String listDataAccessTypes( @DefaultValue( "false" ) @QueryParam( "refreshCache" ) Boolean refreshCache ) {
    DataAccessConnectionDescriptor[] data =
      CdaEngine.getInstance().getSettingsManager().getDataAccessDescriptors( refreshCache );

    StringBuilder output = new StringBuilder();
    if ( data != null ) {
      output.append( "{\n" );

      for ( DataAccessConnectionDescriptor datum : data ) {
        output.append( datum.toJSON() ).append( ",\n" );
      }

      return output.toString().replaceAll( ",\n\\z", "\n}" );
    } else {
      return ""; // XXX
    }
  }

  @GET
  @Path( "/manageCache" )
  @Produces( MimeTypes.HTML )
  public String manageCache() throws AccessDeniedException, IOException {
    return getCacheManager().manageCache();
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  @VisibleForTesting
  CdaCoreService getCdaCoreService() {
    return new CdaCoreService( CdaEngine.getInstance() );
  }

  /**
   * @deprecated (Interplugin calls - Should be moved to a dedicated bean and method signature should be changed
   * Compatibility with CDF 5 - Trunk)
   */
  @Deprecated
  public void listQueriesInterPluginOld( @QueryParam( "path" ) String path,
                                         @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType,
                                         @Context HttpServletResponse servletResponse,
                                         @Context HttpServletRequest servletRequest )
    throws IOException {
    StreamingOutput so = listQueries( path, outputType, servletResponse );

    so.write( servletResponse.getOutputStream() );
  }

  /**
   * @deprecated (Interplugin calls - Should be moved to a dedicated bean and method signature should be changed
   * Compatibility with CDF 5 - Trunk)
   */
  @Deprecated
  public void doQueryInterPluginOld( @Context HttpServletResponse servletResponse,
                                     @Context HttpServletRequest servletRequest ) throws Exception {
    ExportedQueryResult result = doQueryInternal( queryParametersUtil.getDoQueryParameters( getParameterMapFromRequest( servletRequest ) ) );
    result.writeResponse( servletResponse );
  }

  public String doQueryInterPlugin( @Context HttpServletRequest servletRequest ) throws Exception {
    return doQueryInternal( queryParametersUtil.getDoQueryParameters( getParameterMapFromRequest( servletRequest ) ) ).asString();
  }

  private MultivaluedMap<String, String> getParameterMapFromRequest( HttpServletRequest servletRequest ) {
    MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

    final Enumeration<String> enumeration = servletRequest.getParameterNames();
    while ( enumeration.hasMoreElements() ) {
      final String param = enumeration.nextElement();

      final String[] values = servletRequest.getParameterValues( param );
      if ( values.length == 1 ) {
        params.add( param, values[ 0 ] );
      } else {
        List<String> list = new ArrayList<>();
        Collections.addAll( list, values );

        params.put( param, list ); // assigns the array
      }
    }

    return params;
  }

  @VisibleForTesting
  void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
    CorsUtil.getInstance().setCorsHeaders( request, response );
  }

  /**
   * @deprecated (Adding this because of compatibility with the reporting plugin on 5.0.1. The cda datasource on the
   * reporting plugin is expecting this signature)
   */
  @Deprecated
  public void listParameters( @QueryParam( "path" ) String path,
                              @QueryParam( "solution" ) String solution,
                              @QueryParam( "file" ) String file,
                              @DefaultValue( "json" ) @QueryParam( "outputType" ) String outputType,
                              @DefaultValue( "<blank>" ) @QueryParam( "dataAccessId" ) String dataAccessId,
                              @Context HttpServletResponse servletResponse,
                              @Context HttpServletRequest servletRequest ) {


    logger.debug( DOQUERY_GETSOLPATH + path );
    try {
      final ExportOptions exportOptions = getSimpleExportOptions( outputType );

      ExportedQueryResult result = getCdaCoreService().listParameters( path, dataAccessId, exportOptions );
      result.writeOut( servletResponse.getOutputStream() );
    } catch ( Exception e ) {
      logger.error( e );
      throw new WebApplicationException( e );
    }
  }


  @Deprecated
  public void doQueryPost( @FormParam( "path" ) String path,
                           @DefaultValue( "json" ) @FormParam( "outputType" ) String outputType,
                           @DefaultValue( "1" ) @FormParam( "outputIndexId" ) int outputIndexId,
                           @DefaultValue( "1" ) @FormParam( "dataAccessId" ) String dataAccessId,
                           @DefaultValue( "false" ) @FormParam( "bypassCache" ) Boolean bypassCache,
                           @DefaultValue( "false" ) @FormParam( "paginateQuery" ) Boolean paginateQuery,
                           @DefaultValue( "0" ) @FormParam( "pageSize" ) int pageSize,
                           @DefaultValue( "0" ) @FormParam( "pageStart" ) int pageStart,
                           @DefaultValue( "false" ) @FormParam( "wrapItUp" ) Boolean wrapItUp,
                           @FormParam( "sortBy" ) List<String> sortBy,
                           @Context HttpServletResponse servletResponse,
                           @Context HttpServletRequest servletRequest ) throws Exception {

    DoQueryParameters parameters = new DoQueryParameters( path );
    parameters.setOutputType( outputType );
    parameters.setOutputIndexId( outputIndexId );
    parameters.setDataAccessId( dataAccessId );
    parameters.setBypassCache( bypassCache );
    parameters.setPageSize( pageSize );
    parameters.setPageStart( pageStart );
    parameters.setWrapItUp( wrapItUp );
    parameters.setSortBy( sortBy );

    ExportedQueryResult result = doQueryInternal( parameters );
    result.writeResponse( servletResponse );
  }
}
