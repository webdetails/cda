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

package pt.webdetails.cda.connections.dataservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.utils.Util;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

  public ConnectionProvider getInitializedConnectionProvider( Map<String, String> dataserviceParameters ) throws InvalidConnectionException {

    logger.debug( "Creating new dataservices connection" );

    IDataservicesLocalConnection dataservicesLocalConnection = CdaEngine.getEnvironment().getDataServicesLocalConnection();

    try {
      final DriverConnectionProvider connectionProvider = dataservicesLocalConnection.getDriverConnectionProvider( dataserviceParameters );

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
      throw new InvalidConnectionException( "DataservicesConnection: Found MalformedURLException: " + Util.getExceptionDescription( e ), e );
    } catch ( SQLException e ) {
      throw new InvalidConnectionException( "DataservicesConnection: Found SQLException: " + Util.getExceptionDescription( e ), e );
    }
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
