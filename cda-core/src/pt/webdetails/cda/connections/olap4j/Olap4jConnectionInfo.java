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

package pt.webdetails.cda.connections.olap4j;

import java.util.List;
import java.util.Properties;

import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;
/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:51:09
 *
 * @author Thomas Morgner.
 */
public class Olap4jConnectionInfo
{

	private String driver;
	private String url;
	private String user;
	private String pass;
	private String role;
	private Properties properties;
	private String roleField;
	private String userField;
	private String passwordField;



	public Olap4jConnectionInfo(
			String driver, 
			String url, 
			String user, 
			String password, 
			String role,
			Properties properties, 
			String roleField, 
			String userField, 
			String passField) 
	{
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pass = password;
		this.role = role;
		this.properties = properties;
		this.roleField = roleField;
		this.userField = userField;
		this.passwordField = passField;
	}

	public Olap4jConnectionInfo(final Element connection)
	{

		final String driver = (String) connection.selectObject("string(./Driver)");
		final String url = (String) connection.selectObject("string(./Url)");
		final String userName = (String) connection.selectObject("string(./User)");
		final String password = (String) connection.selectObject("string(./Pass)");
		final String role = (String) connection.selectObject("string(./Role)");
		final String roleFormula = (String) connection.selectObject("string(./RoleField)");
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

		this.url = url;
		this.driver = driver;

		if (StringUtils.isEmpty(userName) == false)
		{
			setUser(userName);
		}
		if (StringUtils.isEmpty(password) == false)
		{
			setPass(password);
		}
		if (StringUtils.isEmpty(role) == false)
		{
			setRole(role);
		}
		if (StringUtils.isEmpty(userFormula) == false)
		{
			setUserField(userFormula);
		}
		if (StringUtils.isEmpty(passFormula) == false)
		{
			setPasswordField(passFormula);
		}
		if (StringUtils.isEmpty(roleFormula) == false)
		{
			setRoleField(roleFormula);
		}

		properties = new Properties();
		@SuppressWarnings("unchecked")
		final List<Element> list = connection.elements("Property");
		for (final Element childElement : list)
		{
			final String name = childElement.attributeValue("name");
			final String text = childElement.getText();
			properties.put(name, text);
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

	public Properties getProperties()
	{
		return this.properties;
	}

	public void setProperties(Properties properties)
	{
		this.properties = properties;
	}

	/**
	 * @return the role
	 */
	 public String getRole() {
		return role;
	}

	 /**
	  * @param role the role to set
	  */
	 public void setRole(String role) {
		 this.role = role;
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

		 final Olap4jConnectionInfo that = (Olap4jConnectionInfo) o;

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
