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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import pt.webdetails.cda.utils.AuditHelper;
import pt.webdetails.cda.utils.AuditHelper.QueryAudit;
import pt.webdetails.cda.utils.QueryParameters;

import jakarta.websocket.CloseReason;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import java.util.List;
import java.util.Map;

/**
 * This is the class that handles messages with queries to execute and then sends the results back to the
 * clients of the websocket server endpoint.
 */
public class CdaPushQueryMessageHandler implements MessageHandler.Whole<String> {

  private final Log logger = LogFactory.getLog( CdaPushQueryMessageHandler.class );

  private final Session session;
  private final PentahoContext context;
  private final AuditHelper auditHelper;
  private final WebsocketJsonQueryEndpoint websocketJsonQueryEndpoint;
  private final QueryParameters queryParametersUtil;
  public CdaPushQueryMessageHandler( Session session, PentahoContext context ) {
    this.session = session;
    this.context = context;
    this.websocketJsonQueryEndpoint = new WebsocketJsonQueryEndpoint( );
    this.auditHelper = new AuditHelper( CdaPushQueryMessageHandler.class, context.getSession() );
    this.queryParametersUtil = new QueryParameters();
  }

  /**
   * This method executes the query sent by the client of this websocket.
   *
   * Registers the Query operation with the Pentaho Auditing API.
   *
   * @param query The query to be executed, and sent over the websocket.
   */
  @Override
  public void onMessage( String query ) {
    context.run( () -> {
      try {
        Map<String, List<String>> params = queryParametersUtil.getParametersFromJson( query );
        String path = params.get( "path" ).get( 0 );
        IParameterProvider paramProvider = new SimpleParameterProvider( params );

        try ( QueryAudit qa = auditHelper.startQuery( path, paramProvider ) ) {
          this.websocketJsonQueryEndpoint.onMessage( query, this::processOutboundMessage );
        }

      } catch ( Exception e ) {
        logger.error( "Error processing message. Closing websocket...", e );
        processErrorMessage( e.getMessage() );
      }
    } );
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
