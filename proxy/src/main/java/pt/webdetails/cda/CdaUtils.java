/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
  private String url_do_query = "http://" + CDA_HOST + ":"
      + CDA_PORT + "/cxf/cda/cda/api/utils/doQuery";

  public CdaUtils() {
    String serverFlag = System.getProperty( "cda.to_server" );
    if ( serverFlag != null && serverFlag.equals( "1" ) ) {
      url_do_query = "http://" + CDA_HOST + ":" + CDA_PORT + "/pentaho/plugin/cda/api/doQuery";
    }
  }

  @POST
  @Path( "/doQuery" )
  @Consumes( APPLICATION_FORM_URLENCODED )
  @Produces( { MimeTypes.JSON, MimeTypes.XML, MimeTypes.CSV, MimeTypes.XLS, MimeTypes.PLAIN_TEXT, MimeTypes.HTML } )
  public StreamingOutput doQueryPost( MultivaluedMap<String, String> formParams,
      @Context HttpServletRequest servletRequest,
      @Context HttpServletResponse servletResponse ) throws WebApplicationException {

    //proxy request to the real CDA Endpoint

    String url = url_do_query + servletRequest.getQueryString();

    //Init
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
    Client client = Client.create( clientConfig );
    client.addFilter( new HTTPBasicAuthFilter( USER, PASS ) );

    //Invoke Rest endpoint with same params, converting POST into a GET request
    WebResource webResource = client.resource( url );

    // add parameters
    Iterator<String> it = formParams.keySet().iterator();
    String key;
    while ( it.hasNext() ) {
      key = it.next();
      webResource = webResource.queryParam( key, formParams.get( key ).get( 0 ) );
    }

    try {
      ClientResponse response = webResource.type( MediaType.APPLICATION_OCTET_STREAM_TYPE )
        .get( ClientResponse.class );

      if ( response.getStatus() == 200 ) {
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

    String dataSourceDefinitions = "{\n"
        + "\"denormalizedMdx_mondrianJdbc\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"denormalizedMdx over mondrianJdbc\",\n"
        + "\t\t\"conntype\": \"mondrian.jdbc\",\n"
        + "\t\t\"datype\": \"denormalizedMdx\",\n"
        + "\t\t\"group\": \"MDX\",\n"
        + "\t\t\"groupdesc\": \"MDX Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"catalog\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"driver\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"url\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"user\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"pass\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"denormalizedMdx_mondrianJndi\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"denormalizedMdx over mondrianJndi\",\n"
        + "\t\t\"conntype\": \"mondrian.jndi\",\n"
        + "\t\t\"datype\": \"denormalizedMdx\",\n"
        + "\t\t\"group\": \"MDX\",\n"
        + "\t\t\"groupdesc\": \"MDX Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"catalog\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"jndi\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"denormalizedOlap4j_olap4j\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"denormalizedOlap4j over olap4j\",\n"
        + "\t\t\"conntype\": \"olap4j.defaultolap4j\",\n"
        + "\t\t\"datype\": \"denormalizedOlap4j\",\n"
        + "\t\t\"group\": \"OLAP4J\",\n"
        + "\t\t\"groupdesc\": \"OLAP4J Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"driver\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"url\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"role\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"property\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"join\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"join\",\n"
        + "\t\t\"datype\": \"join\",\n"
        + "\t\t\"group\": \"NONE\",\n"
        + "\t\t\"groupdesc\": \"Compound Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"left\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"right\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"joinType\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"kettle_kettleTransFromFile\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"kettle over kettleTransFromFile\",\n"
        + "\t\t\"conntype\": \"kettle.TransFromFile\",\n"
        + "\t\t\"datype\": \"kettle\",\n"
        + "\t\t\"group\": \"KETTLE\",\n"
        + "\t\t\"groupdesc\": \"KETTLE Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"ktrFile\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"variables\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"mdx_mondrianJdbc\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"mdx over mondrianJdbc\",\n"
        + "\t\t\"conntype\": \"mondrian.jdbc\",\n"
        + "\t\t\"datype\": \"mdx\",\n"
        + "\t\t\"group\": \"MDX\",\n"
        + "\t\t\"groupdesc\": \"MDX Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"catalog\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"driver\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"url\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"user\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"pass\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"bandedMode\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"mdx_mondrianJndi\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"mdx over mondrianJndi\",\n"
        + "\t\t\"conntype\": \"mondrian.jndi\",\n"
        + "\t\t\"datype\": \"mdx\",\n"
        + "\t\t\"group\": \"MDX\",\n"
        + "\t\t\"groupdesc\": \"MDX Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"catalog\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"jndi\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"bandedMode\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"mql_metadata\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"mql over metadata\",\n"
        + "\t\t\"conntype\": \"metadata.metadata\",\n"
        + "\t\t\"datype\": \"mql\",\n"
        + "\t\t\"group\": \"MQL\",\n"
        + "\t\t\"groupdesc\": \"MQL Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"xmiFile\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"domainId\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"olap4j_olap4j\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"olap4j over olap4j\",\n"
        + "\t\t\"conntype\": \"olap4j.defaultolap4j\",\n"
        + "\t\t\"datype\": \"olap4j\",\n"
        + "\t\t\"group\": \"OLAP4J\",\n"
        + "\t\t\"groupdesc\": \"OLAP4J Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"driver\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"url\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"role\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"property\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"scriptable_scripting\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"scriptable over scripting\",\n"
        + "\t\t\"conntype\": \"scripting.scripting\",\n"
        + "\t\t\"datype\": \"scriptable\",\n"
        + "\t\t\"group\": \"SCRIPTING\",\n"
        + "\t\t\"groupdesc\": \"SCRIPTING Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"language\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"initscript\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"jsonScriptable_scripting\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"jsonScriptable over scripting\",\n"
        + "\t\t\"conntype\": \"scripting.scripting\",\n"
        + "\t\t\"datype\": \"jsonScriptable\",\n"
        + "\t\t\"group\": \"SCRIPTING\",\n"
        + "\t\t\"groupdesc\": \"SCRIPTING Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"language\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"initscript\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"sql_sqlJdbc\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"sql over sqlJdbc\",\n"
        + "\t\t\"conntype\": \"sql.jdbc\",\n"
        + "\t\t\"datype\": \"sql\",\n"
        + "\t\t\"group\": \"SQL\",\n"
        + "\t\t\"groupdesc\": \"SQL Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"driver\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"url\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"user\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"pass\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"sql_sqlJndi\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"sql over sqlJndi\",\n"
        + "\t\t\"conntype\": \"sql.jndi\",\n"
        + "\t\t\"datype\": \"sql\",\n"
        + "\t\t\"group\": \"SQL\",\n"
        + "\t\t\"groupdesc\": \"SQL Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"jndi\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"union\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"union\",\n"
        + "\t\t\"datype\": \"union\",\n"
        + "\t\t\"group\": \"NONE\",\n"
        + "\t\t\"groupdesc\": \"Compound Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"top\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"bottom\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"xPath_xPath\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"xPath over xPath\",\n"
        + "\t\t\"conntype\": \"xpath.xPath\",\n"
        + "\t\t\"datype\": \"xPath\",\n"
        + "\t\t\"group\": \"XPATH\",\n"
        + "\t\t\"groupdesc\": \"XPATH Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"dataFile\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"dataservices_dataservices\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"sql over dataservices\",\n"
        + "\t\t\"conntype\": \"dataservices.dataservices\",\n"
        + "\t\t\"datype\": \"dataservices\",\n"
        + "\t\t\"group\": \"DATASERVICES\",\n"
        + "\t\t\"groupdesc\": \"DATASERVICES Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"variables\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"dataServiceName\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"dataServiceQuery\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "},\n"
        + "\"streaming_dataservices\": {\n"
        + "\t\"metadata\": {\n"
        + "\t\t\"name\": \"streaming over dataservices\",\n"
        + "\t\t\"conntype\": \"dataservices.dataservices\",\n"
        + "\t\t\"datype\": \"streaming\",\n"
        + "\t\t\"group\": \"DATASERVICES\",\n"
        + "\t\t\"groupdesc\": \"DATASERVICES Queries\"\n"
        + "\t},\n"
        + "\t\"definition\": {\n"
        + "\t\t\"connection\": {\n"
        + "\t\t\t\"variables\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}\n"
        + "\t\t},\n"
        + "\t\t\"dataaccess\": {\n"
        + "\t\t\t\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"dataServiceQuery\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},\n"
        + "\t\t\t\"streamingDataServiceName\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"windowMode\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"windowSize\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"windowEvery\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"windowLimit\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},\n"
        + "\t\t\t\"componentRefreshPeriod\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}\n"
        + "\t\t}\n"
        + "\t}\n"
        + "}\n"
        + "}";

    return dataSourceDefinitions;
  }
}
