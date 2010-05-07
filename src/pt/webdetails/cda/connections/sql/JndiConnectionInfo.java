package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;
import org.pentaho.reporting.libraries.base.util.StringUtils;

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
  private String jndi;
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
    return jndi;
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

    if (!jndi.equals(that.jndi))
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return jndi.hashCode();
  }
}
