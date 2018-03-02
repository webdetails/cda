/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.dataaccess;

import junit.framework.TestCase;
import org.junit.Before;
import java.util.List;
import java.util.stream.Collectors;

import pt.webdetails.cda.connections.ConnectionCatalog;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;


public class DataservicesDataAccessTest extends TestCase {

  DataservicesDataAccess da;

  @Before
  public void setUp() {
    initBareEngine( getMockEnvironment() );
    da = new DataservicesDataAccess();
  }

  public void testGetType() {
    assertEquals( da.getType(), "sql" );
  }

  public void testGetConnectionType() {
    assertEquals( da.getConnectionType(), ConnectionCatalog.ConnectionType.DATASERVICES );
  }

  public void testGetInterface() {
    List<PropertyDescriptor> daInterface = da.getInterface();

    List<PropertyDescriptor> dataServiceNameProperty = daInterface.stream()
        .filter( p -> p.getName().equals( "dataServiceName" ) ).collect( Collectors.toList() );

    assertEquals( dataServiceNameProperty.size(), 1 );
  }

}
