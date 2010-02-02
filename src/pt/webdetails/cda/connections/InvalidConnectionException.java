package pt.webdetails.cda.connections;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 6:38:21 PM
 */
public class InvalidConnectionException extends Exception {
  public InvalidConnectionException(String s, Exception cause) {
    super(s,cause);
  }
}
