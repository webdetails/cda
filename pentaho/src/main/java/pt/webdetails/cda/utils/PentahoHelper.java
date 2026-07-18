/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.utils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;

public class PentahoHelper {

  public static boolean isAdmin( IPentahoSession session ) {
    return SecurityHelper.getInstance().isPentahoAdministrator( session );
  }

}
