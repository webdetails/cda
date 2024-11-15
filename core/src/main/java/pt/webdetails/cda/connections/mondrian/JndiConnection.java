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


package pt.webdetails.cda.connections.mondrian;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.EvaluableConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.utils.FormulaEvaluator;

public class JndiConnection extends AbstractMondrianConnection implements EvaluableConnection {

  private static final Log logger = LogFactory.getLog( JndiConnection.class );
  public static final String TYPE = "mondrianJndi";
  private MondrianJndiConnectionInfo connectionInfo;
  private Element connection;

  public JndiConnection( final Element connection ) throws InvalidConnectionException {
    super( connection );
    this.connection = connection;
  }

  public JndiConnection() {
  }

  public JndiConnection( String id, MondrianJndiConnectionInfo info ) {
    super( id );
    connectionInfo = info;
  }

  @Override
  protected void initializeConnection( final Element connection ) throws InvalidConnectionException {

    connectionInfo = new MondrianJndiConnectionInfo( connection );

  }

  @Override
  public String getType() {
    return TYPE;
  }

  public DataSourceProvider getInitializedDataSourceProvider() throws InvalidConnectionException {
    logger.debug( "Creating new jndi connection" );
    IDataAccessUtils idu = CdaEngine.getEnvironment().getDataAccessUtils();
    return idu.getMondrianJndiDatasourceProvider( connectionInfo );

  }

  public synchronized MondrianJndiConnectionInfo getConnectionInfo() {
    return this.connectionInfo;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final JndiConnection that = (JndiConnection) o;

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
    final ArrayList<PropertyDescriptor> properties = super.getProperties();
    properties
      .add( new PropertyDescriptor( "jndi", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  @Override
  public Connection evaluate() {
    JndiConnection evaluated = this;
    try {
      evaluated = new JndiConnection( this.connection );
    } catch ( InvalidConnectionException e ) {
      logger.error( "Unable to duplicate connection for evaluation", e );
    }
    evaluated.setCdaSettings( getCdaSettings() );
    // process formula on jndi
    evaluated.getConnectionInfo().setJndi( FormulaEvaluator.replaceFormula( getConnectionInfo().getJndi() ) );
    // assemble role
    evaluated.getConnectionInfo().setMondrianRole( assembleRole( getConnectionInfo().getCatalog() ) );
    return evaluated;
  }
}
