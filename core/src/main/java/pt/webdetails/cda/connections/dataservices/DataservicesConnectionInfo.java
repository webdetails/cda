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
import org.pentaho.reporting.engine.classic.core.ParameterMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DataservicesConnectionInfo {

  private Properties properties;
  private ParameterMapping[] definedVariableNames;

  public DataservicesConnectionInfo( final Element connection ) {
    properties = new Properties();

    final List<?> list = connection.elements( "Property" );
    for ( int i = 0; i < list.size(); i++ ) {
      final Element childElement = (Element) list.get( i );
      final String name = childElement.attributeValue( "name" );
      final String text = childElement.getText();
      properties.put( name, text );
    }

    final List<Element> varsList = connection.elements( "variables" );
    final ParameterMapping[] vars = new ParameterMapping[ varsList.size() ];
    for ( int i = 0; i < varsList.size(); i++ ) {
      final Element element = varsList.get( i );
      final String dataRowName = element.attributeValue( "datarow-name" );
      final String variableName = element.attributeValue( "variable-name" );
      if ( variableName == null ) {
        vars[ i ] = new ParameterMapping( dataRowName, dataRowName );
      } else {
        vars[ i ] = new ParameterMapping( dataRowName, variableName );
      }
    }
    definedVariableNames = vars;
  }

  public Properties getProperties() {
    return properties;
  }

  public ParameterMapping[] getDefinedVariableNames() {
    return definedVariableNames;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof DataservicesConnectionInfo ) ) {
      return false;
    }

    DataservicesConnectionInfo that = (DataservicesConnectionInfo) o;

    if ( !Arrays.deepEquals( parameterMappingToStringArray( definedVariableNames ), parameterMappingToStringArray( that.definedVariableNames ) ) ) {
      return false;
    }

    return getProperties().equals( that.getProperties() );
  }

  @Override public int hashCode() {
    int result = getProperties().hashCode();
    result = 31 * result
      + ( definedVariableNames != null
      ? Arrays.deepHashCode( parameterMappingToStringArray( definedVariableNames ) )
      : 0 );
    return result;
  }

  private String[][] parameterMappingToStringArray( ParameterMapping[] paramMaps ) {
    if ( paramMaps == null ) {
      return null;
    }
    String[][] result = new String[ paramMaps.length ][];
    for ( int i = 0; i < paramMaps.length; i++ ) {
      String[] item = new String[] { paramMaps[ i ].getName(), paramMaps[ i ].getAlias() };
      result[ i ] = item;
    }
    return result;
  }
}
