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


package pt.webdetails.cda.connections.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.utils.Util;

public class JdbcConnection extends AbstractSqlConnection {

  private static final Log logger = LogFactory.getLog( JdbcConnection.class );
  public static final String TYPE = "sqlJdbc";
  private JdbcConnectionInfo connectionInfo;

  public JdbcConnection( final Element connection ) throws InvalidConnectionException {

    super( connection );

  }

  public JdbcConnection() {
  }

  @Override
  protected void initializeConnection( final Element connection ) throws InvalidConnectionException {

    connectionInfo = new JdbcConnectionInfo( connection );

  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException {

    logger.debug( "Creating new jdbc connection" );

    final DriverConnectionProvider connectionProvider = new DriverConnectionProvider();
    connectionProvider.setDriver( connectionInfo.getDriver() );
    connectionProvider.setUrl( connectionInfo.getUrl() );

    final Properties properties = connectionInfo.getProperties();
    final Enumeration<Object> keys = properties.keys();
    while ( keys.hasMoreElements() ) {
      final String key = (String) keys.nextElement();
      final String value = properties.getProperty( key );
      connectionProvider.setProperty( key, value );
    }

    logger.debug( "Opening connection" );
    try {
      final Connection connection =
        connectionProvider.createConnection( connectionInfo.getUser(), connectionInfo.getPass() );
      connection.close();
    } catch ( SQLException e ) {

      throw new InvalidConnectionException( "JdbcConnection: Found SQLException: " + Util.getExceptionDescription( e ),
        e );
    }

    logger.debug( "Connection opened" );

    return connectionProvider;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final JdbcConnection that = (JdbcConnection) o;

    if ( connectionInfo != null ? !connectionInfo.equals( that.connectionInfo ) : that.connectionInfo != null ) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
    ArrayList<PropertyDescriptor> properties = super.getProperties();
    properties.add(
      new PropertyDescriptor( "driver", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "url", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "user", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "pass", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  public String getPasswordField() {
    return connectionInfo.getPasswordField();
  }

  public String getUserField() {
    return connectionInfo.getUserField();
  }

  public JdbcConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }
}
