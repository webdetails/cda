/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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
