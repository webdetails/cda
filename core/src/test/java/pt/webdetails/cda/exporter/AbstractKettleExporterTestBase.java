/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.exporter;

import org.junit.BeforeClass;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import pt.webdetails.cda.test.util.CdaTestHelper;

import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;

public class AbstractKettleExporterTestBase {

  @BeforeClass
  public static void init() throws KettleException {
    // not great but about half the time of a full init;
    // will not work where reporting libraries are needed
    KettleEnvironment.init();
    CdaTestHelper.initBareEngine( getMockEnvironment() );
  }
}
