/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.NamedStaticDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:20:39
 *
 * @author Thomas Morgner.
 */
public class ReflectionDataAccess extends PREDataAccess
{

  public ReflectionDataAccess(final Element element)
  {
    super(element);
  }

  public ReflectionDataAccess()
  {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final NamedStaticDataFactory dataFactory = new NamedStaticDataFactory();
    dataFactory.setQuery("query", getQuery());
    return dataFactory;
  }

  public String getType()
  {
    return "reflection";
  }

  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.NONE;
  }
}
