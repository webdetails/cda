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

package pt.webdetails.cda.push;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * Websocket implementation for CDA queries.
 * This Endpoint is registered in plugin.spring.xml file by means of the {@link org.pentaho.platform.web.websocket.WebsocketEndpointConfig} bean.
 */
public class CdaPushQueryEndpoint extends Endpoint {

  private final Log logger = LogFactory.getLog( CdaPushQueryEndpoint.class );

  private CdaPushQueryMessageHandler queryHandler;

  /**
   * This method is called whenever a client creates a new websocket connection to the URL registered to
   * this endpoint.
   *
   * @param session the websocket session.
   * @param endpointConfig the endpoint configuration.
   */
  @Override
  public void onOpen( Session session, EndpointConfig endpointConfig ) {

    PentahoEndpointConfigurationHelper configHelper = new PentahoEndpointConfigurationHelper( endpointConfig );
    PentahoContext context = configHelper.getPentahoContext();

    context.run( () -> {
      this.queryHandler = new CdaPushQueryMessageHandler( session, context );

      queryHandler.getWebsocketJsonQueryEndpoint().onOpen( null );

      int maxMessageLength = configHelper.getMaxMessageLength();
      session.setMaxTextMessageBufferSize( maxMessageLength );
      session.setMaxBinaryMessageBufferSize( maxMessageLength );

      session.addMessageHandler( queryHandler );
    } );
  }

  /**
   * Executed whenever a connected websocket connection is terminated or timeouts according
   * to the web server or container configurations (the connection is not kept alive forever
   * to better manage server resources, so it is disconnected after a defined period of time - timeout).
   *
   * @param session the websocket session.
   * @param closeReason the reason for closing the connection.
   */
  @Override public void onClose( Session session, CloseReason closeReason ) {
    this.queryHandler.getWebsocketJsonQueryEndpoint().onClose();
  }

  /**
   * Executed whenever there is an error associated with the websocket connection. We close the
   * underlying websocket implementation when this happens
   *
   * @param session the websocket session.
   * @param throwable the {@link Throwable} that occurred.
   */
  @Override public void onError( Session session, Throwable throwable ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Error occurred in session id " + session.getId(), throwable );
    }
    this.queryHandler.getWebsocketJsonQueryEndpoint().onClose();
  }

}
