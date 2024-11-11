/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.connections;

import java.util.List;

import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitable;

/**
 * Holds the Connections Settings of a file
 */
public interface Connection extends DomVisitable {

  public String getId();

  public String getType();

  public ConnectionType getGenericType();

  public CdaSettings getCdaSettings();

  public void setCdaSettings( CdaSettings cdaSettings );

  public List<PropertyDescriptor> getProperties();

  public String getTypeForFile();
}
