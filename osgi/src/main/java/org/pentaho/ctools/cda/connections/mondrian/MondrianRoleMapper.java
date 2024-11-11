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
