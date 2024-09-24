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

import org.junit.Test;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class DataservicesDriverLocalConnectionProviderTest {

  @Test( expected = NullPointerException.class)
  public void getLocalConnectionProviderTestUrlRequired2() throws SQLException, MalformedURLException {
    DataservicesDriverLocalConnectionProvider provider = spy( new DataservicesDriverLocalConnectionProvider(
            () -> null
    ) );
    provider.createConnection( "user", "password" );
  }

  @Test( expected = NullPointerException.class)
  public void getLocalConnectionProviderTestUrlRequired3() throws SQLException, MalformedURLException {
    DataservicesDriverLocalConnectionProvider provider = spy( new DataservicesDriverLocalConnectionProvider(
            () -> new ArrayList<>()
    ) );
    provider.createConnection( "user", "password" );
  }

  @Test( expected = NullPointerException.class)
  public void getLocalConnectionProviderTestUrlRequired() throws SQLException, MalformedURLException {
    IDataServiceClientService dataServiceMock = mock( IDataServiceClientService.class );
    DataservicesDriverLocalConnectionProvider provider = spy(new DataservicesDriverLocalConnectionProvider(
      () -> Arrays.asList(dataServiceMock)
    ) );
    provider.createConnection( "user", "password" );
  }

  @Test( expected = SQLException.class)
  public void getLocalConnectionProviderTestDriverRequired() throws SQLException, MalformedURLException {
    IDataServiceClientService dataServiceMock = mock( IDataServiceClientService.class );
    DataservicesDriverLocalConnectionProvider provider = spy( new DataservicesDriverLocalConnectionProvider(
      () -> Arrays.asList( dataServiceMock )
    ) );
    provider.setUrl( "jdbc:pdi://localhost:8080/pentaho/kettle?local=true" );
    provider.createConnection( "user", "password" );
  }

}
