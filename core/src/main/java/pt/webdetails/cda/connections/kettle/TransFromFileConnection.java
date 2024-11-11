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


package pt.webdetails.cda.connections.kettle;

import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Todo: Document me!
 */
public class TransFromFileConnection extends AbstractConnection implements KettleConnection {

  private TransFromFileConnectionInfo connectionInfo;

  public TransFromFileConnection() {
  }


  public TransFromFileConnection( final Element connection ) throws InvalidConnectionException {
    super( connection );
  }


  /**
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer( final String query, CdaSettings cdaSettings ) {
    IDataAccessUtils dataAccessUtils = CdaEngine.getEnvironment().getDataAccessUtils();
    return dataAccessUtils.createKettleTransformationProducer( connectionInfo, query, cdaSettings );
  }


  public ConnectionCatalog.ConnectionType getGenericType() {
    return ConnectionCatalog.ConnectionType.KETTLE;
  }

  protected void initializeConnection( final Element connection ) throws InvalidConnectionException {
    connectionInfo = new TransFromFileConnectionInfo( connection );
  }


  public String getType() {
    return "kettleTransFromFile";
  }


  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final TransFromFileConnection that = (TransFromFileConnection) o;

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
    final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(
      new PropertyDescriptor( "ktrFile", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "variables", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }


  @Override
  public String getTypeForFile() {
    return "kettle.TransFromFile";
  }

  public TransFromFileConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }
}
