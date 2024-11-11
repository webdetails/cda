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


package pt.webdetails.cda.utils;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

public class Util extends pt.webdetails.cpf.Util {

  public static String getExceptionDescription( final Exception e ) {
    final StringBuilder out = new StringBuilder();
    out.append( "[ " ).append( e.getClass().getName() ).append( " ] - " );
    out.append( e.getMessage() );

    if ( e.getCause() != null ) {
      out.append( " .( Cause [ " ).append( e.getCause().getClass().getName() ).append( " ] " );
      out.append( e.getCause().getMessage() );

      if ( e.getCause().getCause() != null ) {
        out.append( " .( Parent [ " ).append( e.getCause().getCause().getClass().getName() ).append( " ] " );
        out.append( e.getCause().getCause().getMessage() );
      }
    }

    return out.toString();
  }


  /**
   * Extracts a string between after the first occurrence of begin, and before the last occurence of end
   *
   * @param source From where to extract
   * @param begin
   * @param end
   * @return
   */
  public static String getContentsBetween( final String source, final String begin, final String end ) {
    if ( source == null ) {
      return null;
    }

    int startIdx = source.indexOf( begin ) + begin.length();
    int endIdx = source.lastIndexOf( end );
    if ( startIdx < 0 || endIdx < 0 ) {
      return null;
    }

    return source.substring( startIdx, endIdx );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectElements( Document document, String xpath ) {
    return (List<Element>) (List<?>) document.selectNodes( xpath );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectElements( Element element, String xpath ) {
    return (List<Element>) (List<?>) element.selectNodes( xpath );
  }
}
