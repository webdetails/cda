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


package pt.webdetails.cda.formula;

import junit.framework.TestCase;

public class CdaSystemParameterProviderTest extends TestCase {
  CdaSystemParameterProvider cdaSystemParameterProvider;

  public void setUp() throws Exception {
    super.setUp();
    cdaSystemParameterProvider = new CdaSystemParameterProvider();
  }

  public void testGetParameter() throws Exception {
    assertNull( cdaSystemParameterProvider.getParameter( "test" ) );
    assertNotNull( cdaSystemParameterProvider.getParameter( "java.vendor" ) );
  }
}
