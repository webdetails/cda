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

package pt.webdetails.cda.connections.metadata;

import org.dom4j.Element;


public class MetadataConnectionInfo {
  private String domainId;
  private String xmiFile;

  public MetadataConnectionInfo( final Element connection ) {
    domainId = ( (String) connection.selectObject( "string(./DomainId)" ) );
    xmiFile = ( (String) connection.selectObject( "string(./XmiFile)" ) );
  }

  public MetadataConnectionInfo( String domainId, String xmiFile ) {
    this.domainId = domainId;
    this.xmiFile = xmiFile;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getXmiFile() {
    return xmiFile;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final MetadataConnectionInfo that = (MetadataConnectionInfo) o;

    if ( !domainId.equals( that.domainId ) ) {
      return false;
    }
    if ( !xmiFile.equals( that.xmiFile ) ) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = domainId.hashCode();
    result = 31 * result + xmiFile.hashCode();
    return result;
  }
}
