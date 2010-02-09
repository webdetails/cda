package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;

public class JdbcConnectionInfo implements MondrianConnectionInfo
{

  private String driver;
  private String url;
  private String user;
  private String pass;
  private String catalog;
  private String cube;



  public JdbcConnectionInfo(final Element connection) {

    setDriver((String) connection.selectObject("string(./Driver)"));
    setUrl((String) connection.selectObject("string(./Url)"));
    setUser((String) connection.selectObject("string(./User)"));
    setPass((String) connection.selectObject("string(./Pass)"));
    setCatalog((String) connection.selectObject("string(./Catalog)"));
    setCube((String) connection.selectObject("string(./Cube)"));

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
    if (!(o instanceof JdbcConnectionInfo))
    {
      return false;
    }

    final JdbcConnectionInfo that = (JdbcConnectionInfo) o;

    if (catalog != null ? !catalog.equals(that.catalog) : that.catalog != null)
    {
      return false;
    }
    if (cube != null ? !cube.equals(that.cube) : that.cube != null)
    {
      return false;
    }
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
    result = 31 * result + (catalog != null ? catalog.hashCode() : 0);
    result = 31 * result + (cube != null ? cube.hashCode() : 0);
    return result;
  }
}