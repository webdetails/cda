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

package pt.webdetails.cda.connections.sql;

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;

public interface SqlConnection extends Connection {

  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException;

  public String getUserField();

  public String getPasswordField();

}
