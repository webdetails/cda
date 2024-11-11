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


package pt.webdetails.cda.connections;

import java.util.ArrayList;

import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

public class DummyConnection extends AbstractConnection {

  public DummyConnection() {
    super();
  }

  public DummyConnection( String id ) {
    super( id );
  }

  public DummyConnection( final Element connection ) throws InvalidConnectionException {
    super( connection );
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.NONE;
  }

  @Override
  protected void initializeConnection( Element connection ) throws InvalidConnectionException {
  }

  @Override
  public String getType() {
    return "dummy";
  }

  @Override
  public int hashCode() {
    return -1;
  }

  @Override
  public boolean equals( Object obj ) {
    return false;
  }

  public ArrayList<PropertyDescriptor> getProperties() {
    return new ArrayList<PropertyDescriptor>();
  }
}
