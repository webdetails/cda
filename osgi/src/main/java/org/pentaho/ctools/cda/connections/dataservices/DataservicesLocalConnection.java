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
