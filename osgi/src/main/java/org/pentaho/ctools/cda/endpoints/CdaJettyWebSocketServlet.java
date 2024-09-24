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
