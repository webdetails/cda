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


package pt.webdetails.cda.connections.olap4j;

import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.OlapConnectionProvider;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;


public interface Olap4JConnection extends Connection {
  public OlapConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException;

  public String getUrl();

  public String getDriver();

  public String getUser();

  public String getPass();

  public String getRole();

  public String getRoleField();

  public String getUserField();

  public String getPasswordField();
}
