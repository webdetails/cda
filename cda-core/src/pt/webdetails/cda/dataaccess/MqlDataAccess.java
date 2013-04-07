/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
 * <p/>
 * Date: 16.02.2010
 * Time: 13:17:20
 *
 * @author Thomas Morgner.
 */
public class MqlDataAccess extends PREDataAccess {
  public MqlDataAccess(final Element element) {
    super(element);
  }

  public MqlDataAccess() {
  }
  
  /**
   * 
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public MqlDataAccess(String id, String name, String connectionId, String query){
  	super(id,name, connectionId, query);
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final MetadataConnection connection = (MetadataConnection) getCdaSettings().getConnection(getConnectionId());

    final PmdDataFactory returnDataFactory = new PmdDataFactory();
    returnDataFactory.setXmiFile(connection.getConnectionInfo().getXmiFile());
    returnDataFactory.setDomainId(connection.getConnectionInfo().getDomainId());
    IDataAccessUtils dataAccessUtils = (IDataAccessUtils)CdaEngine.getInstance().getBeanFactory().getBean("IDataAccessUtils");
    dataAccessUtils.setConnectionProvider(returnDataFactory);
    // using deprecated method for 3.10 support
    returnDataFactory.setQuery("query", getQuery());

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
