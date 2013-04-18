/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.mondrian;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;


public class MondrianRoleMapper implements IMondrianRoleMapper {
  
private static final Log logger = LogFactory.getLog(MondrianRoleMapper.class);  
  
  
  public String getRoles(String catalog) {
	  if (PentahoSystem.getObjectFactory().objectDefined(MDXConnection.MDX_CONNECTION_MAPPER_KEY)) {
			  final IConnectionUserRoleMapper mondrianUserRoleMapper =
				  PentahoSystem.get(IConnectionUserRoleMapper.class, MDXConnection.MDX_CONNECTION_MAPPER_KEY, null);
           try{               
			  final String[] validMondrianRolesForUser =
				  mondrianUserRoleMapper.mapConnectionRoles(PentahoSessionHolder.getSession(), "solution:" + catalog.replaceAll("solution/",""));//XXX report the exception

			  if ((validMondrianRolesForUser != null) && (validMondrianRolesForUser.length > 0))
			  {
				  final StringBuffer buff = new StringBuffer();
				  for (int i = 0; i < validMondrianRolesForUser.length; i++)
				  {
					  final String aRole = validMondrianRolesForUser[i];
					  // According to http://mondrian.pentaho.org/documentation/configuration.php
					  // double-comma escapes a comma
					  if (i > 0)
					  {
						  buff.append(",");
					  }
					  buff.append(aRole.replaceAll(",", ",,"));
				  }
				  logger.debug("Assembled role: " + buff.toString() + " for catalog: " + catalog);
				  return buff.toString();
			  }
                          
          } catch(PentahoAccessControlException e){//In case of exception do...
              
          }
		  }    
    return "";
  }
}
