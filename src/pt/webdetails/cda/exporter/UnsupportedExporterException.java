package pt.webdetails.cda.exporter;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:39:10 PM
 */
public class UnsupportedExporterException extends Exception {
  public UnsupportedExporterException(final String s, final Exception cause) {
    super(s,cause);
  }
}