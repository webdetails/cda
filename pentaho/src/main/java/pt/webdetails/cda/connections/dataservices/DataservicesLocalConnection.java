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

package pt.webdetails.cda.connections.dataservices;

import org.pentaho.di.trans.dataservice.client.DataServiceClientPlugin;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cpf.PluginEnvironment;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class DataservicesLocalConnection implements IDataservicesLocalConnection {

  public DriverConnectionProvider getDriverConnectionProvider( Map<String, String> dataserviceParameters ) throws MalformedURLException {
    final DataservicesDriverLocalConnectionProvider connectionProvider = new DataservicesDriverLocalConnectionProvider();

    String driver = new DataServiceClientPlugin().getDriverClass();
    connectionProvider.setDriver( driver );

    URL contextRootUrl = new URL( PluginEnvironment.env().getUrlProvider().getWebappContextRoot() );
    String hostname = contextRootUrl.getHost();
    String port = String.valueOf( contextRootUrl.getPort() );
    String path = PluginEnvironment.env().getUrlProvider().getWebappContextPath().replace( "/", "" );
    String url = new DataServiceClientPlugin().getURL( hostname, port, path ) + "?local=true";

    //puts the properties in the connection URL (they must be prefixed with "PARAMETER_", or they are ignored)
    for ( String paramenterKey : dataserviceParameters.keySet() ) {
      if ( dataserviceParameters.get( paramenterKey ) != null ) {
        try {
          url += "&" + URLEncoder.encode( IDataServiceClientService.PARAMETER_PREFIX + paramenterKey, "UTF-8" ) + "="
            + URLEncoder.encode( dataserviceParameters.get( paramenterKey ), "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
          throw new RuntimeException( "Error encoding dataservice URL", e );
        }
      }
    }

    connectionProvider.setUrl( url );
    return connectionProvider;
  }
}
