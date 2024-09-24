/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;

import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.CdaEngine;

/**
 * Todo: Document me!
 */
public class MqlDataAccess extends PREDataAccess {
  public MqlDataAccess( final Element element ) {
    super( element );
  }

  public MqlDataAccess() {
  }

  /**
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public MqlDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name, connectionId, query );
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final MetadataConnection connection = (MetadataConnection) getCdaSettings().getConnection( getConnectionId() );

    final PmdDataFactory returnDataFactory = new PmdDataFactory();
    returnDataFactory.setXmiFile( connection.getConnectionInfo().getXmiFile() );
    returnDataFactory.setDomainId( connection.getConnectionInfo().getDomainId() );
    IDataAccessUtils dataAccessUtils = CdaEngine.getEnvironment().getDataAccessUtils();
    dataAccessUtils.setConnectionProvider( returnDataFactory );

    // using deprecated method for 3.10 support
    returnDataFactory.setQuery( "query", getQuery() );

    return returnDataFactory;
  }

  @Override
  public String getType() {
    return "mql";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.MQL;
  }
}
