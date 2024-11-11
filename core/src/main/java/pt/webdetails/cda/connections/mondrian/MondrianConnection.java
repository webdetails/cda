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


package pt.webdetails.cda.connections.mondrian;

import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;

public interface MondrianConnection extends Connection {

  public DataSourceProvider getInitializedDataSourceProvider() throws InvalidConnectionException;

  public MondrianConnectionInfo getConnectionInfo();

}
