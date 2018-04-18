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
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.FormulaParameter;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.WrappingFormulaContext;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.parser.ParseException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.xml.DomVisitor;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    this.dataServiceName = element.selectSingleNode( "./DataServiceName" ) != null ? element.selectSingleNode( "./DataServiceName" ).getText() : null;
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

    // using deprecated version for 3.9/3.10 support until it breaks with latest
    reportDataFactory.setQuery( "query", getQuery() );
    // reportDataFactory.setQuery("query", getQuery(), null, null);

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

  public SQLReportDataFactory getSQLReportDataFactory( DataservicesConnection connection,
                                                       ParameterDataRow parameterDataRow )
    throws InvalidConnectionException, UnknownConnectionException {
    try {
      return new SQLReportDataFactory(
        connection.getInitializedConnectionProvider( getParameterValues( connection, parameterDataRow ) ) );
    } catch ( InvalidParameterException | QueryException | EvaluationException | ParseException e ) {
      throw new InvalidConnectionException( "Error when creating the connection from the parameters", e );
    }
  }

  protected Map<String, String> getParameterValues( DataservicesConnection connection,
                                                    ParameterDataRow parameterDataRow )
    throws InvalidParameterException, QueryException, EvaluationException, ParseException {
    FormulaContext formulaContext =
      new WrappingFormulaContext( CdaEngine.getEnvironment().getFormulaContext(), parameterDataRow );

    Map<String, String> parametersValues = new TreeMap<>();

    FormulaParameter[] definedVariableNames =
      FormulaParameter.convert( connection.getConnectionInfo().getDefinedVariableNames() );
    for ( int i = 0; i < definedVariableNames.length; ++i ) {
      FormulaParameter mapping = definedVariableNames[ i ];
      String sourceName = mapping.getName();
      Object value = mapping.compute( formulaContext );
      if ( value != null ) {
        parametersValues.put( sourceName, String.valueOf( value ) );
      } else {
        parametersValues.put( sourceName, (String) null );
      }
    }
    return parametersValues;
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
