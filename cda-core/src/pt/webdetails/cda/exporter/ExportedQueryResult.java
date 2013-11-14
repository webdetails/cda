package pt.webdetails.cda.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * Standard cda query result
 */
public class ExportedQueryResult {

  private Exporter exporter;
  private TableModel table;

  public ExportedQueryResult ( Exporter exporter, TableModel table ) {
    assert exporter != null && table != null;

    this.exporter = exporter;
    this.table = table;
  }

  public Exporter getExporter() {
    return exporter;
  }

  public void writeResponse( HttpServletResponse response ) throws ExporterException, IOException {
    setResponseHeaders( response, exporter.getMimeType(), exporter.getAttachmentName() );
    OutputStream out = response.getOutputStream();
    writeOut( out );
  }

  public void writeOut( OutputStream out ) throws ExporterException {
    exporter.export( out, table );
  }

  public String asString() throws ExporterException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeOut( out );
    return Util.toString( out.toByteArray() );
  }

  private static void setResponseHeaders(HttpServletResponse response, String mimeType, String attachmentName) {
    if (StringUtils.isEmpty( mimeType ) && !StringUtils.isEmpty( attachmentName )) {
      mimeType = MimeTypes.getMimeType( attachmentName );
    }
    response.setHeader("Content-Type", mimeType);

    if (attachmentName != null)
    {
      response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
    } 
  }

}
