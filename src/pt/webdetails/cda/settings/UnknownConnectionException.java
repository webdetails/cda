package pt.webdetails.cda.settings;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 3:12:17 PM
 */
public class UnknownConnectionException extends Exception {
  public UnknownConnectionException(final String s, final Exception cause) {
    super(s,cause);
  }
}
