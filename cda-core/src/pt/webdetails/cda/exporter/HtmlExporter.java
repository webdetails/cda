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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


public class HtmlExporter extends AbstractExporter {

  private static final Log logger = LogFactory.getLog( HtmlExporter.class );

  private static final String MIME_TYPE = "text/html";

  private static final String DEFAULT_TITLE = "CDA HTML Export";

  private static final String NULL = "#NULL";

  private static final class ExtraSettings {
    private static final String DATE_FORMAT = "dateFormat";
    //private static final String NUMBER_FORMAT = "#.##";
    private static final String TITLE = "title";
    private static final String FULL_HTML = "fullHtml";
  }

  private SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US );//TODO:..
  private String title = DEFAULT_TITLE;
  private boolean fullHtml = false;

  public HtmlExporter( Map<String, String> extraSettings ) {
    super();
    //format
    String providedFormat = extraSettings.get( ExtraSettings.DATE_FORMAT );
    if ( providedFormat != null ) {
      try {
        this.format = new SimpleDateFormat( providedFormat );
      } catch ( IllegalArgumentException e ) {
        //default not changed
        logger.error( "Invalid date format", e );
      }
    }
    //title
    String titleParam = extraSettings.get( ExtraSettings.TITLE );
    if ( titleParam != null ) {
      this.title = titleParam;
    }
    String fullHtmlParam = extraSettings.get( ExtraSettings.FULL_HTML );
    if ( fullHtmlParam != null ) {
      fullHtml = Boolean.parseBoolean( fullHtmlParam );
    }

  }


  @Override
  public void export( OutputStream out, TableModel tableModel ) throws ExporterException {
    final Document document = DocumentHelper.createDocument();
    Element table = null;

    if ( fullHtml ) {
      final Element html = document.addElement( "html" );
      final Element head = html.addElement( "head" );
      head.addElement( "title" ).addText( title );
      table = html.addElement( "body" ).addElement( "table" );
    } else {
      table = document.addElement( "table" );
    }

    final int columnCount = tableModel.getColumnCount();

    //table headers
    final Element headerRow = table.addElement( "tr" );
    for ( int i = 0; i < columnCount; i++ ) {
      String colName = tableModel.getColumnName( i );
      headerRow.addElement( "th" ).addText( colName );
    }

    //table body
    for ( int i = 0; i < tableModel.getRowCount(); i++ ) {
      Element row = table.addElement( "tr" );

      for ( int j = 0; j < columnCount; j++ ) {
        Element tableCell = row.addElement( "td" );
        Object value = tableModel.getValueAt( i, j );
        tableCell.setText( valueToText( value ) );

        if ( value instanceof Date ) {
          tableCell.setText( format.format( value ) );
        } else if ( value != null ) {
          // numbers can be safely converted via toString, as they use a well-defined format there
          tableCell.setText( value.toString() );
        }

      }
    }

    try {
      document.setXMLEncoding( "UTF-8" );

      OutputFormat outFormat = new OutputFormat();
      outFormat.setOmitEncoding( true );
      outFormat.setSuppressDeclaration( true );//otherwise msexcel/oocalc may not recognize content
      outFormat.setNewlines( true );
      outFormat.setIndentSize( columnCount );
      final Writer writer = new BufferedWriter( new OutputStreamWriter( out ) );
      XMLWriter xmlWriter = new XMLWriter( writer, outFormat );
      xmlWriter.write( document );
      xmlWriter.flush();
    } catch ( IOException e ) {
      throw new ExporterException( "IO Exception converting to utf-8", e );
    }
  }


  private String valueToText( Object value ) {
    if ( value == null ) {
      return NULL;
    } else if ( value instanceof Date ) {
      return format.format( (Date) value );
    } else {
      return value.toString();//as of now default formatting, maybe change this in the future for numeric values
    }
  }


  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }

  public String getExtension() {
    return "html";
  }


  @Override
  public String getAttachmentName() {
    // No attachment required
    return null;
  }

}
