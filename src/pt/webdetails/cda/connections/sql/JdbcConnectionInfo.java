package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;

public class JdbcConnectionInfo {

  private String driver;
  private String url;
  private String user;
  private String pass;


  public JdbcConnectionInfo(final Element connection) {

    setDriver((String) connection.selectObject("string(./Driver)"));
    setUrl((String) connection.selectObject("string(./Url)"));
    setUser((String) connection.selectObject("string(./User)"));
    setPass((String) connection.selectObject("string(./Pass)"));

  }


  public String getDriver() {
    return driver;
  }

  public void setDriver(final String driver) {
    this.driver = driver;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(final String user) {
    this.user = user;
  }

  public String getPass() {
    return pass;
  }

  public void setPass(final String pass) {
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