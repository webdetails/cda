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

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;

import java.net.MalformedURLException;
import java.util.Map;

public interface IDataservicesLocalConnection {

  DriverConnectionProvider getDriverConnectionProvider( Map<String, String> dataserviceParameters ) throws MalformedURLException;
}
