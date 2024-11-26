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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
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

  @OnWebSocketOpen
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
      this.session.sendText( outboundMessage, Callback.NOOP );
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
        this.session.close( 1011, errorMessage, Callback.NOOP );
      }
    } catch ( Exception e ) {
      logger.error( "Error closing socket, with message " + errorMessage, e );
    }
  }
}
