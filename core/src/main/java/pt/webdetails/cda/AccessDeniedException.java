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



package pt.webdetails.cda;

public class AccessDeniedException extends Exception {

  private static final long serialVersionUID = 1L;

  public AccessDeniedException( final String s, final Exception cause ) {
    super( s, cause );
  }
}
