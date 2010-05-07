package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;
import org.pentaho.reporting.libraries.base.util.StringUtils;

public class JndiConnectionInfo implements MondrianConnectionInfo
{

  private String jndi;
  private String catalog;
  private String cube;
  private String roleField;
  private String userField;
  private String passwordField;
  private String mondrianRole;
  private String user;
  private String pass;



  public JndiConnectionInfo(final Element connection) {

    setJndi((String) connection.selectObject("string(./Jndi)"));

    setCatalog((String) connection.selectObject("string(./Catalog)"));
    setCube((String) connection.selectObject("string(./Cube)"));

    final String userName = (String) connection.selectObject("string(./User)");
    final String password = (String) connection.selectObject("string(./Pass)");
    final String role = (String) connection.selectObject("string(./Role)");

    final String roleFormula = (String) connection.selectObject("string(./RoleField)");
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

    if (StringUtils.isEmpty(role) == false)
    {
      setMondrianRole(role);
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

  public String getMondrianRole()
  {
    return mondrianRole;
  }

  public void setMondrianRole(final String mondrianRole)
  {
    this.mondrianRole = mondrianRole;
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

  public String getJndi()
  {
    return jndi;
  }

  public void setJndi(final String jndi)
  {
    this.jndi = jndi;
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
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final JndiConnectionInfo that = (JndiConnectionInfo) o;

    if (catalog != null ? !catalog.equals(that.catalog) : that.catalog != null)
    {
      return false;
    }
    if (cube != null ? !cube.equals(that.cube) : that.cube != null)
    {
      return false;
    }
    if (jndi != null ? !jndi.equals(that.jndi) : that.jndi != null)
    {
      return false;
    }
    if (mondrianRole != null ? !mondrianRole.equals(that.mondrianRole) : that.mondrianRole != null)
    {
      return false;
    }
    if (pass != null ? !pass.equals(that.pass) : that.pass != null)
    {
      return false;
    }
    if (passwordField != null ? !passwordField.equals(that.passwordField) : that.passwordField != null)
    {
      return false;
    }
    if (roleField != null ? !roleField.equals(that.roleField) : that.roleField != null)
    {
      return false;
    }
    if (user != null ? !user.equals(that.user) : that.user != null)
    {
      return false;
    }
    if (userField != null ? !userField.equals(that.userField) : that.userField != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = jndi != null ? jndi.hashCode() : 0;
    result = 31 * result + (catalog != null ? catalog.hashCode() : 0);
    result = 31 * result + (cube != null ? cube.hashCode() : 0);
    result = 31 * result + (roleField != null ? roleField.hashCode() : 0);
    result = 31 * result + (userField != null ? userField.hashCode() : 0);
    result = 31 * result + (passwordField != null ? passwordField.hashCode() : 0);
    result = 31 * result + (mondrianRole != null ? mondrianRole.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (pass != null ? pass.hashCode() : 0);
    return result;
  }
}
