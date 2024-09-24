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
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.xml.DomVisitor;

import java.util.List;

/**
 * Implementation of a DataAccess that will get data from a Pentaho Data Service
 */
public class DataservicesDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( DataservicesDataAccess.class );
  private static final DataAccessEnums.DataAccessInstanceType TYPE = DataAccessEnums.DataAccessInstanceType.DATASERVICES;
  protected String dataServiceName;

  public DataservicesDataAccess( final Element element ) {
    super( element );
    this.query = element.selectSingleNode( "./DataServiceQuery" ).getText();
    this.dataServiceName = parseNode( element, "./DataServiceName", s -> s, null );
  }

  public DataservicesDataAccess() {
  }

  @Override
  public DataFactory getDataFactory( final ParameterDataRow parameterDataRow )
      throws UnknownConnectionException, InvalidConnectionException {
    logger.debug( "Creating DataServicesDataFactory" );

    final DataservicesConnection connection =
      (DataservicesConnection) getCdaSettings().getConnection( getConnectionId() );
    final SQLReportDataFactory reportDataFactory = getSQLReportDataFactory( connection, parameterDataRow );

    reportDataFactory.setQuery( "query", getQuery() );

    return reportDataFactory;
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    return getDataFactory( null );
  }

  public String getDataServiceName() {
    return dataServiceName;
  }

  public void setDataServiceQuery( String dataServiceQuery ) {
    this.query = dataServiceQuery;
  }

  public SQLReportDataFactory getSQLReportDataFactory(
      DataservicesConnection connection,
      ParameterDataRow parameterDataRow ) throws InvalidConnectionException, UnknownConnectionException {
    return new SQLReportDataFactory(
      connection.getInitializedConnectionProvider( parameterDataRow, CdaEngine.getEnvironment().getFormulaContext() ) );
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
    properties.add( new PropertyDescriptor( "dataServiceQuery", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.removeIf( propertyDescriptor -> propertyDescriptor.getName().equalsIgnoreCase( "query" ) );
    return properties;
  }

  public void accept( DomVisitor xmlVisitor, Element root ) {
    xmlVisitor.visit( (DataservicesDataAccess) this, root );
  }
}
