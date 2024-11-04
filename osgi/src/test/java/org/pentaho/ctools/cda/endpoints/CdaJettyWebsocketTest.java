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

package org.pentaho.ctools.cda.endpoints;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cda.push.IWebsocketEndpoint;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WebSocket
public class CdaJettyWebsocketTest {
  public static final String OUTBOUND_MESSAGE_1 = "Outbound Message 1";
  public static final String OUTBOUND_MESSAGE_2 = "Outbound Message 2";

  public static final String INBOUND_MESSAGE = "Inbound Message 1";

  private HttpServletRequest mockRequest;
  private IWebsocketEndpoint mockPlatformWebsocketEndpoint;

  private CdaJettyWebsocket websocket;

  @Before
  public void setUp() {
    this.mockRequest = mock( HttpServletRequest.class );
    this.mockPlatformWebsocketEndpoint = mock( IWebsocketEndpoint.class );

    this.websocket = new CdaJettyWebsocket( this.mockRequest, this.mockPlatformWebsocketEndpoint );
  }

  @OnWebSocketConnect
  public void onConnect( Session session ) {
    this.websocket.onConnect( session );

    verify( this.mockPlatformWebsocketEndpoint, times( 1 ) ).onOpen( any( Consumer.class ) );
  }

  @Test
  public void onOpenMaySendMessages() throws IOException {
    doAnswer( invocation -> {
      Consumer<String> outboundMessageConsumer = (Consumer<String>) invocation.getArguments()[ 0 ];
      outboundMessageConsumer.accept( OUTBOUND_MESSAGE_1 );
      outboundMessageConsumer.accept( OUTBOUND_MESSAGE_2 );

      return null;
    } ).when( this.mockPlatformWebsocketEndpoint ).onOpen( any( Consumer.class ) );

    Session mockSession = mock( Session.class );
    RemoteEndpoint mockRemote = mock( RemoteEndpoint.class );
    when(mockSession.getRemote()).thenReturn( mockRemote );
    this.websocket.onConnect( mockSession );

    verify( mockRemote, times( 1 ) ).sendString( OUTBOUND_MESSAGE_1 );
    verify( mockRemote, times( 1 ) ).sendString( OUTBOUND_MESSAGE_2 );
  }

  @Test
  public void onMessage() {
    Session mockSession = mock( Session.class );
    this.websocket.onConnect( mockSession );

    this.websocket.onMessage( INBOUND_MESSAGE );

    verify( this.mockPlatformWebsocketEndpoint, times( 1 ) ).onMessage( same( INBOUND_MESSAGE ), any( Consumer.class ) );
  }

  @Test
  public void onClose() {
    this.websocket.onClose( 1000, "" );

    verify( this.mockPlatformWebsocketEndpoint, times( 1 ) ).onClose();
  }
}
