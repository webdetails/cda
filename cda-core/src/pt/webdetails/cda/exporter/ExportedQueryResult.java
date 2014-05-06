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

  private static void setResponseHeaders( HttpServletResponse response, String mimeType, String attachmentName ) {
    if ( StringUtils.isEmpty( mimeType ) && !StringUtils.isEmpty( attachmentName ) ) {
      mimeType = MimeTypes.getMimeType( attachmentName );
    }

    response.setContentType( mimeType );

    if ( attachmentName != null ) {
      response.setHeader( "content-disposition", "attachment; filename=" + attachmentName );
    }
  }
}
