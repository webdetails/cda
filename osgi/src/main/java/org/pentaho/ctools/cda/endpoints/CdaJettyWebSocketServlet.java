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
