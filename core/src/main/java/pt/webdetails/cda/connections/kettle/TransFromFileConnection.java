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

package pt.webdetails.cda.connections.kettle;

import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 14:02:09
 *
 * @author Thomas Morgner.
 */
public class TransFromFileConnection extends AbstractConnection implements KettleConnection
{

  private TransFromFileConnectionInfo connectionInfo;

  public TransFromFileConnection()
  {
  }


  public TransFromFileConnection(final Element connection)
          throws InvalidConnectionException
  {
    super(connection);
  }


  /**
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer(final String query, CdaSettings cdaSettings)
  {
	IDataAccessUtils dataAccessUtils = CdaEngine.getEnvironment().getDataAccessUtils();
	return dataAccessUtils.createKettleTransformationProducer(connectionInfo, query, cdaSettings);
  }


  public ConnectionCatalog.ConnectionType getGenericType()
  {
    return ConnectionCatalog.ConnectionType.KETTLE;
  }


  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new TransFromFileConnectionInfo(connection);
  }


  public String getType()
  {
    return "kettleTransFromFile";
  }


  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final TransFromFileConnection that = (TransFromFileConnection) o;

    if (connectionInfo != null ? !connectionInfo.equals(that.connectionInfo) : that.connectionInfo != null)
    {
      return false;
    }

    return true;
  }


  public int hashCode()
  {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }


  @Override
  public ArrayList<PropertyDescriptor> getProperties()
  {
    final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("ktrFile", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("variables", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    return properties;
  }


  @Override
  public String getTypeForFile()
  {
    return "kettle.TransFromFile";
  }

  public TransFromFileConnectionInfo getConnectionInfo() {
	  return connectionInfo;
  }
}
