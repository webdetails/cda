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

import java.util.List;

import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitor;

public abstract class AbstractConnection implements Connection {

  private String id;
  private CdaSettings cdaSettings;
  protected static ConnectionType connectionType;

  public AbstractConnection() {
  }

  public AbstractConnection( String id ) {
    this.id = id;
  }

  public AbstractConnection( final Element connection ) throws InvalidConnectionException {

    id = connection.attributeValue( "id" );

    initializeConnection( connection );

  }

  public abstract ConnectionType getGenericType();

  protected abstract void initializeConnection( Element connection ) throws InvalidConnectionException;

  public String getId() {
    return id;
  }

  public abstract String getType();

  public CdaSettings getCdaSettings() {
    return cdaSettings;
  }

  public void setCdaSettings( final CdaSettings cdaSettings ) {
    this.cdaSettings = cdaSettings;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals( final Object obj );

  public List<PropertyDescriptor> getProperties() {
    // Let implementors know they're missing something important
    throw new UnsupportedOperationException( "Not implemented yet!" );
  }

  public String getTypeForFile() {
    return this.getClass().toString().toLowerCase()
      .replaceAll( "class pt.webdetails.cda.connections.(.*)connection", "$1" );
  }

  @Override
  public void accept( DomVisitor v, Element ele ) {
    v.visit( (AbstractConnection) this, ele );
  }

}
