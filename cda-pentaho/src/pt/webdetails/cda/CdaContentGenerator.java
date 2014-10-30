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

package pt.webdetails.cda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IParameterProvider;

import pt.webdetails.cda.cache.monitor.CacheMonitorHandler;
import pt.webdetails.cda.cache.scheduler.CacheScheduleManager;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.services.CacheManager;
import pt.webdetails.cda.services.Editor;
import pt.webdetails.cda.services.ExtEditor;
import pt.webdetails.cda.services.Previewer;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Audited;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.messaging.JsonResult;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.JsonHelper;
import pt.webdetails.cpf.utils.MimeTypes;

import javax.servlet.http.HttpServletResponse;


public class CdaContentGenerator extends SimpleContentGenerator {
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;
  private static final String PREFIX_PARAMETER = "param";
  private static final String PREFIX_SETTING = "setting";

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  @Audited( action = "doQuery" )
  public void doQuery( final OutputStream out ) throws Exception {
    final IParameterProvider requestParams = getRequestParameters();
    DoQueryParameters parameters = getDoQueryParameters( requestParams );
    CdaCoreService service = getCoreService();
    if ( parameters.isWrapItUp() ) {
      writeOut( out, service.wrapQuery( parameters ) );
    } else {
      ExportedQueryResult result = service.doQuery( parameters );
      writeOut( result, getResponse(), out );
    }
  }

  protected void writeOut( ExportedQueryResult result, HttpServletResponse response, OutputStream out )
    throws IOException, ExporterException {
    //We're breaking the write in two because when in InterpluginCalls the output stream to write to is not
    //the one in response
    if ( response != null ) {
      result.writeHeaders( response );
    }
    result.writeOut( out );
  }


  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void unwrapQuery( final OutputStream out ) throws Exception {
    final IParameterProvider requestParams = getRequestParameters();
    String cda = getPath( requestParams );
    CdaCoreService coreService = getCoreService();
    ExportedQueryResult result = coreService.unwrapQuery( cda, requestParams.getStringParameter( "uuid", null ) );
    writeOut( result, getResponse(), out );

  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void listQueries( final OutputStream out ) throws Exception {
    final IParameterProvider requestParams = getRequestParameters();
    CdaCoreService service = getCoreService();
    String cda = getPath( requestParams );
    String outputType = requestParams.getStringParameter( "outputType", "json" );
    ExportedQueryResult result =  service.listQueries( cda, getSimpleExportOptions( outputType ) );
    writeOut( result, getResponse(), out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void listParameters( final OutputStream out ) throws Exception {
    final IParameterProvider requestParams = getRequestParameters();
    DoQueryParameters params = getDoQueryParameters( requestParams );
    CdaCoreService service = getCoreService();
    ExportedQueryResult result = service.listParameters(
            params.getPath(),
            params.getDataAccessId(),
            getSimpleExportOptions( params.getOutputType() ) );
    writeOut( result, getResponse(), out );

  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void canEdit( final OutputStream out ) throws Exception {
    final IParameterProvider requestParams = getRequestParameters();
    String path = requestParams.getStringParameter( "path", null );
    boolean canEdit = getEditor().canEdit( path );
    JsonHelper.writeJson( new JsonResult( true, JsonHelper.toJson( canEdit ) ), out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void getCdaFile( final OutputStream out ) throws Exception {
    try {
      final IParameterProvider requestParams = getRequestParameters();
      // XXX return Json
      String repoPath = requestParams.getStringParameter( "path", null );
      String result = getEditor().getFile( repoPath );
      JsonHelper.writeJson( new JsonResult( true, result ), out );
    } catch ( Exception e ) {
      writeErrorResult( e, out );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.HTML )
  public void extEditor( final OutputStream out ) throws Exception {
    writeOut( out, getExtEditor().getExtEditor() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void writeCdaFile( OutputStream out ) throws Exception {
    try {
      final IParameterProvider requestParams = getRequestParameters();
      String path = getPath( requestParams );
      String data = requestParams.getStringParameter( "data", null );
      if ( data == null ) {
        throw new IllegalArgumentException( "data" );
      }
      if ( StringUtils.isEmpty( path ) ) {
        throw new IllegalArgumentException( "path" );
      }
      boolean result = getEditor().writeFile( path, data );
      JsonHelper.writeJson( new JsonResult( result, path ), out );
    } catch ( Exception e ) {
      writeErrorResult( e, out );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC,  outputType = MimeTypes.HTML )
  public void editFile( final OutputStream out ) throws Exception {
    writeOut( out, getExtEditor().getMainEditor() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getCdaList( final OutputStream out ) throws Exception {
    CdaCoreService service = getCoreService();
    final String outputType = getOutputType( getRequestParameters() );
    service.getCdaList( getSimpleExportOptions( outputType ) );
  }

  @Exposed( accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT )
  public void clearCache( final OutputStream out ) throws Exception {
    String msg = "Cache Cleared Successfully";

    try {
      CdaCoreService service = getCoreService();
      service.clearCache();
    } catch ( Exception cce ) {
      msg = "Method clearCache failed while trying to execute.";
      writeErrorResult( cce, out, msg );
      return;
    }

    writeSuccessResult( out, msg );
  }

  @Exposed( accessLevel = AccessLevel.ADMIN )
  public void cacheMonitor( final OutputStream out ) throws Exception {
    //XXX
    IParameterProvider parameters = getRequestParameters();
    String method = getMethodArg( out );
    if ( method == null ) {
      writeErrorResult( new IllegalArgumentException( "method not specified" ), out );
      return;
    }
    CacheMonitorHandler.getInstance().handleCall(method, parameters, out );
  }

  @Override
  public Log getLogger() {
    return logger;
  }



  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void previewQuery( final OutputStream out ) throws Exception {
    Previewer previewer = new Previewer( PluginEnvironment.env().getUrlProvider(),
            CdaEngine.getEnvironment().getRepo() );
    writeOut( out, previewer.previewQuery( getParamsWithPath( getRequestParameters() ).getPath() ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.CSS )
  public void getCssResource( final OutputStream out ) throws Exception {
    getSystemResource( out, getRequestParameters().getStringParameter( "resource", null ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JAVASCRIPT )
  public void getJsResource( final OutputStream out ) throws Exception {
    getSystemResource( out, getRequestParameters().getStringParameter( "resource", null ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeTypes.JSON )
  public void listDataAccessTypes( final OutputStream out ) throws Exception {
    boolean refreshCache = Boolean.parseBoolean( getRequestParameters().getStringParameter( "refreshCache", "false" ) );

    CdaCoreService coreService = getCoreService();
    writeOut( out, coreService.listDataAccessTypes( refreshCache ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void cacheController( OutputStream out ) throws IOException {
    String method = getMethodArg( out );
    if ( method == null ) {
      writeErrorResult( new IllegalArgumentException( "method not specified" ), out );
      return;
    }
    String obj = getRequestParameters().getStringParameter( "object", null );
    CacheScheduleManager.getInstance().handleCall( method, obj, out );
  }


  /**
   * get method arg as either path param or query arg
   */
  private String getMethodArg( OutputStream out ) throws IOException {
    String method = getRequestParameters().getStringParameter( "method", null );
    if ( StringUtils.isEmpty( method ) ) {
      return getPathParameter( 1 );
    }
    return method;
  }

  private String getPathParameter( int pos ) {
    String path = getPathParameters().getStringParameter( "path", null );
    if ( !StringUtils.isEmpty( path ) ) {
      String[] pathSections = StringUtils.split( path, '/' );
      if ( pathSections.length > pos ) {
        return pathSections[pos];
      }
    }
    return null;
  }

  @Exposed( accessLevel = AccessLevel.ADMIN )
  public void manageCache( final OutputStream out ) throws Exception {
    CacheManager cacheMan = new CacheManager( PluginEnvironment.env().getUrlProvider(),
            CdaEngine.getEnvironment().getRepo() );
    writeOut( out, cacheMan.manageCache() );
  }

  @Override
  public String getPluginName() {
    return "cda";
  }

  private DoQueryParameters getDoQueryParameters( final IParameterProvider requestParams ) {
    DoQueryParameters parameters = getParamsWithPath( requestParams );

    parameters.setBypassCache( Boolean.parseBoolean( requestParams.getStringParameter( "bypassCache", "false" ) ) );
    parameters.setDataAccessId( requestParams.getStringParameter( "dataAccessId", "<blank>" ) );
    parameters.setOutputIndexId( Integer.parseInt( requestParams.getStringParameter( "outputIndexId", "1" ) ) );
    parameters.setOutputType( requestParams.getStringParameter( "outputType", "json" ) );
    parameters.setPageSize( (int) requestParams.getLongParameter( "pageSize", 0 ) );
    parameters.setPageStart( (int) requestParams.getLongParameter( "pageStart", 0 ) );
    parameters.setPaginateQuery( Boolean.parseBoolean( requestParams.getStringParameter( "paginateQuery", "false" ) ) );

    Map<String, Object> extraParams = new HashMap<String, Object>();
    Map<String, Object> extraSettings = new HashMap<String, Object>();
    @SuppressWarnings( "unchecked" )
    final Iterator<String> params = (Iterator<String>) requestParams.getParameterNames();
    while ( params.hasNext() ) {
      final String param = params.next();

      if ( param.startsWith( PREFIX_PARAMETER ) ) {
        extraParams.put( param.substring( PREFIX_PARAMETER.length() ), requestParams.getParameter( param ) );
      } else if ( param.startsWith( PREFIX_SETTING ) ) {
        extraSettings.put( param.substring( PREFIX_SETTING.length() ), requestParams.getStringParameter( param, "" ) );
      }
    }

    parameters.setParameters( extraParams );

    parameters.setExtraSettings( extraSettings );

    final ArrayList<String> sortBy = new ArrayList<String>();
    String[] def =
    {
    };
    for ( Object obj : requestParams.getArrayParameter( "sortBy", def ) ) {
      if ( !( obj ).equals( "" ) ) {
        sortBy.add( (String) obj );
      }
    }
    parameters.setSortBy( sortBy );
    parameters.setWrapItUp( requestParams.getStringParameter( "wrapItUp", null ) != null );
    return parameters;
  }

  private Editor getEditor() {
    return new Editor();
  }
  private ExtEditor getExtEditor() {
    return new ExtEditor( PluginEnvironment.env().getUrlProvider(), CdaEngine.getEnvironment().getRepo() );
  }

  private DoQueryParameters getParamsWithPath( IParameterProvider requestParams ) {
    DoQueryParameters dqp = null;
    try {
      dqp = new DoQueryParameters(
        URLDecoder.decode( (String) requestParams.getParameter( "path" ), "UTF-8" ),
        (String) requestParams.getParameter( "solution" ),
        (String) requestParams.getParameter( "file" ) );
    } catch ( UnsupportedEncodingException use ) {
      //This won't happen as long as UTF-8 is used
    }
    return dqp;
  }

  private String getPath( IParameterProvider requestParams ) {
    DoQueryParameters parameters = getParamsWithPath( requestParams );
    return parameters.getPath();
  }

  private CdaCoreService getCoreService() {
    return new CdaCoreService();
  }

  private String getOutputType( IParameterProvider requestParams ) {
    return requestParams.getStringParameter( "outputType", "json" );
  }
  private ExportOptions getSimpleExportOptions( final String outputType ) {
    return new ExportOptions() {

      public String getOutputType() {
        return outputType;
      }

      public Map<String, String> getExtraSettings() {
        return Collections.<String, String>emptyMap();
      }
    };
  }

  private void writeErrorResult( Exception e, OutputStream out ) throws IOException {
    writeErrorResult( e, out, "" );
  }

  private void writeErrorResult( Exception e, OutputStream out, String message ) throws IOException {
    logger.error( message, e );
    JsonHelper.writeJson( new JsonResult( false, e.getLocalizedMessage() ), out );
  }

  private void writeSuccessResult( OutputStream out, String message) throws IOException {
    JsonHelper.writeJson( new JsonResult( true, message ), out );
  }

  private void getSystemResource( final OutputStream out, String resource ) throws Exception {
    IReadAccess repo = getRepositoryAccess();
    if ( repo.fileExists( resource ) ) {
      InputStream in = null;
      try {
        in = repo.getFileInputStream( resource );
        IOUtils.copy( in, out );
        out.flush();
      } finally {
        IOUtils.closeQuietly( in );
      }
    }
  }

  private IReadAccess getRepositoryAccess() {
    return CdaEngine.getRepo().getPluginSystemReader( null );
  }
}
