/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.connections.dataservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.FormulaParameter;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.WrappingFormulaContext;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.parser.ParseException;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.InvalidParameterException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.utils.Util;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class DataservicesConnection extends AbstractConnection {

  private static final Log logger = LogFactory.getLog( DataservicesConnection.class );
  public static final String TYPE = "dataservices";
  private DataservicesConnectionInfo connectionInfo;

  public DataservicesConnection( final Element connection ) throws InvalidConnectionException {
    super( connection );
  }

  public DataservicesConnection() {
  }

  @Override public ConnectionCatalog.ConnectionType getGenericType() {
    return ConnectionCatalog.ConnectionType.DATASERVICES;
  }

  @Override
  protected void initializeConnection( final Element connection ) throws InvalidConnectionException {
    connectionInfo = new DataservicesConnectionInfo( connection );
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public ConnectionProvider getInitializedConnectionProvider( Map<String, String> dataserviceParameters )
    throws InvalidConnectionException {

    logger.debug( "Creating new dataservices connection" );

    IDataservicesLocalConnection dataservicesLocalConnection =
        CdaEngine.getEnvironment().getDataServicesLocalConnection();

    try {
      final DriverConnectionProvider connectionProvider =
          dataservicesLocalConnection.getDriverConnectionProvider( dataserviceParameters );

      final Properties properties = connectionInfo.getProperties();
      final Enumeration<Object> keys = properties.keys();
      while ( keys.hasMoreElements() ) {
        final String key = (String) keys.nextElement();
        final String value = properties.getProperty( key );
        connectionProvider.setProperty( key, value );
      }

      logger.debug( "Opening connection" );
      final Connection connection = connectionProvider.createConnection( null, null );
      connection.close();
      logger.debug( "Connection opened" );
      return connectionProvider;
    } catch ( MalformedURLException e ) {
      throw new InvalidConnectionException(
          "DataservicesConnection: Found MalformedURLException: " + Util.getExceptionDescription( e ), e );
    } catch ( SQLException e ) {
      throw new InvalidConnectionException(
          "DataservicesConnection: Found SQLException: " + Util.getExceptionDescription( e ), e );
    }
  }

  public ConnectionProvider getInitializedConnectionProvider(
      ParameterDataRow parameterDataRow,
      FormulaContext formulaContext ) throws InvalidConnectionException {
    try {
      Map<String, String> paramValues = getParameterValues( parameterDataRow, formulaContext );
      return getInitializedConnectionProvider( paramValues );
    } catch ( InvalidParameterException | QueryException | EvaluationException | ParseException e ) {
      throw new InvalidConnectionException( "Error when creating the connection from the parameters", e );
    }
  }

  private Map<String, String> getParameterValues( ParameterDataRow parameterDataRow, FormulaContext formulaContext )
    throws InvalidParameterException, QueryException, EvaluationException, ParseException {
    FormulaContext wrappedContext =
        new WrappingFormulaContext( formulaContext, parameterDataRow );

    Map<String, String> parametersValues = new TreeMap<>();

    FormulaParameter[] definedVariableNames =
        FormulaParameter.convert( getConnectionInfo().getDefinedVariableNames() );
    for ( int i = 0; i < definedVariableNames.length; ++i ) {
      FormulaParameter mapping = definedVariableNames[i];
      String sourceName = mapping.getName();
      Object value = mapping.compute( wrappedContext );
      if ( value != null ) {
        parametersValues.put( sourceName, String.valueOf( value ) );
      } else {
        parametersValues.put( sourceName, (String) null );
      }
    }
    return parametersValues;
  }

  @Override
  public List<PropertyDescriptor> getProperties() {
    final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(
      new PropertyDescriptor( "variables", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  public String getTypeForFile() {
    return "dataservices.dataservices";
  }

  public DataservicesConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final DataservicesConnection that = (DataservicesConnection) o;

    if ( connectionInfo != null ? !connectionInfo.equals( that.connectionInfo ) : that.connectionInfo != null ) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }
}
