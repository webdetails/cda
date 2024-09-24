/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.utils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;

public class PentahoHelper {

  public static boolean isAdmin( IPentahoSession session ) {
    return SecurityHelper.getInstance().isPentahoAdministrator( session );
  }

}
