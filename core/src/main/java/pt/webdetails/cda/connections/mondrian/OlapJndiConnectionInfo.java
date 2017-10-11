/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
