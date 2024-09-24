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

package pt.webdetails.cda.settings;

import org.pentaho.reporting.libraries.resourceloader.ResourceLoader;

public interface ICdaResourceLoader extends ResourceLoader {

  public boolean hasReadAccess( String id );

  public long getLastModified( String id );

  public boolean isValidId( String id );

}
