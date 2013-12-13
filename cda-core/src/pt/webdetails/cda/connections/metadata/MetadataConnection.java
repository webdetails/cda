/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.connections.metadata;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:10:27
 *
 * @author Thomas Morgner.
 */
public class MetadataConnection extends AbstractConnection {

  protected static final ConnectionType connectionType = ConnectionType.MQL;
  private MetadataConnectionInfo connectionInfo;

  public MetadataConnection(final Element connection)
          throws InvalidConnectionException {
    super(connection);
  }

  public MetadataConnection() {
  }
  
/**
 * 
 * @param id this connection's ID
 * @param domainId domain ID
 * @param xmiFile XMI file with metadata definition
 */
  public MetadataConnection(String id, String domainId, String xmiFile){
  	super(id);
  	this.connectionInfo = new MetadataConnectionInfo(domainId,xmiFile);
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException {
    connectionInfo = new MetadataConnectionInfo(connection);
  }

  public String getType() {
    return "metadata";
  }

  /**
   * @deprecated use {@link #getConnectionInfo()}
   */
  public MetadataConnectionInfo getMetadataConnectionInfo() {
    return connectionInfo;
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MetadataConnection that = (MetadataConnection) o;

    if (!connectionInfo.equals(that.connectionInfo)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo.hashCode();
  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.MQL;
  }

  @Override
  public List<PropertyDescriptor> getProperties() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("xmiFile", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("domainId", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));

    return properties;
  }
  
  public MetadataConnectionInfo getConnectionInfo() {
	  return connectionInfo;
  }
	
}
