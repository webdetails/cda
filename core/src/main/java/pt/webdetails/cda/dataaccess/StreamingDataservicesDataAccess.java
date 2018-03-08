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
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.streaming.SQLStreamingReportDataFactory;
import pt.webdetails.cda.xml.DomVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a StreamingDataAccess that will get data from a Pentaho Data Service
 */
public class StreamingDataservicesDataAccess extends DataservicesDataAccess {

  private static final Log logger = LogFactory.getLog( StreamingDataservicesDataAccess.class );
  private static final DataAccessEnums.DataAccessInstanceType TYPE =
    DataAccessEnums.DataAccessInstanceType.STREAMING_DATASERVICES;
  protected String dataServiceName;
  protected int windowRowSize;
  protected long windowRate;
  protected long windowMillisSize;

  public StreamingDataservicesDataAccess( final Element element ) {
    super( element );
    this.dataServiceName = element.selectSingleNode( "./StreamingDataServiceName" ).getText();
    this.windowRowSize = Integer.valueOf( element.selectSingleNode( "./WindowRowSize" ).getText() );
    this.windowRate = Long.valueOf( element.selectSingleNode( "./WindowRate" ).getText() );
    this.windowMillisSize = Long.valueOf( element.selectSingleNode( "./WindowMillisSize" ).getText() );
  }

  public String getDataServiceName() {
    return dataServiceName;
  }

  public int getWindowRowSize() {
    return windowRowSize;
  }

  public long getWindowRate() {
    return windowRate;
  }

  public long getWindowMillisSize() {
    return windowMillisSize;
  }

  public StreamingDataservicesDataAccess() {
  }

  @Override
  public SQLReportDataFactory getSQLReportDataFactory( DataservicesConnection connection )
          throws InvalidConnectionException, UnknownConnectionException {
    return new SQLStreamingReportDataFactory( connection.getInitializedConnectionProvider(),
            this.windowRowSize, this.windowMillisSize, this.windowRate );
  }

  public String getType() {
    return TYPE.getType();
  }

  public String getLabel() {
    return TYPE.getLabel();
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
    properties.add( new PropertyDescriptor( "windowRowSize", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowRate", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowMillisSize", PropertyDescriptor.Type.STRING,
            PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  public void accept( DomVisitor xmlVisitor, Element root ) {
    xmlVisitor.visit( (StreamingDataservicesDataAccess) this, root );
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.DATASERVICES;
  }
}
