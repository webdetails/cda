/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.cache;

import java.io.OutputStream;

/**
 * @deprecated to be serviced
 */
public interface ICacheScheduleManager {
  public void handleCall( String method, String obj, OutputStream out );
}
