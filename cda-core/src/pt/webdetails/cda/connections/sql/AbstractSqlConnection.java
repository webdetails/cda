/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.sql;

import java.util.ArrayList;
import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractSqlConnection extends AbstractConnection implements SqlConnection {

  public AbstractSqlConnection(final Element connection) throws InvalidConnectionException {

    super(connection);

  }

  public AbstractSqlConnection() {
  }
  
  /**
   * 
   * @param id this connection's ID
   */
  public AbstractSqlConnection(String id){
  	super(id);
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.SQL;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    return properties;
  }
}
