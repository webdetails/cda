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
