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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.BandedMDXDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.olap4j.Olap4JConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 */
public class Olap4JDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( Olap4JDataAccess.class );

  public Olap4JDataAccess( final Element element ) {
    super( element );
  }

  public Olap4JDataAccess() {
  }

  protected AbstractNamedMDXDataFactory createDataFactory()
    throws UnknownConnectionException, InvalidConnectionException {
    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection( getConnectionId() );
    return new BandedMDXDataFactory( connection.getInitializedConnectionProvider() );
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {

    logger.debug( "Creating BandedMDXDataFactory" );

    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection( getConnectionId() );

    final AbstractNamedMDXDataFactory mdxDataFactory = createDataFactory();
    // using deprecated method for 3.10 support
    mdxDataFactory.setQuery( "query", getQuery() );
    mdxDataFactory.setJdbcPasswordField( connection.getPasswordField() );
    mdxDataFactory.setJdbcUserField( connection.getUserField() );
    mdxDataFactory.setRoleField( connection.getRoleField() );
    return mdxDataFactory;
  }

  public String getType() {
    return "olap4j";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.OLAP4J;
  }
}
