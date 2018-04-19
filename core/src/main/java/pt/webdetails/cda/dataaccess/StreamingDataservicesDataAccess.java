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
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.parser.ParseException;
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
  private static final DataAccessEnums.DataAccessInstanceType TYPE = DataAccessEnums.DataAccessInstanceType.STREAMING_DATASERVICES;
  protected String dataServiceName;
  protected String windowMode;
  protected long windowSize;
  protected long windowEvery;
  protected long windowLimit;
  protected int componentRefreshPeriod = 10;

  public StreamingDataservicesDataAccess( final Element element ) {
    super( element );
    this.dataServiceName = element.selectSingleNode( "./StreamingDataServiceName" ).getText();
    this.windowMode = String.valueOf( element.selectSingleNode( "./WindowMode" ).getText() );
    this.windowSize = Integer.valueOf( element.selectSingleNode( "./WindowSize" ).getText() );
    this.windowEvery = Long.valueOf( element.selectSingleNode( "./WindowEvery" ).getText() );
    this.windowLimit = Long.valueOf( element.selectSingleNode( "./WindowLimit" ).getText() );
    this.componentRefreshPeriod = Integer.valueOf( element.selectSingleNode( "./ComponentRefreshPeriod" ).getText() );
  }

  public String getDataServiceName() {
    return dataServiceName;
  }

  public String getWindowMode() {
    return windowMode;
  }

  public long getWindowSize() {
    return windowSize;
  }

  public long getWindowEvery() {
    return windowEvery;
  }

  public long getWindowLimit() {
    return windowLimit;
  }

  public int getComponentRefreshPeriod() {
    return componentRefreshPeriod;
  }

  public StreamingDataservicesDataAccess() {
  }

  @Override
  public SQLReportDataFactory getSQLReportDataFactory( DataservicesConnection connection,
                                                       ParameterDataRow parameterDataRow )
    throws InvalidConnectionException, UnknownConnectionException {

    IDataServiceClientService.StreamingMode mode =
      IDataServiceClientService.StreamingMode.ROW_BASED.toString().equalsIgnoreCase( windowMode )
        ? IDataServiceClientService.StreamingMode.ROW_BASED
        : IDataServiceClientService.StreamingMode.TIME_BASED;

    try {
      return new SQLStreamingReportDataFactory(
        connection.getInitializedConnectionProvider( getParameterValues( connection, parameterDataRow ) ),
        mode, this.windowSize, this.windowEvery, this.windowLimit );
    } catch ( InvalidParameterException | QueryException | EvaluationException | ParseException e ) {
      throw new InvalidConnectionException( "Error when creating the connection from the parameters", e );
    }
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
    properties.add( new PropertyDescriptor( "dataServiceQuery", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "connection", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "streamingDataServiceName", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowMode", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowSize", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowEvery", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "windowLimit", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "componentRefreshPeriod", PropertyDescriptor.Type.STRING,
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
