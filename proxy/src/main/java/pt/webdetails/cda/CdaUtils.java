/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import pt.webdetails.cpf.utils.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;


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
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
    Client client = Client.create( clientConfig );
    client.addFilter( new HTTPBasicAuthFilter( USER, PASS ) );
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
    WebResource webResource = client.resource( url );

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
      ClientResponse response = webResource.type( MediaType.APPLICATION_FORM_URLENCODED )
        .get( ClientResponse.class );

      if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ) {
        InputStream in = response.getEntityInputStream();

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
      client.destroy();
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
    WebResource webResource = client.resource( url );

    try {
      ClientResponse response = webResource.type( MediaType.APPLICATION_FORM_URLENCODED )
        .post( ClientResponse.class, formParams );

      if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ) {
        InputStream in = response.getEntityInputStream();

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
      client.destroy();
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

    WebResource webResource = client.resource( url );
    webResource = webResource.queryParam( "refreshCache", refreshCache.toString() );

    try {
      ClientResponse response = webResource.type( MediaType.APPLICATION_JSON )
        .get( ClientResponse.class );

      if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ) {
        InputStream in = response.getEntityInputStream();
        try {
          return IOUtils.toString( in );
        } finally {
          IOUtils.closeQuietly( in );
        }
      }
    } catch ( Exception ex ) {
      logger.fatal( ex );
    } finally {
      client.destroy();
    }

    return null;
  }
}
