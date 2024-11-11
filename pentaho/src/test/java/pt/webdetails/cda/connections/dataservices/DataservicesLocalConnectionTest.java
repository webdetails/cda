/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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

  @Test
  public void getDriverConnectionProviderWithNullParameterTest() throws MalformedURLException {
    Map<String, String> dataserviceParameters = new TreeMap<>( );
    dataserviceParameters.put( "param1", "value1" );
    dataserviceParameters.put( "paramNull", null );
    DriverConnectionProvider connectionProvider = new DataservicesLocalConnection().getDriverConnectionProvider( dataserviceParameters );
    assertNotNull("Driver shouldn't be empty", connectionProvider.getDriver());
    assertNotNull("Url shouldn't be empty", connectionProvider.getUrl());
    assertEquals("jdbc:pdi://localhost:8080/pentaho/kettle?local=true&"+ IDataServiceClientService.PARAMETER_PREFIX +"param1=value1", connectionProvider.getUrl());
  }
}
