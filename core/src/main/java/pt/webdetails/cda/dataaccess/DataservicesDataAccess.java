/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
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
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

import java.util.List;

/**
 * Implementation of a DataAccess that will get data from a Pentaho Data Service
 */
public class DataservicesDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( DataservicesDataAccess.class );
  private static final DataAccessEnums.DataAccessInstanceType TYPE = DataAccessEnums.DataAccessInstanceType.DATASERVICES;

  public DataservicesDataAccess( final Element element ) {
    super( element );
  }

  public DataservicesDataAccess() {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    logger.debug( "Creating DataServicesDataFactory" );

    final DataservicesConnection connection = (DataservicesConnection) getCdaSettings().getConnection( getConnectionId() );
    final SQLReportDataFactory reportDataFactory = getSQLReportDataFactory( connection );

    // using deprecated version for 3.9/3.10 support until it breaks with latest
    reportDataFactory.setQuery( "query", getQuery() );
    // reportDataFactory.setQuery("query", getQuery(), null, null);

    return reportDataFactory;
  }

  public SQLReportDataFactory getSQLReportDataFactory( DataservicesConnection connection )
          throws InvalidConnectionException, UnknownConnectionException {
    return new SQLReportDataFactory( connection.getInitializedConnectionProvider() );
  }

  public String getType() {
    return TYPE.getType();
  }

  public String getLabel() {
    return TYPE.getLabel();
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.DATASERVICES;
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    List<PropertyDescriptor> properties = super.getInterface();
    properties.add(
        new PropertyDescriptor( "dataServiceName", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }
}
