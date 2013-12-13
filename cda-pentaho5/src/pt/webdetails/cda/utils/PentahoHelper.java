package pt.webdetails.cda.utils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;

public class PentahoHelper {

  public static boolean isAdmin( IPentahoSession session ) {
    return SecurityHelper.getInstance().isPentahoAdministrator( session );
  }

}
