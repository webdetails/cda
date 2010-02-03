package pt.webdetails.cda.dataaccess;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:39:10 PM
 */
public class QueryException extends Exception {
  public QueryException(final String s, final Exception cause) {
    super(s,cause);
  }
}