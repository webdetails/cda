/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.mondrian;

import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 9, 2010
 * Time: 12:13:28 PM
 */
public interface MondrianConnection extends Connection
{


  public DataSourceProvider getInitializedDataSourceProvider() throws InvalidConnectionException;


  public MondrianConnectionInfo getConnectionInfo();

}