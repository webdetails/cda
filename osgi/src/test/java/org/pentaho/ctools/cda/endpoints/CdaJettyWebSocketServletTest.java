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

import org.eclipse.jetty.websocket.WebSocket;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cda.push.WebsocketJsonQueryEndpoint;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CdaJettyWebSocketServletTest {
  private HttpServletRequest mockRequest;

  private CdaJettyWebSocketServlet servlet;

  @Before
  public void setUp() {
    this.mockRequest = mock( HttpServletRequest.class );

    this.servlet = new CdaJettyWebSocketServlet();
  }

  @Test
  public void doWebSocketConnectKnownSubprotocol() {
    String subprotocol = WebsocketJsonQueryEndpoint.ACCEPTED_SUB_PROTOCOL;

    WebSocket webSocket = this.servlet.doWebSocketConnect( this.mockRequest, subprotocol );

    assertNotNull( webSocket );
  }

  @Test
  public void doWebSocketConnectUnknownSubprotocol() {
    String subprotocol = "Unknown-protocol-for-tests";

    WebSocket webSocket = this.servlet.doWebSocketConnect( this.mockRequest, subprotocol );

    assertNull( webSocket );
  }

  @Test
  public void doWebSocketConnectNullSubprotocol() {
    String subprotocol = null;

    WebSocket webSocket = this.servlet.doWebSocketConnect( this.mockRequest, subprotocol );

    assertNull( webSocket );
  }

  @Test
  public void checkOriginAlwaysTrue() {
    String originUri = "http://for-now-any-url-should-return-true";

    assertTrue( this.servlet.checkOrigin( this.mockRequest, originUri ) );
  }
}