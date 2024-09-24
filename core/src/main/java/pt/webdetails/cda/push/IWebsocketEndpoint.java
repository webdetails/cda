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

import java.util.List;
import java.util.function.Consumer;

public interface IWebsocketEndpoint {

  /**
   * The websocket endpoint implementation should call this when the websocket is opened.
   * This gives an opportunity to allocate resources needed for receiving messages, or to reply back to
   * the message consumer received as parameter.
   *
   * The use of the outboundMessageConsumer parameter is optional, so this interface
   * implementations should not rely on it.
   *
   * @param outboundMessageConsumer Parameter which will send a string message over the transport channel.
   */
  void onOpen( Consumer<String> outboundMessageConsumer );

  /**
   * This method should be called when the websocket endpoint implementation
   * receives a new String message.
   *
   * @param message The message received.
   * @param outboundMessageConsumer the consumer used for sending messages produced by the message received
   */
  void onMessage( String message, Consumer<String> outboundMessageConsumer );

  /**
   * The websocket endpoint implementation should call this when the websocket is closed.
   * This gives an opportunity to release resources allocated during the onOpen or onMessage.
   */
  void onClose();

  /**
   * Gets a list of subprotocols that this endpoint can handle.
   * @return a {@link List} of subprotocols.
   */
  List<String> getSubProtocols();

}
