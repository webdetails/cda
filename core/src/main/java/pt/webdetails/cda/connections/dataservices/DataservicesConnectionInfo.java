/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
