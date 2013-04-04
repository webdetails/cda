package pt.webdetails.cda.utils;

/**
 * Created by IntelliJ IDEA.
 * User: andre
 * Date: Jul 14, 2011
 * Time: 11:13:10 PM
 */
public class InvalidOutputIndexException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidOutputIndexException(final String s, final Exception cause) {
    super(s,cause);
  }
}