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
    this.websocketJsonQueryEndpoint.onMessage( query, this::processOutboundMessage );
  }

  public void onClose( int closeCode, String message ) {
    this.websocketJsonQueryEndpoint.onClose();
  }

  private void processOutboundMessage( String outboundMessage ) {
    try {
      this.connection.sendMessage( outboundMessage );
    } catch ( Exception e ) {
      logger.error( "Error sending message. Closing websocket...", e );

      // 1011:	Server error
      // The server is terminating the connection because
      // it encountered an unexpected condition that prevented it from
      // fulfilling the request.
      this.connection.close( 1011, "Error while sending message." );
    }
  }
}
