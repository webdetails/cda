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

package pt.webdetails.cda.connections.dataservices;

import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.jdbc.ThinConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class DataservicesDriverLocalConnectionProvider extends DriverConnectionProvider {

  private Supplier<List<IDataServiceClientService>> serviceSupplier;

  public DataservicesDriverLocalConnectionProvider() {
    this( DataservicesDriverLocalConnectionProvider.defaultSupplier() );
  }

  public DataservicesDriverLocalConnectionProvider( Supplier<List<IDataServiceClientService>> serviceSupplier ) {
    this.serviceSupplier = serviceSupplier;
  }

  private static Supplier<List<IDataServiceClientService>> defaultSupplier() {
    return () -> PentahoSystem.getAll( IDataServiceClientService.class );
  }

  @Override
  public Connection createConnection( String user, String password ) throws SQLException {
    if ( ThinConnection.localClient == null ) {
      List<IDataServiceClientService> listDataServiceClientServices = this.serviceSupplier.get();
      if ( listDataServiceClientServices != null ) {
        if ( listDataServiceClientServices.size() > 0 ) {
          // gets the first one... if later more than one implementation exists, we need to think on how to filter
          // the correct one
          ThinConnection.localClient = listDataServiceClientServices.get( 0 );
        }
      }
    }

    return super.createConnection( user, password );
  }
}
