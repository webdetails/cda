/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.DenormalizedMDXDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.olap4j.Olap4JConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:07:29
 *
 * @author Thomas Morgner.
 */
public class DenormalizedOlap4JDataAccess extends Olap4JDataAccess {

  public DenormalizedOlap4JDataAccess() {
  }

  public DenormalizedOlap4JDataAccess(final Element element) {
    super(element);
  }

  @Override
  protected AbstractNamedMDXDataFactory createDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection(getConnectionId());
    return new DenormalizedMDXDataFactory(connection.getInitializedConnectionProvider());
  }

  @Override
  public String getType() {
    return "denormalizedOlap4j";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.OLAP4J;
  }
}
