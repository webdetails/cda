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

package pt.webdetails.cda.dataaccess.kettle;

public class KettleAdapterException extends Exception {
  private static final long serialVersionUID = 1L;

  public KettleAdapterException( String message, Throwable cause ) {
    super( message, cause );
  }

  public KettleAdapterException( String message ) {
    super( message );
  }

  public KettleAdapterException( Throwable cause ) {
    super( cause );
  }
}
