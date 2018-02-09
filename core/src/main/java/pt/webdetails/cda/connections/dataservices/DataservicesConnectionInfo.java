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

package pt.webdetails.cda.connections.dataservices;

import org.dom4j.Element;

import java.util.List;
import java.util.Properties;

public class DataservicesConnectionInfo {

  private Properties properties;

  private String dataServiceName;

  public DataservicesConnectionInfo( final Element connection ) {
    properties = new Properties();

    final String dataServiceName = (String) connection.selectObject( "string(./DataServiceName)" );
    this.dataServiceName = dataServiceName;
    properties.setProperty( "dataServiceName", dataServiceName );

    final List<?> list = connection.elements( "Property" );
    for ( int i = 0; i < list.size(); i++ ) {
      final Element childElement = (Element) list.get( i );
      final String name = childElement.attributeValue( "name" );
      final String text = childElement.getText();
      properties.put( name, text );
    }
  }

  public String getDataServiceName() {
    return dataServiceName;
  }

  public Properties getProperties() {
    return properties;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof DataservicesConnectionInfo ) ) {
      return false;
    }

    DataservicesConnectionInfo that = (DataservicesConnectionInfo) o;

    return getDataServiceName().equals( that.getDataServiceName() );
  }

  @Override public int hashCode() {
    return getDataServiceName().hashCode();
  }
}
