package pt.webdetails.cda.connections;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:39:10 PM
 */
public class UnsupportedConnectionException extends Exception {
  public UnsupportedConnectionException(final String s, final Exception cause) {
    super(s,cause);
  }
  public UnsupportedConnectionException(final String msg){
    super(msg);
  }
}

