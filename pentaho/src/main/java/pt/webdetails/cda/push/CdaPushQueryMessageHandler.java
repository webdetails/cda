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

package pt.webdetails.cda.push;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * This is the class that handles messages with queries to execute and then sends the results back to the
 * clients of the websocket server endpoint.
 */
public class CdaPushQueryMessageHandler implements MessageHandler.Whole<String> {

  private final Log logger = LogFactory.getLog( CdaPushQueryMessageHandler.class );

  private Session session;
  private IPentahoRequestContext requestContext;

  private WebsocketJsonQueryEndpoint websocketJsonQueryEndpoint;

  public CdaPushQueryMessageHandler( Session session, IPentahoRequestContext requestContext ) {
    this.session = session;
    this.requestContext = requestContext;
    this.websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( );
  }

  /**
   * This method executes the query sent by the client of this websocket.
   *
   * @param query The query to be executed, and sent over the websocket.
   */
  @Override
  public void onMessage( String query ) {
    try {
      PentahoRequestContextHolder.setRequestContext( requestContext );

      AbstractAuthenticationToken principal = (AbstractAuthenticationToken) session.getUserPrincipal();
      if ( principal.isAuthenticated() ) {
        IPentahoSession pentahoSession = new StandaloneSession( principal.getName() );
        PentahoSessionHolder.setSession( pentahoSession );
        SecurityHelper.getInstance().becomeUser( principal.getName() );
      }

      this.websocketJsonQueryEndpoint.onMessage( query, this::processOutboundMessage );

    } catch ( Exception e ) {
      logger.error( "Error processing message. Closing websocket...", e );
      processErrorMessage( e.getMessage() );
    }
  }

  /**
   * Gets the websocket json query endpoint implementation that was created within this
   * message handler.
   * @return
   */
  public WebsocketJsonQueryEndpoint getWebsocketJsonQueryEndpoint() {
    return websocketJsonQueryEndpoint;
  }

  /**
   * The consumer for the inbound messages received from the web socket.
   *
   * @param outboundMessage the message (query) that will be processed.
   *
   * @return A String consumer for the messages received.
   */
  private void processOutboundMessage( String outboundMessage ) {
    try {
      if ( session.isOpen() ) {
        session.getBasicRemote().sendText( outboundMessage );
      }
    } catch ( Exception e ) {
      logger.error( "Error sending message. Closing websocket...", e );
      processErrorMessage( "Error sending message" );
    }
  }

  /**
   * The consumer for when a error happens in the query. This handler closes the socket and sends the
   * consumed String message.
   *
   * @param errorMessage the error message that will be sent as the reason message when closing the web socket.
   *
   * @return A String consumer that is sent to the web socket as a CloseReason.CloseCodes.UNEXPECTED_CONDITION message.
   */
  private void processErrorMessage( String errorMessage ) {
    try {
      if ( session.isOpen() ) {
        session.close( new CloseReason( CloseReason.CloseCodes.UNEXPECTED_CONDITION, errorMessage ) );
      }
    } catch ( Exception e ) {
      logger.error( "Error closing socket while processing error message [" + errorMessage + "]", e );
    }
  }
}
