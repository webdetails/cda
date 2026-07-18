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


package org.pentaho.ctools.cda.connections.mondrian;

import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;

/**
 * Dummy class for support
 */
public class MondrianRoleMapper implements IMondrianRoleMapper {

  @Override
  public String getRoles( String catalog ) {
    return "";
  }
}
