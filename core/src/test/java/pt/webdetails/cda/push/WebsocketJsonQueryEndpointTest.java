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


package pt.webdetails.cda.push;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.dataaccess.StreamingDataservicesDataAccess;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.QueryParameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;

public class WebsocketJsonQueryEndpointTest {

  WebsocketJsonQueryEndpoint websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint();

  SettingsManager settingsManager;
  CdaSettings cdaSettings;
  StreamingDataservicesDataAccess streamingDataservicesDataAccess;
  TableExporter tableExporter;

  @Before
  public void setUp() throws Exception {
    CdaTestEngine engine = spy( new CdaTestEngine( getMockEnvironment() ) );
    CdaTestEngine.init( engine );

    settingsManager = mock( SettingsManager.class );
    cdaSettings = mock( CdaSettings.class );
    streamingDataservicesDataAccess = mock( StreamingDataservicesDataAccess.class );
    tableExporter = mock( TableExporter.class );

    doReturn( settingsManager ).when( engine ).getSettingsManager();
    doReturn( cdaSettings ).when( settingsManager ).parseSettingsFile( any() );
    doReturn( streamingDataservicesDataAccess ).when( cdaSettings ).getDataAccess( any() );
    doReturn( tableExporter ).when( engine ).getExporter( Mockito.<ExportOptions>any() );
  }

  @Test
  public void testConstructors() throws JSONException {
    WebsocketJsonQueryEndpoint websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( );
    assertNotNull( websocketJsonQueryEndpoint );
    websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( null );
    assertNotNull( websocketJsonQueryEndpoint );

    boolean fail = true;
    try{
      websocketJsonQueryEndpoint.onMessage( "{param1: \"value1\"}", null );
    } catch ( RuntimeException npe ) {
      if ( npe.getCause() instanceof NullPointerException ) {
        fail = false;
      }
    }
    assertFalse( fail );

    QueryParameters queryParameters = spy( new QueryParameters() );
    websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( queryParameters );
    assertNotNull( new WebsocketJsonQueryEndpoint( queryParameters ) );
    StringBuffer stringBuffer = new StringBuffer( );
    String message = "{param1: \"value1\"}";
    websocketJsonQueryEndpoint.onMessage( message, s -> stringBuffer.append( s ) );
    verify( queryParameters, times( 1 ) ).getParametersFromJson( message );
  }

  @Test
  public void testSetQueryParameters() throws JSONException {
    QueryParameters queryParameters = spy( new QueryParameters() );
    WebsocketJsonQueryEndpoint websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( );
    websocketJsonQueryEndpoint.setQueryParametersUtil( queryParameters );
    assertNotNull( new WebsocketJsonQueryEndpoint( queryParameters ) );
    StringBuffer stringBuffer = new StringBuffer( );
    String message = "{param1: \"value1\"}";
    websocketJsonQueryEndpoint.onMessage( message, s -> stringBuffer.append( s ) );
    verify( queryParameters, times( 1 ) ).getParametersFromJson( message );

  }

  @Test
  public void testOnMessage() throws Exception {
    assertEquals( null, websocketJsonQueryEndpoint.getConsumer() );
    StringBuilder sb = new StringBuilder( );
    websocketJsonQueryEndpoint.onMessage( "{param1: value1}", s -> sb.append( s ) );

    ArgumentCaptor<WebsocketJsonQueryEndpoint.WebsocketDisposableObserver> observerCapture = ArgumentCaptor.forClass( WebsocketJsonQueryEndpoint.WebsocketDisposableObserver.class );
    verify( streamingDataservicesDataAccess ).doPushStreamQuery( any(), observerCapture.capture() );

    assertNotEquals( null, websocketJsonQueryEndpoint.getConsumer() );
    assertFalse( websocketJsonQueryEndpoint.getConsumer().hasComplete() );
    assertFalse( websocketJsonQueryEndpoint.getDisposableConsumer().isDisposed() );
  }

  @Test
  public void testOnClose() throws Exception {
    assertEquals( null, websocketJsonQueryEndpoint.getConsumer() );

    StringBuilder sb = new StringBuilder( );
    websocketJsonQueryEndpoint.onMessage( "{param1: value1}", s -> sb.append( s ) );
    ArgumentCaptor<WebsocketJsonQueryEndpoint.WebsocketDisposableObserver> observerCapture = ArgumentCaptor.forClass( WebsocketJsonQueryEndpoint.WebsocketDisposableObserver.class );
    verify( streamingDataservicesDataAccess ).doPushStreamQuery( any(), observerCapture.capture() );

    assertNotEquals( null, websocketJsonQueryEndpoint.getConsumer() );
    assertFalse( websocketJsonQueryEndpoint.getConsumer().hasComplete() );
    websocketJsonQueryEndpoint.onClose();
    assertTrue( websocketJsonQueryEndpoint.getConsumer().hasComplete() );
  }

  private static class CdaTestEngine extends CdaEngine {

    protected CdaTestEngine( ICdaEnvironment env ) throws InitializationException {
      super( env );
    }

    public static CdaEngine init( ICdaEnvironment env ) {
      CdaEngine engine = spy( new CdaTestEngine( env ) );
      CdaEngine.initTestBare( engine );
      return engine;
    }

    public static void init( CdaEngine eng ) {
      CdaEngine.initTestBare( eng );
    }
  }
}
