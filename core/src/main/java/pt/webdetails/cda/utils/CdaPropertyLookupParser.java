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


package pt.webdetails.cda.utils;

import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.util.PropertyLookupParser;

/**
 * this class extends org.pentaho.reporting.engine.classic.core.util.PropertyLookupParser
 */
public class CdaPropertyLookupParser extends PropertyLookupParser {

  private static final long serialVersionUID = 1L;

  private DataRow parameters = null;

  public CdaPropertyLookupParser( final DataRow parameters ) {
    this.parameters = parameters;
  }

  public CdaPropertyLookupParser() {

  }

  protected String lookupVariable( final String property ) {
    return parameters.get( property ).toString();
  }
}
