/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

import org.dom4j.Element;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import pt.webdetails.cda.utils.FormulaEvaluator;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:54:53
 *
 * @author Thomas Morgner.
 */
public class JndiConnectionInfo
{
  private String jndi = "";
  private String user;
  private String pass;
  private String userField;
  private String passwordField;

  public JndiConnectionInfo(final Element connection) {

    jndi = ((String) connection.selectObject("string(./Jndi)"));

    final String userName = (String) connection.selectObject("string(./User)");
    final String password = (String) connection.selectObject("string(./Pass)");
    final String userFormula = (String) connection.selectObject("string(./UserField)");
    final String passFormula = (String) connection.selectObject("string(./PassField)");

    if (StringUtils.isEmpty(userName) == false)
    {
      setUser(userName);
    }
    if (StringUtils.isEmpty(password) == false)
    {
      setPass(password);
    }
    if (StringUtils.isEmpty(userFormula) == false)
    {
      setUserField(userFormula);
    }
    if (StringUtils.isEmpty(passFormula) == false)
    {
      setPasswordField(passFormula);
    }
  }
  
  public JndiConnectionInfo(String jndi, String userName, String password, String userFormula, String passFormula){
    this.jndi = jndi;
    if (!StringUtils.isEmpty(userName)) setUser(userName);
    if (!StringUtils.isEmpty(password)) setPass(password);
    if (!StringUtils.isEmpty(userFormula))  setUserField(userFormula);
    if (!StringUtils.isEmpty(passFormula)) setPasswordField(passFormula);
  }

  public String getUser()
  {
    return FormulaEvaluator.replaceFormula( user );
  }

  public void setUser(final String user)
  {
    this.user = user;
  }

  public String getPass()
  {
    return pass;
  }

  public void setPass(final String pass)
  {
    this.pass = pass;
  }

  public String getUserField()
  {
    return userField;
  }

  public void setUserField(final String userField)
  {
    this.userField = userField;
  }

  public String getPasswordField()
  {
    return passwordField;
  }

  public void setPasswordField(final String passwordField)
  {
    this.passwordField = passwordField;
  }

  public String getJndi()
  {
    return jndi == null ? "" : jndi;
  }
  
  public void setJndi(String jndi){
    this.jndi = jndi == null ? "" : jndi;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final JndiConnectionInfo that = (JndiConnectionInfo) o;

    if (!StringUtils.equals(jndi, that.jndi))
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return getJndi().hashCode();
  }
}
