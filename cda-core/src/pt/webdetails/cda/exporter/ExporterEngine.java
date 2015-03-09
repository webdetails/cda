/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.exporter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.utils.Util;


public class ExporterEngine {

  private static final Log logger = LogFactory.getLog( ExporterEngine.class );

  public enum OutputType {

    JSON( "json" ),
    XML( "xml" ),
    CSV( "csv" ),
    XLS( "xls" ),
    HTML( "html" ),
    BINARY( "binary" );

    private String type;

    OutputType( String type ) {
      this.type = type;
    }

    public String toString() {
      return type;
    }

    public static OutputType parse( String typeStr ) {
      for ( OutputType outputType : OutputType.values() ) {
        if ( StringUtils.equalsIgnoreCase( outputType.toString(), typeStr ) ) {
          return outputType;
        }
      }
      return null;
    }
  }

  public ExporterEngine() {

  }


  public TableExporter getExporter( final String outputType ) throws UnsupportedExporterException {
    return getExporter( outputType, null );
  }

  public TableExporter getExporter( final String outputType, final Map<String, String> extraSettings )
    throws UnsupportedExporterException {
    TableExporter exporter = getExporter( OutputType.parse( outputType ), extraSettings );
    if ( exporter != null ) {
      return exporter;
    }
    //else fallback to old version
    logger.error( MessageFormat.format( "getExporter for {0} failed, falling back to old version", outputType ) );
    //TODO: can this be deleted?
    try {

      final String className = "pt.webdetails.cda.exporter."
        + outputType.substring( 0, 1 ).toUpperCase() + outputType.substring( 1, outputType.length() ) + "Exporter";

      final Class<?> clazz = Class.forName( className );
      final Class<?>[] params =
        {
          HashMap.class
        };

      exporter = (TableExporter) clazz.getConstructor( params ).newInstance( new Object[]
        {
          extraSettings
        } );
      return exporter;

    } catch ( ClassNotFoundException e ) {
      throw new UnsupportedExporterException( "Could not find exporter for " + outputType, e );
    } catch ( Exception e ) {
      throw new UnsupportedExporterException( "Error initializing export class: " + Util.getExceptionDescription( e ),
        e );
    }

  }

  private TableExporter getExporter( OutputType type, Map<String, String> extraSettings ) {
    if ( type == null ) {
      return null;
    }

    switch( type ) {
      case CSV:
        return new CsvExporter( extraSettings );
      case HTML:
        return new HtmlExporter( extraSettings );
      case JSON:
        return new JsonExporter( extraSettings );
      case XLS:
        return new XlsExporter( extraSettings );
      case XML:
        return new XmlExporter( extraSettings );
      case BINARY:
        return new BinaryExporter( extraSettings );
      default:
        return null;
    }
  }

}
