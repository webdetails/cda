/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.connections.mondrian;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;

public class MondrianRoleMapper implements IMondrianRoleMapper {

  private static final Log logger = LogFactory.getLog( MondrianRoleMapper.class );

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected IConnectionUserRoleMapper getConnectionUserRoleMapper() {
    return PentahoSystem.get(IConnectionUserRoleMapper.class, MDXConnection.MDX_CONNECTION_MAPPER_KEY, null);
  }

  protected boolean isObjectDefined() {
    return PentahoSystem.getObjectFactory().objectDefined( MDXConnection.MDX_CONNECTION_MAPPER_KEY );
  }

  public String getRoles( String catalog ) {
    if ( isObjectDefined() ) {
      final IConnectionUserRoleMapper mondrianUserRoleMapper = getConnectionUserRoleMapper();

      try {
        final String[] validMondrianRolesForUser;
        //XXX report the exception
        validMondrianRolesForUser = mondrianUserRoleMapper.mapConnectionRoles(getSession(), catalog );

        if ( ( validMondrianRolesForUser != null ) && ( validMondrianRolesForUser.length > 0 ) ) {
          final StringBuffer buff = new StringBuffer();

          for ( int i = 0; i < validMondrianRolesForUser.length; i++ ) {
            final String aRole = validMondrianRolesForUser[i];
            // According to http://mondrian.pentaho.org/documentation/configuration.php
            // double-comma escapes a comma
            if ( i > 0 ) {
              buff.append( "," );
            }
            buff.append(aRole.replaceAll( ",", ",," ) );
          }
          logger.debug( "Assembled role: " + buff.toString() + " for catalog: " + catalog );
          return buff.toString();
        }

      } catch ( PentahoAccessControlException e ) { //In case of exception do...

      }
    }

    return "";
  }


}
