package pt.webdetails.cda.dataaccess;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:39:10 PM
 */
public class InvalidParameterException extends Exception {
  public InvalidParameterException(final String s, final Exception cause) {
    super(s,cause);
  }
}