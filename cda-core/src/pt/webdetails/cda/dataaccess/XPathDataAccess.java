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
