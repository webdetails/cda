/*!
 * Copyright 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import pt.webdetails.cda.push.IWebsocketEndpoint;

import jakarta.servlet.http.HttpServletRequest;

@WebSocket
public class CdaJettyWebsocket {
  private static final Log logger = LogFactory.getLog( CdaJettyWebsocket.class );

  private Session session;

  private final HttpServletRequest request;
  private final IWebsocketEndpoint websocketJsonQueryEndpoint;

  public CdaJettyWebsocket( HttpServletRequest request, IWebsocketEndpoint websocketJsonQueryEndpoint ) {
    this.request = request;
    this.websocketJsonQueryEndpoint = websocketJsonQueryEndpoint;
  }

  @OnWebSocketConnect
  public void onConnect( Session session ) {
    this.session = session;
    this.websocketJsonQueryEndpoint.onOpen( this::processOutboundMessage );
  }

  @OnWebSocketMessage
  public void onMessage( String query ) {
    try {
      this.websocketJsonQueryEndpoint.onMessage( query, this::processOutboundMessage );
    } catch ( Exception e ) {
      logger.error( "Error processing message.", e );
      processErrorMessage( e.getMessage() );
    }
  }

  @OnWebSocketClose
  public void onClose( int statusCode, String reason ) {
    this.websocketJsonQueryEndpoint.onClose();
  }

  private void processOutboundMessage( String outboundMessage ) {
    try {
      this.session.getRemote().sendString( outboundMessage );
    } catch ( Exception e ) {
      logger.error( "Error sending message. Closing websocket...", e );
      processErrorMessage( "Error while sending message." );
    }
  }

  private void processErrorMessage( String errorMessage ) {
    try {
      if ( this.session.isOpen() ) {
        // 1011: Server error
        // The server is terminating the connection because
        // it encountered an unexpected condition that prevented it from
        // fulfilling the request.
        this.session.close( 1011, errorMessage );
      }
    } catch ( Exception e ) {
      logger.error( "Error closing socket, with message " + errorMessage, e );
    }
  }
}
