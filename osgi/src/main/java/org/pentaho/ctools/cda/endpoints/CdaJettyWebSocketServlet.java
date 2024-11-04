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

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import pt.webdetails.cda.push.WebsocketJsonQueryEndpoint;

import jakarta.servlet.http.HttpServletRequest;

public class CdaJettyWebSocketServlet extends JettyWebSocketServlet {
  public CdaJettyWebsocket doWebSocketConnect( HttpServletRequest request, String subprotocol ) {
    if ( WebsocketJsonQueryEndpoint.ACCEPTED_SUB_PROTOCOL.equals( subprotocol ) ) {
      return new CdaJettyWebsocket( request, new WebsocketJsonQueryEndpoint() );
    }

    // returns 503 SERVICE UNAVAILABLE
    return null;
  }
  public boolean checkOrigin( HttpServletRequest request, String origin ) {
    // TODO Cross-domain origin logic
    return true;
  }

  @Override
  protected void configure( JettyWebSocketServletFactory jettyWebSocketServletFactory ) {

  }
}
