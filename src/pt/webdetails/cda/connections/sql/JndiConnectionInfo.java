package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;

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

  public JndiConnectionInfo(final Element connection) {

    jndi = ((String) connection.selectObject("string(./Jndi)"));

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
