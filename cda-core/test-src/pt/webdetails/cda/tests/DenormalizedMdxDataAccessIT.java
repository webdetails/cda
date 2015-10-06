/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.tests;

import junit.framework.Assert;
import org.junit.Test;
import pt.webdetails.cda.dataaccess.DenormalizedMdxDataAccess;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.tests.utils.CdaTestCase;

import java.util.List;

public class DenormalizedMdxDataAccessIT extends CdaTestCase {

  @Test
  public void testInterfaceNoBandedMode() {
    boolean bandedFound = false;
    DenormalizedMdxDataAccess dmda = new DenormalizedMdxDataAccess();
    List<PropertyDescriptor> properties = dmda.getInterface();
    for ( PropertyDescriptor pd : properties ) {
      if ( pd.getName() == "bandedMode" ) {
        bandedFound = true;
      }
    }

    Assert.assertFalse( bandedFound );
  }
}
