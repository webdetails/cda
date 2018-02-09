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

package pt.webdetails.cda.connections.dataservices;

import org.dom4j.Element;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DataservicesConnectionTest {

  @Test
  public void testHashCode() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertEquals( dataservicesConnection.hashCode(), 0 );
  }

  @Test
  public void testEquals() throws Exception {
    DataservicesConnection dataservicesConnection = new DataservicesConnection();
    assertTrue( dataservicesConnection.equals( dataservicesConnection ) );

    DataservicesConnection differentDataservicesConnection = new DataservicesConnection();
    assertTrue( dataservicesConnection.equals( differentDataservicesConnection ) );
    assertFalse( dataservicesConnection.equals( new Object() ) );
    assertFalse( dataservicesConnection.equals( null ) );

    Element connection = mock( Element.class );
    when( connection.selectObject( Mockito.anyString() ) ).thenReturn( "" );
    differentDataservicesConnection.initializeConnection( connection );
    assertFalse( differentDataservicesConnection.equals( dataservicesConnection ) );
  }
}
