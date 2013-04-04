/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.connections;

import java.util.ArrayList;
import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 *
 * @author pdpi
 */
public class DummyConnection extends AbstractConnection {

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.NONE;
  }

  @Override
  protected void initializeConnection(Element connection) throws InvalidConnectionException {
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
  public boolean equals(Object obj) {
    return false;
  }

  public ArrayList<PropertyDescriptor> getProperties() {
    return new ArrayList<PropertyDescriptor>();
  }
}
