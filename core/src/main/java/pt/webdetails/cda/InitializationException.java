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


package pt.webdetails.cda;


public class InitializationException extends RuntimeException {

  private static final long serialVersionUID = 1089220229330479839L;

  public InitializationException( final String s, final Exception cause ) {
    super( s, cause );
  }

}
