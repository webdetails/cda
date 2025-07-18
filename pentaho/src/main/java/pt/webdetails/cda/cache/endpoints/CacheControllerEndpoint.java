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
import java.io.OutputStream;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

import pt.webdetails.cda.cache.scheduler.CdaCacheScheduler;
import pt.webdetails.cpf.messaging.JsonGeneratorSerializable;
import pt.webdetails.cpf.utils.JsonHelper;
import pt.webdetails.cpf.utils.MimeTypes;

@Path( "/{plugin}/api/cacheController" )
/**
 * Cache scheduler api
 */
public class CacheControllerEndpoint {

  @GET
  @Path( "/list" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput cacheOverview() {
    JsonGeneratorSerializable result = getCacheScheduler().listScheduledQueries();
    return toStreamingOutput( result );
  }

  @POST
  @Path( "/change" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput scheduleJob( @FormParam( "object" ) String json ) {
    JsonGeneratorSerializable result = getCacheScheduler().scheduleQueryExecution( json );
    return toStreamingOutput( result );
  }

  @DELETE
  @Path( "/delete" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput deleteJob( @FormParam( "id" ) String id ) {
    JsonGeneratorSerializable result = getCacheScheduler().deleteJob( id );
    return toStreamingOutput( result );
  }

  @POST
  @Path( "/execute" )
  @Produces( MimeTypes.JSON )
  public StreamingOutput executeJob( @FormParam( "id" ) String id ) {
    JsonGeneratorSerializable result = getCacheScheduler().executeJob( id );
    return toStreamingOutput( result );
  }

  private StreamingOutput toStreamingOutput( final JsonGeneratorSerializable json ) {
    return new StreamingOutput() {
      public void write( OutputStream out ) throws IOException, WebApplicationException {
        JsonHelper.writeJson( json, out );
      }
    };
  }

  private CdaCacheScheduler getCacheScheduler() {
    return new CdaCacheScheduler();
  }
}
