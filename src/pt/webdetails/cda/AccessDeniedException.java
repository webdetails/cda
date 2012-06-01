package pt.webdetails.cda;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 6:38:21 PM
 */
public class AccessDeniedException extends Exception {

  private static final long serialVersionUID = 1L;

  public AccessDeniedException(final String s, final Exception cause) {
    super(s,cause);
  }
}