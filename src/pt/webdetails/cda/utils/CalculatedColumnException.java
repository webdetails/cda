package pt.webdetails.cda.utils;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:39:10 PM
 */
public class CalculatedColumnException extends Exception {

  private static final long serialVersionUID = 1L;

  public CalculatedColumnException(final String s, final Exception cause) {
    super(s,cause);
  }
}