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
package org.pentaho.ctools.cda.endpoints;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.WebSocket;
import pt.webdetails.cda.push.IWebsocketEndpoint;

import javax.servlet.http.HttpServletRequest;

public class CdaJettyWebsocket implements WebSocket.OnTextMessage {
  private static final Log logger = LogFactory.getLog( CdaJettyWebsocket.class );

  private Connection connection;

  private final HttpServletRequest request;
  private final IWebsocketEndpoint websocketJsonQueryEndpoint;

  CdaJettyWebsocket( HttpServletRequest request, IWebsocketEndpoint websocketJsonQueryEndpoint ) {
    this.request = request;
    this.websocketJsonQueryEndpoint = websocketJsonQueryEndpoint;
  }

  public void onOpen( Connection connection ) {
    this.connection = connection;

    this.websocketJsonQueryEndpoint.onOpen( this::processOutboundMessage );
  }

  public void onMessage( String query ) {
    try {
      this.websocketJsonQueryEndpoint.onMessage( query, this::processOutboundMessage );
    } catch ( Exception e ) {
      logger.error( "Error processing message.", e );
      processErrorMessage( e.getMessage() );
    }
  }

  public void onClose( int closeCode, String message ) {
    this.websocketJsonQueryEndpoint.onClose();
  }

  private void processOutboundMessage( String outboundMessage ) {
    try {
      this.connection.sendMessage( outboundMessage );
    } catch ( Exception e ) {
      logger.error( "Error sending message. Closing websocket...", e );
      processErrorMessage( "Error while sending message." );
    }
  }

  private void processErrorMessage( String errorMessage ) {
    try {
      if ( this.connection.isOpen() ) {
        // 1011: Server error
        // The server is terminating the connection because
        // it encountered an unexpected condition that prevented it from
        // fulfilling the request.
        this.connection.close( 1011, errorMessage );
      }
    } catch ( Exception e ) {
      logger.error( "Error closing socket, with message " + errorMessage, e );
    }
  }
}
