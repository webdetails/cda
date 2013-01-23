package pt.webdetails.cda.connections;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 6:38:21 PM
 */
public class InvalidConnectionException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidConnectionException(final String s, final Exception cause) {
    super(s,cause);
  }
}
