/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.xml;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * XML utils, including formatting.
 */
public class XmlUtils {

  private static Log logger = LogFactory.getLog( XmlUtils.class );

  private static XmlFormatter formatter = new XmlFormatter( 2, 80 );

  public static String formatXml( String s ) {
    return formatter.format( s, 0 );
  }

  public static String formatXml( String s, int initialIndent ) {
    return formatter.format( s, initialIndent );
  }

  private static class XmlFormatter {
    private int indentNumChars;
    private boolean singleLine;

    public XmlFormatter( int indentNumChars, int lineLength ) {
      this.indentNumChars = indentNumChars;
    }

    private static String buildWhitespace( int numChars ) {
      StringBuilder sb = new StringBuilder();
      for ( int i = 0; i < numChars; i++ ) {
        sb.append( " " );
      }
      return sb.toString();
    }

    public synchronized String format( String s, int initialIndent ) {
      int indent = initialIndent;
      StringBuilder sb = new StringBuilder();
      for ( int i = 0; i < s.length(); i++ ) {
        char currentChar = s.charAt( i );
        if ( currentChar == '<' ) {
          char nextChar = s.charAt( i + 1 );
          if ( nextChar == '/' ) {
            indent -= indentNumChars;
          }
          if ( !singleLine ) { // Don't indent before closing element if we're creating opening and closing elements
            // on a
            // single line.
            sb.append( buildWhitespace( indent ) );
          }
          if ( nextChar != '?' && nextChar != '!' && nextChar != '/' ) {
            indent += indentNumChars;
          }
          singleLine = false; // Reset flag.
        }
        sb.append( currentChar );
        if ( currentChar == '>' ) {
          if ( s.charAt( i - 1 ) == '/' ) {
            indent -= indentNumChars;
            sb.append( "\n" );
          } else {
            int nextStartElementPos = s.indexOf( '<', i );
            if ( nextStartElementPos > i + 1 ) {
              String textBetweenElements = s.substring( i + 1, nextStartElementPos );

              // If the space between elements is solely newlines, let them through to preserve additional newlines in
              // source document.
              if ( textBetweenElements.replaceAll( "\n", "" ).length() == 0 ) {
                sb.append( textBetweenElements + "\n" );
              }
              sb.append( textBetweenElements );
              i = nextStartElementPos - 1;
            } else {
              sb.append( "\n" );
            }
          }
        }
      }
      return sb.toString();
    }
  }



  public static String prettyPrint( final String xml ) {
    StringWriter sw = null;

    try {
      final OutputFormat format = new OutputFormat( "  ", true );
      final org.dom4j.Document document = DocumentHelper.parseText( xml );
      sw = new StringWriter();
      final XMLWriter writer = new XMLWriter( sw, format );
      writer.write( document );
    } catch ( Exception e ) {
      logger.warn( "creating beautified xml failed.", e );
    }
    return sw != null ? sw.toString() : null;
  }

}
