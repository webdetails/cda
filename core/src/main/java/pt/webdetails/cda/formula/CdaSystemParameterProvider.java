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



package pt.webdetails.cda.formula;

import java.util.Properties;

public class CdaSystemParameterProvider implements ICdaParameterProvider {

  Properties props = System.getProperties();

  @Override
  public Object getParameter( String name ) {
    if ( props.containsKey( name ) ) {
      return props.get( name );
    }
    return null;
  }
}
