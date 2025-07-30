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

import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import pt.webdetails.cpf.utils.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;


@Path( "/cda/api" )
public class CdaUtils {
  private static final Log logger = LogFactory.getLog( CdaUtils.class );

  private static String CDA_HOST = System.getProperty( "cda.hostname" );
  private static String CDA_PORT = System.getProperty( "cda.port" );
  private static String USER = System.getProperty( "repos.user" );
  private static String PASS = System.getProperty( "repos.password" );
  private String urlDoQuery = "http://" + CDA_HOST + ":"
      + CDA_PORT + "/cxf/cda/cda/api/utils/doQuery";
  private String urlListDataAccessTypes = "http://" + CDA_HOST + ":"
    + CDA_PORT + "/cxf/cda/cda/api/utils/listDataAccessTypes";

  public CdaUtils() {
    String serverFlag = System.getProperty( "cda.to_server" );
    if ( serverFlag != null && serverFlag.equals( "1" ) ) {
      urlDoQuery = "http://" + CDA_HOST + ":" + CDA_PORT + "/pentaho/plugin/cda/api/doQuery";
      urlListDataAccessTypes = "http://" + CDA_HOST + ":" + CDA_PORT + "/pentaho/plugin/cda/api/listDataAccessTypes";
    }
  }

  private Client getClientInitialized() {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register( MultiPartFeature.class );
    Client client = ClientBuilder.newClient( clientConfig );
    client.register( HttpAuthenticationFeature.basic( USER, PASS ) );
    return client;
  }

  @GET
  @Path( "/doQuery" )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput doQueryGet( @Context UriInfo urii, @Context HttpServletRequest servletRequest,
                                    @Context HttpServletResponse servletResponse ) throws WebApplicationException {
    //proxy request to the real CDA Endpoint
    String url = urlDoQuery;

    //Init
    Client client = getClientInitialized();

    //Invoke Rest endpoint with same params
    WebTarget webResource = client.target( url );

    // add parameters
    if ( urii != null && urii.getQueryParameters() != null ) {
      Iterator<String> it = urii.getQueryParameters().keySet().iterator();
      String key;
      while ( it.hasNext() ) {
        key = it.next();
        webResource = webResource.queryParam( key, urii.getQueryParameters().get( key ).get( 0 ) );
      }
    }

    try {
      Response response = webResource.request( MediaType.APPLICATION_FORM_URLENCODED )
        .get( Response.class );

      if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
        InputStream in = response.readEntity( InputStream.class );

        return new StreamingOutput() {
          @Override
          public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
            try {
              IOUtils.copy( in, outputStream );
            } finally {
              IOUtils.closeQuietly( in );
            }
          }
        };
      }
    } catch ( Exception ex ) {
      logger.fatal( ex );
    } finally {
      client.close();
    }

    return null;
  }

  @POST
  @Path( "/doQuery" )
  @Consumes( APPLICATION_FORM_URLENCODED )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput doQueryPost( MultivaluedMap<String, String> formParams,
      @Context HttpServletRequest servletRequest,
      @Context HttpServletResponse servletResponse ) throws WebApplicationException {

    //proxy request to the real CDA Endpoint
    String url = urlDoQuery + servletRequest.getQueryString();

    //Init
    Client client = getClientInitialized();

    //Invoke Rest endpoint with same params
    WebTarget webResource = client.target( url );

    try {
      Response response = webResource.request( MediaType.APPLICATION_FORM_URLENCODED )
        .post( Entity.form( formParams ), Response.class);

      if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
        InputStream in = response.readEntity( InputStream.class );

        return new StreamingOutput() {
          @Override
          public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
            try {
              IOUtils.copy( in, outputStream );
            } finally {
              IOUtils.closeQuietly( in );
            }
          }
        };
      }
    } catch ( Exception ex ) {
      logger.fatal( ex );
    } finally {
      client.close();
    }

    return null;
  }

  /**
   * For CDE discovery
   * Used by CPF PluginCall, when CDE Editor opens in Pentaho Server the first time
   */
  @GET
  @Path( "/listDataAccessTypes" )
  @Produces( APPLICATION_JSON )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String listDataAccessTypes( @DefaultValue( "false" ) @QueryParam( "refreshCache" ) Boolean refreshCache )
      throws Exception {

    //proxy request to the real CDA Endpoint
    String url = urlListDataAccessTypes;

    //Init
    Client client = getClientInitialized();

    WebTarget webResource = client.target( url );
    webResource = webResource.queryParam( "refreshCache", refreshCache.toString() );

    try {
      Response response = webResource.request( MediaType.APPLICATION_JSON )
        .get( Response.class );

      if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
        InputStream in = response.readEntity( InputStream.class );
        try {
          return IOUtils.toString( in );
        } finally {
          IOUtils.closeQuietly( in );
        }
      }
    } catch ( Exception ex ) {
      logger.fatal( ex );
    } finally {
      client.close();
    }

    return null;
  }
}
