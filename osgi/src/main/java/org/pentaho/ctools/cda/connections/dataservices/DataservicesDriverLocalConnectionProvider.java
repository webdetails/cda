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

import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.jdbc.ThinConnection;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class DataservicesDriverLocalConnectionProvider extends DriverConnectionProvider {

  private List<IDataServiceClientService> dataServiceClientServices;

  public DataservicesDriverLocalConnectionProvider( List<IDataServiceClientService> dataServiceClientServices ) {
    if ( dataServiceClientServices == null ) {
      throw new IllegalArgumentException( "dataServiceClientServices can not be null" );
    }
    this.dataServiceClientServices = dataServiceClientServices;
  }

  @Override public Connection createConnection( String user, String password ) throws SQLException {
    if ( ThinConnection.localClient == null && !dataServiceClientServices.isEmpty() ) {
      // gets the first one... if later more than one implementation exists, we need to think on how to filter
      // the correct one
      ThinConnection.localClient = dataServiceClientServices.get( 0 );
    }

    return super.createConnection( user, password );
  }
}
