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

package pt.webdetails.cda.connections.dataservices;

import org.pentaho.di.trans.dataservice.client.DataServiceClientPlugin;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cpf.PluginEnvironment;

import java.net.MalformedURLException;
import java.net.URL;

public class DataservicesLocalConnection implements IDataservicesLocalConnection {

  public DriverConnectionProvider getDriverConnectionProvider() throws MalformedURLException {
    final DataservicesDriverLocalConnectionProvider connectionProvider = new DataservicesDriverLocalConnectionProvider();

    String driver = new DataServiceClientPlugin().getDriverClass();
    connectionProvider.setDriver( driver );

    URL contextRootUrl = new URL( PluginEnvironment.env().getUrlProvider().getWebappContextRoot() );
    String hostname = contextRootUrl.getHost();
    String port = String.valueOf( contextRootUrl.getPort() );
    String path = PluginEnvironment.env().getUrlProvider().getWebappContextPath().replace( "/", "" );
    String url = new DataServiceClientPlugin().getURL( hostname, port, path ) + "?local=true";
    connectionProvider.setUrl( url );
    return connectionProvider;
  }
}
