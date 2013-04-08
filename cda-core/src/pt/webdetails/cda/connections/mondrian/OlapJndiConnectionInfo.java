/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:59:29
 *
 * @author Thomas Morgner.
 */
public class OlapJndiConnectionInfo extends pt.webdetails.cda.connections.JndiConnectionInfo
{
  private String roleField;

  public OlapJndiConnectionInfo(final String roleFiled, String jndi) {
    super(jndi, null, null, null, null);
  
  }
  public OlapJndiConnectionInfo(final Element connection) {
    
    super(connection);

    final String roleFormula = (String) connection.selectObject("string(./RoleField)");
    
    if (StringUtils.isEmpty(roleFormula) == false)
    {
      setRoleField(roleFormula);
    }

  }

  public String getRoleField()
  {
    return roleField;
  }

  public void setRoleField(final String roleField)
  {
    this.roleField = roleField;
  }
}
