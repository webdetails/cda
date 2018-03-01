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

  public DataservicesConnectionInfo( final Element connection ) {
    properties = new Properties();

    final List<?> list = connection.elements( "Property" );
    for ( int i = 0; i < list.size(); i++ ) {
      final Element childElement = (Element) list.get( i );
      final String name = childElement.attributeValue( "name" );
      final String text = childElement.getText();
      properties.put( name, text );
    }
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

    for ( Object key : getProperties().keySet() ) {
      Object thatName = that.getProperties().get( key );
      if ( thatName == null || !thatName.equals( getProperties().get( key ) ) ) {
        return false;
      }
    }
    return true;
  }

  @Override public int hashCode() {
    return getProperties().hashCode();
  }
}
