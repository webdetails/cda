/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.xpath.XPathDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.xpath.XPathConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:20:39
 *
 * @author Thomas Morgner.
 */
public class XPathDataAccess extends PREDataAccess
{

  public XPathDataAccess(final Element element)
  {
    super(element);
  }

  public XPathDataAccess()
  {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final XPathConnection connection = (XPathConnection) getCdaSettings().getConnection(getConnectionId());

    final XPathDataFactory dataFactory = new XPathDataFactory();
    dataFactory.setXqueryDataFile(connection.getXqueryDataFile());

    dataFactory.setQuery("query", getQuery());
    return dataFactory;
  }

  public String getType()
  {
	  return "xPath";
    //return "XPath";
  }

  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.XPATH;
  }
}
