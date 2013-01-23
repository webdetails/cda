/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class SqlDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog(SqlDataAccess.class);
  private static final String TYPE = "sql";

  public SqlDataAccess(final Element element) {
    super(element);
  }
  public SqlDataAccess() {
  }
  
  /**
   * 
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public SqlDataAccess(String id, String name, String connectionId, String query){
  	super(id,name, connectionId, query);
  }

  public String getType()
  {
    return TYPE;
  }


  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {

    logger.debug("Creating SQLReportDataFactory");

    final SqlConnection connection = (SqlConnection) getCdaSettings().getConnection(getConnectionId());
    final SQLReportDataFactory reportDataFactory = new SQLReportDataFactory(connection.getInitializedConnectionProvider());

    reportDataFactory.setUserField(connection.getUserField());
    reportDataFactory.setPasswordField(connection.getPasswordField());

    reportDataFactory.setQuery("query", getQuery(), null, null);

    return reportDataFactory;



  }

  @Override
  public ConnectionType getConnectionType() {return ConnectionType.SQL;}
}
