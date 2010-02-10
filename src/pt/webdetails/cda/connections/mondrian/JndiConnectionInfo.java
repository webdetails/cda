package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;

public class JndiConnectionInfo implements MondrianConnectionInfo
{

  private String jndi;
  private String catalog;
  private String cube;



  public JndiConnectionInfo(final Element connection) {

    setJndi((String) connection.selectObject("string(./Jndi)"));

    setCatalog((String) connection.selectObject("string(./Catalog)"));
    setCube((String) connection.selectObject("string(./Cube)"));

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
    if (!(o instanceof JndiConnectionInfo))
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

    return true;
  }

  public int hashCode()
  {
    int result = jndi != null ? jndi.hashCode() : 0;
    result = 31 * result + (catalog != null ? catalog.hashCode() : 0);
    result = 31 * result + (cube != null ? cube.hashCode() : 0);
    return result;
  }
}