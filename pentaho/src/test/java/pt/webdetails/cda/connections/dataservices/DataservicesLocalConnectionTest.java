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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.context.api.IUrlProvider;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DataservicesLocalConnectionTest {

  @Before
  public void setup(){
    PluginEnvironment mockEnv = mock(PluginEnvironment.class);
    IUrlProvider mockUrlProvider = mock(IUrlProvider.class);
    doReturn( "http://localhost:8080" ).when( mockUrlProvider ).getWebappContextRoot();
    doReturn( "/pentaho" ).when( mockUrlProvider ).getWebappContextPath();
    doReturn( mockUrlProvider ).when( mockEnv ).getUrlProvider();
    PluginEnvironment.init(mockEnv);
  }

  @Test
  public void getDriverConnectionProviderTest() throws MalformedURLException {
    DriverConnectionProvider connectionProvider = new DataservicesLocalConnection().getDriverConnectionProvider( new TreeMap<>( ) );
    assertNotNull("Driver shouldn't be empty", connectionProvider.getDriver());
    assertNotNull("Url shouldn't be empty", connectionProvider.getUrl());
    assertEquals("jdbc:pdi://localhost:8080/pentaho/kettle?local=true", connectionProvider.getUrl());
  }

  @Test
  public void getDriverConnectionProviderWithParametersTest() throws MalformedURLException {
    Map<String, String> dataserviceParameters = new TreeMap<>( );
    dataserviceParameters.put( "param1", "value1" );
    DriverConnectionProvider connectionProvider = new DataservicesLocalConnection().getDriverConnectionProvider( dataserviceParameters );
    assertNotNull("Driver shouldn't be empty", connectionProvider.getDriver());
    assertNotNull("Url shouldn't be empty", connectionProvider.getUrl());
    assertEquals("jdbc:pdi://localhost:8080/pentaho/kettle?local=true&"+ IDataServiceClientService.PARAMETER_PREFIX +"param1=value1", connectionProvider.getUrl());
  }
}
