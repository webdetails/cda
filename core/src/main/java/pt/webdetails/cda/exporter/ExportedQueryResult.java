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

package pt.webdetails.cda.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * Standard cda query result
 */
public abstract class ExportedQueryResult {

  private Exporter exporter;

  public ExportedQueryResult( Exporter exporter ) {
    assert exporter != null;
    this.exporter = exporter;
  }

  public Exporter getExporter() {
    return exporter;
  }

  public void writeHeaders( HttpServletResponse response ) throws IOException {
    setResponseHeaders( response, exporter.getMimeType(), exporter.getAttachmentName() );
  }

  public void writeResponse( HttpServletResponse response ) throws ExporterException, IOException {
    setResponseHeaders( response, exporter.getMimeType(), exporter.getAttachmentName() );
    OutputStream out = response.getOutputStream();
    writeOut( out );
  }

  public abstract void writeOut( OutputStream out ) throws ExporterException;

  public String asString() throws ExporterException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeOut( out );
    return Util.toString( out.toByteArray() );
  }

  public String getContentType() {
    final String mimeType = exporter.getMimeType();
    final String attachmentName = exporter.getAttachmentName();

    return getContentType( mimeType, attachmentName );
  }

  private static String getContentType( String mimeType, String attachmentName ) {
    if ( StringUtils.isEmpty( mimeType ) && !StringUtils.isEmpty( attachmentName ) ) {
      return MimeTypes.getMimeType( attachmentName );
    }

    return mimeType;
  }

  public String getContentDisposition() {
    final String attachmentName = exporter.getAttachmentName();
    final String contentType = getContentType();

    return getContentDisposition( contentType, attachmentName );
  }

  private static String getContentDisposition( String mimeType, String attachmentName ) {
    if ( attachmentName != null ) {
      return "attachment; filename=" + attachmentName;
    }

    final String extension;
    switch ( mimeType ) {
      case MimeTypes.CSV:
        extension = "." + ExporterEngine.OutputType.CSV.toString();
        break;
      case MimeTypes.JSON:
        extension = "." + ExporterEngine.OutputType.JSON.toString();
        break;
      case MimeTypes.XLS:
        extension = "." + ExporterEngine.OutputType.XLS.toString();
        break;
      case MimeTypes.XML:
        extension = "." + ExporterEngine.OutputType.XML.toString();
        break;
      case MimeTypes.HTML:
        extension = "." + ExporterEngine.OutputType.HTML.toString();
        break;
      default: // e.g. BINARY
        extension = "";
        break;
    }

    return "inline; filename=doQuery" + extension;
  }

  private static void setResponseHeaders( HttpServletResponse response, String mimeType, String attachmentName ) {
    final String contentType = getContentType( mimeType, attachmentName );
    response.setContentType( contentType );

    final String contentDisposition = getContentDisposition( mimeType, attachmentName );
    response.setHeader( "content-disposition", contentDisposition );
  }
}
