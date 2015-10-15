/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cda.cache.endpoints;

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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
