package pt.webdetails.cda.settings;

/**
 * Error finding or parsing a cda
 */
public class CdaSettingsReadException extends Exception {

  public CdaSettingsReadException( String message, Throwable inner ) {
    super( message, inner );
  }

  private static final long serialVersionUID = 1L;

}
