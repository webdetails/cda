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

/**
 * Error finding or parsing a cda
 */
public class CdaSettingsReadException extends Exception {

  public CdaSettingsReadException( String message, Throwable inner ) {
    super( message, inner );
  }

  private static final long serialVersionUID = 1L;

}
