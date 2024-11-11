/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
 */
public class DenormalizedOlap4JDataAccess extends Olap4JDataAccess {

  public DenormalizedOlap4JDataAccess() {
  }

  public DenormalizedOlap4JDataAccess( final Element element ) {
    super( element );
  }

  @Override
  protected AbstractNamedMDXDataFactory createDataFactory()
    throws UnknownConnectionException, InvalidConnectionException {
    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection( getConnectionId() );
    return new DenormalizedMDXDataFactory( connection.getInitializedConnectionProvider() );
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
