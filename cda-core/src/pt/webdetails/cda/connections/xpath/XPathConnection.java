/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.xpath;

import java.util.ArrayList;
import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 13:49:12
 *
 * @author Thomas Morgner.
 */
public class XPathConnection extends AbstractConnection
{

  private XPathConnectionInfo connectionInfo;

  public XPathConnection(final Element connection)
          throws InvalidConnectionException
  {
    super(connection);
  }

  public XPathConnection()
  {
  }

  public ConnectionCatalog.ConnectionType getGenericType()
  {
    return ConnectionCatalog.ConnectionType.XPATH;
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new XPathConnectionInfo(connection);
  }

  public String getType()
  {
    return "xPath";
  }

  public String getXqueryDataFile()
  {
    if (connectionInfo == null)
    {
      throw new IllegalStateException();
    }
    return connectionInfo.getXqueryDataFile();
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

    final XPathConnection that = (XPathConnection) o;

    if (connectionInfo != null ? !connectionInfo.equals(that.connectionInfo) : that.connectionInfo != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties()
  {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("dataFile", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }

  public String getTypeForFile()
  {
    //return "xpath.XPath";
	  return "xpath.xPath";
  }

  public XPathConnectionInfo getConnectionInfo() {

	  return connectionInfo;
  }
}
