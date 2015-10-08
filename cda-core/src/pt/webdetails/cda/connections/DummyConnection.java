/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
