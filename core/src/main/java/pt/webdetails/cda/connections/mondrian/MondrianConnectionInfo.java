/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.connections.mondrian;

public interface MondrianConnectionInfo {

  public String getCatalog();

  public String getUser();

  public String getPass();

  public String getMondrianRole();

  public String getRoleField();

  public String getUserField();

  public String getPasswordField();

}
