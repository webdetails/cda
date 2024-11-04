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
    return true;
  }

  @Override
  protected void configure( JettyWebSocketServletFactory jettyWebSocketServletFactory ) {

  }
}
