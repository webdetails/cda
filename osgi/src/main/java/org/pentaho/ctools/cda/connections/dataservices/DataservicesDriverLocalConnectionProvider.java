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

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.trans.dataservice.client.DataServiceConnectionInformation;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class DataservicesDriverLocalConnectionProvider extends DriverConnectionProvider {
  @Override
  public Connection createConnection( String user, String password ) throws SQLException {
    try {
      return getConnection( null );
    } catch ( KettleDatabaseException e ) {
      throw new SQLException( e );
    }
  }

  private Connection getConnection( String dataServiceName ) throws KettleDatabaseException {
    DataServiceConnectionInformation connectionInformation = new DataServiceConnectionInformation( dataServiceName, null, null );
    Database database = new Database( null, connectionInformation.getDatabaseMeta() );
    database.normalConnect( null );
    return database.getConnection();
  }
}
