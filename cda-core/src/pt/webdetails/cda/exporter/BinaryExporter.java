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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.table.TableModel;

public class BinaryExporter extends AbstractExporter {

  public static final String MYMETYPE_SETTING = "mimeType";

  private String attachmentName;
  private String mimeType;


  public BinaryExporter() {
    super();
  }


  public BinaryExporter( Map<String, String> extraSettings ) {
    super( extraSettings );
    mimeType = getSetting( MYMETYPE_SETTING, "octet-stream" );
    attachmentName = extraSettings.get( ATTACHMENT_NAME_SETTING );
  }


  public void export( final OutputStream out, final TableModel tableModel ) throws ExporterException {

    byte[] file = getBinaryFromFile( tableModel );

    if ( file != null ) {
      try {
        out.write( file );
      } catch ( IOException ioe ) {
        logger.error( "Exception while writing blob to ouput stream", ioe );
      }
    }

  }


  private byte[] getBinaryFromFile( TableModel tableModel ) throws ExporterException {
    final int columnCount = tableModel.getColumnCount();

    int colIdx = -1, fileNameColIdx = -1;
    for ( int i = 0; i < columnCount; i++ ) {
      Class<?> columnClass = tableModel.getColumnClass( i );
      if ( getColType( columnClass ).equals( "Blob" ) ) {
        colIdx = i;
      }
      if ( tableModel.getColumnName( i ).equals( "file_name" ) ) {
        fileNameColIdx = i;
      }
    }

    int rowCount = tableModel.getRowCount();

    if ( rowCount > 0 && fileNameColIdx >= 0 ) {
      attachmentName = (String) tableModel.getValueAt( 0, fileNameColIdx );
    }


    if ( colIdx >= 0 ) {
      if ( rowCount > 0 ) {
        Object value = tableModel.getValueAt( 0, colIdx );
        if ( value instanceof Blob ) {
          Blob v = (Blob) value;
          try {
            return v.getBytes( 0, (int) v.length() );
          } catch ( SQLException se ) {
            logger.error( "Exception caught while trying to read blob", se );
            return null;
          }
        }
        return (byte[]) value;
      }
    } else {
      logger.warn( "Did not find a blob column in the tableModel" );
    }

    return null;
  }


  public String getMimeType() {
    return "application/" + mimeType;
  }


  public String getAttachmentName() {
    return attachmentName;
  }
}
