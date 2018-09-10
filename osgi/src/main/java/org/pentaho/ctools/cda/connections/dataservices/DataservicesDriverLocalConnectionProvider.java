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
