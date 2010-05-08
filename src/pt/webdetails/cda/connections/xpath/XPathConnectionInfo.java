package pt.webdetails.cda.connections.xpath;

import org.dom4j.Element;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 13:49:22
 *
 * @author Thomas Morgner.
 */
public class XPathConnectionInfo
{
  private String xqueryDataFile;
  
  public XPathConnectionInfo(final Element connection)
  {
    xqueryDataFile = ((String) connection.selectObject("string(./DataFile)"));
  }

  public String getXqueryDataFile()
  {
    return xqueryDataFile;
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

    final XPathConnectionInfo that = (XPathConnectionInfo) o;

    if (xqueryDataFile != null ? !xqueryDataFile.equals(that.xqueryDataFile) : that.xqueryDataFile != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return xqueryDataFile != null ? xqueryDataFile.hashCode() : 0;
  }
}
