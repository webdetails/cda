package org.pentaho.ctools.cda.endpoints;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import pt.webdetails.cda.push.WebsocketJsonQueryEndpoint;

import javax.servlet.http.HttpServletRequest;

public class CdaJettyWebSocketServlet extends WebSocketServlet {
  public WebSocket doWebSocketConnect( HttpServletRequest request, String subprotocol ) {
    if ( WebsocketJsonQueryEndpoint.ACCEPTED_SUB_PROTOCOL.equals( subprotocol ) ) {
      return new CdaJettyWebsocket( request, new WebsocketJsonQueryEndpoint() );
    }

    // returns 503 SERVICE UNAVAILABLE
    return null;
  }

  @Override
  public boolean checkOrigin( HttpServletRequest request, String origin ) {
    // TODO Cross-domain origin logic
    return true;
  }
}
