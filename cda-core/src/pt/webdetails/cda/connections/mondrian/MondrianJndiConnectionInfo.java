/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;

public class MondrianJndiConnectionInfo extends pt.webdetails.cda.connections.mondrian.OlapJndiConnectionInfo implements MondrianConnectionInfo 
{
  private String catalog;
  private String cube;
  private String mondrianRole;


  public MondrianJndiConnectionInfo(String jndi, String catalog, String cube) {
   super(null, jndi);
  setCatalog(catalog);
  setCube(cube);
  }
  
  public MondrianJndiConnectionInfo(final Element connection)
  {
    super(connection);

    setCatalog((String) connection.selectObject("string(./Catalog)"));
    setCube((String) connection.selectObject("string(./Cube)"));

    final String role = (String) connection.selectObject("string(./Role)");

    if (StringUtils.isEmpty(role) == false)
    {
      setMondrianRole(role);
    }
  }


  public String getMondrianRole()
  {
    return mondrianRole;
  }

  public void setMondrianRole(final String mondrianRole)
  {
    this.mondrianRole = mondrianRole;
  }

  public String getCatalog()
  {
    return catalog;
  }


  public void setCatalog(final String catalog)
  {
    this.catalog = catalog;
  }


  public String getCube()
  {
    return cube;
  }


  public void setCube(final String cube)
  {
    this.cube = cube;
  }


  public boolean equals(final Object o)
  {
    if(!super.equals(o)) return false;

    final MondrianJndiConnectionInfo that = (MondrianJndiConnectionInfo) o;

    if (catalog != null ? !catalog.equals(that.catalog) : that.catalog != null)
    {
      return false;
    }
    if (cube != null ? !cube.equals(that.cube) : that.cube != null)
    {
      return false;
    }
    if (mondrianRole != null ? !mondrianRole.equals(that.mondrianRole) : that.mondrianRole != null)
    {
      return false;
    }
    if (getPass() != null ? !getPass().equals(that.getPass()) : that.getPass() != null)
    {
      return false;
    }
    if (getPasswordField() != null ? !getPasswordField().equals(that.getPasswordField()) : that.getPasswordField() != null)
    {
      return false;
    }
    if (getRoleField() != null ? !getRoleField().equals(that.getRoleField()) : that.getRoleField() != null)
    {
      return false;
    }
    if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null)
    {
      return false;
    }
    if (getUserField() != null ? !getUserField().equals(that.getUserField()) : that.getUserField() != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = super.hashCode();//jndi != null ? jndi.hashCode() : 0;
    result = 31 * result + (catalog != null ? catalog.hashCode() : 0);
    result = 31 * result + (cube != null ? cube.hashCode() : 0);
    result = 31 * result + (getRoleField() != null ? getRoleField().hashCode() : 0);
    result = 31 * result + (getUserField() != null ? getUserField().hashCode() : 0);
    result = 31 * result + (getPasswordField() != null ? getPasswordField().hashCode() : 0);
    result = 31 * result + (mondrianRole != null ? mondrianRole.hashCode() : 0);
    result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
    result = 31 * result + (getPass() != null ? getPass().hashCode() : 0);
    return result;
  }
}
