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

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdaSystemResourceLoader extends CdaFileResourceLoader {

  public CdaSystemResourceLoader( String name ) {
    super( name );
  }

  protected IReadAccess getReader() {
    return CdaEngine.getRepo().getPluginSystemReader( ".." );
  }

  protected IACAccess getAccessControl() {
    return new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        switch( access ) {
          case EXECUTE:
          case READ:
            return true;
          default:
            return false;
        }
      }
    };
  }
}
