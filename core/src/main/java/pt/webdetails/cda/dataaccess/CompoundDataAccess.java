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

import java.util.ArrayList;

import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor.Type;
import pt.webdetails.cda.xml.DomVisitable;
import pt.webdetails.cda.xml.DomVisitor;

public abstract class CompoundDataAccess extends AbstractDataAccess implements DomVisitable {

  public CompoundDataAccess( final Element element ) {
    super( element );
  }

  public CompoundDataAccess() {
  }

  public void closeDataSource() throws QueryException {
    // not needed
  }

  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }

  @Override
  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add( new PropertyDescriptor( "id", Type.STRING, PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "parameters", Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "columns", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  public void accept( DomVisitor xmlVisitor, Element root ) {
    xmlVisitor.visit( (CompoundDataAccess) this, root );
  }
}
