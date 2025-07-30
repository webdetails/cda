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


package pt.webdetails.cda.cache.endpoints;

import java.io.IOException;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;

import pt.webdetails.cda.services.CacheMonitor;
import pt.webdetails.cda.utils.framework.JsonCallHandler;
import pt.webdetails.cpf.utils.MimeTypes;

@Path( "/{plugin}/api/cacheMonitor" )
public class CacheMonitorEndpoint {

  private static final int INDENT_FACTOR = 2;

  @GET
  @Path( "/cacheOverview" )
  @Produces( MimeTypes.JSON )
  public String cacheOverview( @QueryParam( "cdaSettingsId" ) String cdaSettingsId )
    throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().getCachedQueriesOverview( cdaSettingsId ).toString( INDENT_FACTOR );
    } catch ( JSONException e ) {
      throw new WebApplicationException( e );
    }
  }

  @GET
  @Path( "/cached" )
  @Produces( MimeTypes.JSON )
  public String getCached(
    @QueryParam( "cdaSettingsId" ) String cdaSettingsId,
    @QueryParam( "dataAccessId" ) String dataAccessId )
    throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().listQueriesInCache( cdaSettingsId, dataAccessId ).toString( INDENT_FACTOR );
    } catch ( JSONException e ) {
      throw new WebApplicationException( e );
    }
  }

  // post only due to get limit issues
  @POST
  @Path( "/getDetails" )
  @Produces( MimeTypes.JSON )
  public String getCached( @FormParam( "key" ) String encodedCacheKey ) throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().getCacheQueryTable( encodedCacheKey ).toString( INDENT_FACTOR );
    } catch ( JSONException e ) {
      throw new WebApplicationException( e );
    }
  }

  @POST
  @Path( "/removeCache" )
  @Produces( MimeTypes.JSON )
  public String removeCache( @FormParam( "key" ) String encodedCacheKey ) throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().removeQueryFromCache( encodedCacheKey ).toString( INDENT_FACTOR );
    } catch ( Exception e ) {
      return getJsonError( e );
    }
  }

  @POST
  @Path( "/removeAll" )
  @Produces( MimeTypes.JSON )
  public String removeAll( @FormParam( "cdaSettingsId" ) String cdaSettingsId,
                           @FormParam( "dataAccessId" ) String dataAccessId )
    throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().removeAll( cdaSettingsId, dataAccessId ).toString( INDENT_FACTOR );
    } catch ( Exception e ) {
      return getJsonError( e );
    }
  }

  @POST
  @Path( "/shutdown" )
  @Produces( MimeTypes.JSON )
  public String shutdown() throws WebApplicationException, IOException {
    checkAdminPermission();
    try {
      return getMonitor().shutdown().toString( INDENT_FACTOR );
    } catch ( Exception e ) {
      return getJsonError( e );
    }
  }

  private String getJsonError( Exception e ) throws WebApplicationException {
    try {
      return JsonCallHandler.getErrorJson( e.getLocalizedMessage() ).toString( INDENT_FACTOR );
    } catch ( JSONException e1 ) {
      throw new WebApplicationException( e );
    }
  }

  private void checkAdminPermission() throws WebApplicationException {
    if ( !SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() ) ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  private CacheMonitor getMonitor() {
    return new CacheMonitor();
  }

  //Interplugin calls  - Should be moved to a dedicated bean and method signature should be changed
  public String removeAllInterPlugin( @QueryParam( "cdaSettingsId" ) String cdaSettingsId,
                                      @QueryParam( "dataAccessId" ) String dataAccessId )
    throws WebApplicationException, IOException {
    return removeAll( cdaSettingsId, dataAccessId );
  }

}
