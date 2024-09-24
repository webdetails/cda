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

package pt.webdetails.cda.connections.xpath;

import org.dom4j.Element;

public class XPathConnectionInfo {
  private String xqueryDataFile;

  public XPathConnectionInfo( final Element connection ) {
    xqueryDataFile = ( (String) connection.selectObject( "string(./DataFile)" ) );
  }

  public String getXqueryDataFile() {
    return xqueryDataFile;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final XPathConnectionInfo that = (XPathConnectionInfo) o;

    if ( xqueryDataFile != null ? !xqueryDataFile.equals( that.xqueryDataFile ) : that.xqueryDataFile != null ) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return xqueryDataFile != null ? xqueryDataFile.hashCode() : 0;
  }
}
