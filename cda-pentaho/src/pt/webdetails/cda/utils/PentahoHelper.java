package pt.webdetails.cda.utils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;

/**
 * For helper APIs incompatible between 4.x and 5.x
 */
public class PentahoHelper {

  public static boolean isAdmin( IPentahoSession session ) {
    return SecurityHelper.isPentahoAdministrator( session );
  }

}
