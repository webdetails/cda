/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a StreamingDataAccess that will get data from a Pentaho Data Service
 */
public class StreamingDataservicesDataAccess extends DataservicesDataAccess {

  private static final Log logger = LogFactory.getLog( StreamingDataservicesDataAccess.class );
  private static final String TYPE = "streaming";

  public StreamingDataservicesDataAccess( final Element element ) {
    super( element );
  }

  public StreamingDataservicesDataAccess() {
  }

  public String getType() {
    return TYPE;
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add( new PropertyDescriptor( "id", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "access", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "parameters", PropertyDescriptor.Type.ARRAY,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "output", PropertyDescriptor.Type.ARRAY,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "columns", PropertyDescriptor.Type.ARRAY,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "query", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "connection", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "streamingDataServiceName", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.DATASERVICES;
  }
}
