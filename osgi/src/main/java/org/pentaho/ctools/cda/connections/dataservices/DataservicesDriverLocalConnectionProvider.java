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

import org.pentaho.di.trans.dataservice.jdbc.ThinDriver;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

public class DataservicesDriverLocalConnectionProvider extends DriverConnectionProvider {

  // Initializing the ThinDriver here in order to trigger its own static initialization block
  // and also make it available in the DriverConnectionProvider's ClassLoader when we are creating
  // a new connection. This will bypass DriverManager.getConnection(...) that would throw an exception
  // before trying to connect to the ThinDriver, making it impossible to connect to it.
  static {
    new ThinDriver();
  }

}
