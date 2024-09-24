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
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.sql.SqlConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 */
public class SqlDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( SqlDataAccess.class );
  private static final String TYPE = "sql";

  public SqlDataAccess( final Element element ) {
    super( element );
  }

  public SqlDataAccess() {
  }

  /**
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public SqlDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name, connectionId, query );
  }

  public String getType() {
    return TYPE;
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {

    logger.debug( "Creating SQLReportDataFactory" );

    final SqlConnection connection = (SqlConnection) getCdaSettings().getConnection( getConnectionId() );
    final SQLReportDataFactory reportDataFactory =
      new SQLReportDataFactory( connection.getInitializedConnectionProvider() );

    reportDataFactory.setUserField( connection.getUserField() );
    reportDataFactory.setPasswordField( connection.getPasswordField() );
    // using deprecated version for 3.9/3.10 support until it breaks with latest 
    reportDataFactory.setQuery( "query", getQuery() );
    // reportDataFactory.setQuery("query", getQuery(), null, null);

    return reportDataFactory;


  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.SQL;
  }
}
