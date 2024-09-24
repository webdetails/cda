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

package pt.webdetails.cda.utils;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

public class MetadataTableModel extends TypedTableModel {

  private static final long serialVersionUID = 1L;

  private Map<String, String> metadata;

  public MetadataTableModel( String[] colNames, Class<?>[] colTypes, int rowCount ) {
    super( colNames, colTypes, rowCount );
    metadata = new HashMap<String, String>();
  }

  public MetadataTableModel( String[] colNames, Class<?>[] colTypes, int rowCount, Map<String, String> metadata ) {
    this( colNames, colTypes, rowCount );
    this.metadata.putAll( metadata );
  }

  public void setMetadata( String key, String value ) {
    metadata.put( key, value );
  }

  public void setMetadata( String key, Object value ) {
    metadata.put( key, value.toString() );
  }

  public String getMetadata( String key ) {
    return metadata.get( key );
  }

  public Map<String, String> getAllMetadata() {
    return metadata;
  }
}
