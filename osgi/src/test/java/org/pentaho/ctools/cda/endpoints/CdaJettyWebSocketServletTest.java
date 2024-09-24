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