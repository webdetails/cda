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

package org.pentaho.ctools.cda.connections.dataservices;

import org.pentaho.di.trans.dataservice.client.DataServiceClientPlugin;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService.PARAMETER_PREFIX;

public class DataservicesLocalConnection implements IDataservicesLocalConnection {
  private final DriverConnectionProvider connectionProvider;

  public DataservicesLocalConnection( DriverConnectionProvider connectionProvider ) {
    this.connectionProvider = connectionProvider;
  }

  public DriverConnectionProvider getDriverConnectionProvider( Map<String, String> dataserviceParameters ) {
    final DriverConnectionProvider connectionProvider = this.connectionProvider;

    DataServiceClientPlugin client = new DataServiceClientPlugin();

    String driver = client.getDriverClass();
    connectionProvider.setDriver( driver );

    // NOTE: hostname, port and path don't matter in a local connection ?
    StringBuilder url = new StringBuilder( client.getURL( "0.0.0.0", "12345", "" ) );
    url.append( "?local=true" );

    //puts the properties in the connection URL (they must be prefixed with "PARAMETER_", or they are ignored)
    for ( String parameterKey : dataserviceParameters.keySet() ) {
      if ( dataserviceParameters.get( parameterKey ) != null ) {
        try {
          final String name = URLEncoder.encode( PARAMETER_PREFIX + parameterKey, "UTF-8" );
          final String value = URLEncoder.encode( dataserviceParameters.get( parameterKey ), "UTF-8" );

          url.append( "&" ).append( name ).append( "=" ).append( value );
        } catch ( UnsupportedEncodingException e ) {
          throw new RuntimeException( "Error encoding dataservice URL", e );
        }
      }
    }

    connectionProvider.setUrl( url.toString() );
    return connectionProvider;
  }
}
