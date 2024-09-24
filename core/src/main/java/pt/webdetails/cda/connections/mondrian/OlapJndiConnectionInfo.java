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

package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;


public class OlapJndiConnectionInfo extends pt.webdetails.cda.connections.JndiConnectionInfo {

  private String roleField;

  public OlapJndiConnectionInfo( final String roleFiled, String jndi ) {
    super( jndi, null, null, null, null );

  }

  public OlapJndiConnectionInfo( final Element connection ) {

    super( connection );

    final String roleFormula = (String) connection.selectObject( "string(./RoleField)" );

    if ( StringUtils.isEmpty( roleFormula ) == false ) {
      setRoleField( roleFormula );
    }

  }

  public String getRoleField() {
    return roleField;
  }

  public void setRoleField( final String roleField ) {
    this.roleField = roleField;
  }
}
