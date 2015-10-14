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

package pt.webdetails.cda.connections.sql;

import java.util.List;
import java.util.Properties;

import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;
public class JdbcConnectionInfo
{

  private String driver;
  private String url;
  private String user;
  private String pass;
  private String userField;
  private String passwordField;
  private Properties properties;

  public JdbcConnectionInfo(final Element connection)
  {


    final String driver = (String) connection.selectObject("string(./Driver)");
    final String url = (String) connection.selectObject("string(./Url)");
    final String userName = (String) connection.selectObject("string(./User)");
    final String password = (String) connection.selectObject("string(./Pass)");
    final String userFormula = (String) connection.selectObject("string(./UserField)");
    final String passFormula = (String) connection.selectObject("string(./PassField)");

    if (StringUtils.isEmpty(driver))
    {
      throw new IllegalStateException("A driver is mandatory");
    }
    if (StringUtils.isEmpty(url))
    {
      throw new IllegalStateException("A url is mandatory");
    }

    setDriver(driver);
    setUrl(url);

    // For user / pass, we also need to set them im the properties
    properties = new Properties();

    if (StringUtils.isEmpty(userName) == false)
    {
      setUser(userName);
      properties.setProperty("user", userName);

    }
    if (StringUtils.isEmpty(password) == false)
    {
      setPass(password);
      properties.setProperty("password", password);
    }
    if (StringUtils.isEmpty(userFormula) == false)
    {
      setUserField(userFormula);
    }
    if (StringUtils.isEmpty(passFormula) == false)
    {
      setPasswordField(passFormula);
    }

    final List<?> list = connection.elements("Property");
    for (int i = 0; i < list.size(); i++)
    {
      final Element childElement = (Element) list.get(i);
      final String name = childElement.attributeValue("name");
      final String text = childElement.getText();
      properties.put(name, text);
    }
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

  public Properties getProperties()
  {
    return properties;
  }

  public String getDriver()
  {
    return driver;
  }

  public void setDriver(final String driver)
  {
    this.driver = driver;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(final String url)
  {
    this.url = url;
  }

  public String getUser()
  {
    return user;
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

    final JdbcConnectionInfo that = (JdbcConnectionInfo) o;

    if (driver != null ? !driver.equals(that.driver) : that.driver != null)
    {
      return false;
    }
    if (pass != null ? !pass.equals(that.pass) : that.pass != null)
    {
      return false;
    }
    if (url != null ? !url.equals(that.url) : that.url != null)
    {
      return false;
    }
    if (user != null ? !user.equals(that.user) : that.user != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = driver != null ? driver.hashCode() : 0;
    result = 31 * result + (url != null ? url.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (pass != null ? pass.hashCode() : 0);
    return result;
  }
}
