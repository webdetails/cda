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

package pt.webdetails.cda.connections.sql;

import java.util.ArrayList;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

public abstract class AbstractSqlConnection extends AbstractConnection implements SqlConnection {

  public AbstractSqlConnection( final Element connection ) throws InvalidConnectionException {

    super( connection );

  }

  public AbstractSqlConnection() {
  }

  /**
   * @param id this connection's ID
   */
  public AbstractSqlConnection( String id ) {
    super( id );
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.SQL;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(
      new PropertyDescriptor( "id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB ) );
    return properties;
  }
}
