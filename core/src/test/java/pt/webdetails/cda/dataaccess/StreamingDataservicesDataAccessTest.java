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
import pt.webdetails.cda.connections.ConnectionCatalog;

import java.util.List;
import java.util.stream.Collectors;

import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;


public class StreamingDataservicesDataAccessTest extends TestCase {

  StreamingDataservicesDataAccess da;

  @Before
  public void setUp() {
    initBareEngine( getMockEnvironment() );
    da = new StreamingDataservicesDataAccess();
  }

  public void testGetType() {
    assertEquals( da.getType(), "streaming" );
  }

  public void testGetConnectionType() {
    assertEquals( da.getConnectionType(), ConnectionCatalog.ConnectionType.DATASERVICES );
  }

  public void testGetInterface() {
    List<PropertyDescriptor> daInterface = da.getInterface();

    List<PropertyDescriptor> dataServiceNameProperty = daInterface.stream()
        .filter( p -> p.getName().equals( "dataServiceName" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> queryProperty = daInterface.stream()
            .filter( p -> p.getName().equals( "query" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheProperty = daInterface.stream()
            .filter( p -> p.getName().equals( "cache" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheDurationProperty = daInterface.stream()
            .filter( p -> p.getName().equals( "cacheDuration" ) ).collect( Collectors.toList() );
    List<PropertyDescriptor> cacheKeysProperty = daInterface.stream()
            .filter( p -> p.getName().equals( "cacheKeys" ) ).collect( Collectors.toList() );

    assertEquals( dataServiceNameProperty.size(), 1 );
    assertEquals( queryProperty.size(), 1 );
    assertEquals( cacheProperty.size(), 0 );
    assertEquals( cacheDurationProperty.size(), 0 );
    assertEquals( cacheKeysProperty.size(), 0 );
  }

}
