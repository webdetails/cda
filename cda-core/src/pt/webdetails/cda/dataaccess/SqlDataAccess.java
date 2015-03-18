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
