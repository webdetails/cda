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


package pt.webdetails.cda.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.utils.MetadataTableModel;

public class JsonExporter extends AbstractExporter {
  private static final String MIME_TYPE = "application/json";
  private static final Log logger = LogFactory.getLog( XmlExporter.class );
  private String attachmentName;
  boolean isJsonp = false;

  public JsonExporter() {
    super();
  }

  public JsonExporter( Map<String, String> extraSettings ) {
    super( extraSettings );
    if ( this.getSetting( "callback", null ) != null ) {
      isJsonp = true;
    }
    // the default should be null and if an attachment name is not given as attribute then the file should be
    // returned inline
    this.attachmentName = getSetting( ATTACHMENT_NAME_SETTING, null );

    logger.debug( "Initialized JsonExporter with attachment filename '" + attachmentName + "'" );
  }

  public void export( final OutputStream out, final TableModel tableModel ) throws ExporterException {

    try {
      JSONObject json = getTableAsJson( tableModel, null );

      try {

        if ( isJsonp ) {
          out.write( this.getSetting( "callback", "xxx" ).concat( "(" ).getBytes( "UTF-8" ) );
        }

        out.write( json.toString().getBytes( "UTF-8" ) );

        if ( isJsonp ) {
          out.write( ");".getBytes( "UTF-8" ) );
        }
        out.flush();
      } catch ( IOException e ) {
        throw new ExporterException( "IO Exception converting to utf-8", e );
      }

    } catch ( JSONException e ) {
      throw new ExporterException( "JSONException building object", e );
    }


  }


  public JSONObject getTableAsJson( TableModel tableModel, Integer rowLimit ) throws JSONException, ExporterException {
    JSONObject json = new JSONObject();

    // Generate metadata
    final JSONArray metadataArray = new JSONArray();

    final int columnCount = tableModel.getColumnCount();
    int rowCount = tableModel.getRowCount();

    if ( rowLimit != null ) {
      rowCount = Math.min( rowCount, rowLimit );
    }

    boolean[] isColumnDouble = new boolean[ columnCount ];
    for ( int i = 0; i < columnCount; i++ ) {
      JSONObject info = new JSONObject();
      info.put( "colIndex", i );
      info.put( "colName", tableModel.getColumnName( i ) );
      Class<?> columnClass = tableModel.getColumnClass( i );
      isColumnDouble[ i ] = ( columnClass.isAssignableFrom( Double.class ) );
      info.put( "colType", getColType( columnClass ) );
      metadataArray.put( info );
    }
    json.put( "metadata", metadataArray );

    if ( tableModel instanceof MetadataTableModel ) {
      json.put( "queryInfo", ( (MetadataTableModel) tableModel ).getAllMetadata() );
    }
    final JSONArray valuesArray = new JSONArray();

    for ( int rowIdx = 0; rowIdx < rowCount; rowIdx++ ) {
      final JSONArray rowArray = new JSONArray();
      for ( int colIdx = 0; colIdx < columnCount; colIdx++ ) {
        Object value = tableModel.getValueAt( rowIdx, colIdx );
        try {
          if ( value != null && isColumnDouble[ colIdx ] && ( ( (Double) value ).isInfinite() || ( (Double) value )
            .isNaN() ) ) {
            value = null;
            //value = Double.POSITIVE_INFINITY == (Double) value ? "Infinity" : "-Infinity";//workaround for JSON
            // issue with Infinity
          }
        } catch ( ClassCastException e ) {
          //just because it says Double doesn't mean we don't get oranges
        }
        rowArray.put( value );
      }
      valuesArray.put( rowArray );
    }
    json.put( "resultset", valuesArray );
    return json;
  }

  public String getMimeType() {
    return MIME_TYPE;
  }

  public String getAttachmentName() {
    return attachmentName;
  }
}
