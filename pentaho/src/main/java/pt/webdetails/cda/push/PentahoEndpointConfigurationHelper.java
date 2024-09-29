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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.websocket.WebsocketEndpointConfig;
import org.springframework.security.core.Authentication;

import javax.websocket.EndpointConfig;

/**
 * The {@code PentahoEndpointConfigurationHelper} helps reading both general and
 * Pentaho-specific information from a Web Socket's {@link EndpointConfig} object.
 */
class PentahoEndpointConfigurationHelper {

  private static final WebsocketEndpointConfig configToRead = WebsocketEndpointConfig.getInstanceToReadProperties();

  private final EndpointConfig endpointConfig;

  /**
   * Creates an endpoint configuration helper object.
   *
   * @param endpointConfig The web socket endpoint configuration.
   */
  public PentahoEndpointConfigurationHelper( EndpointConfig endpointConfig ) {
    this.endpointConfig = endpointConfig;
  }

  /**
   * Creates a Pentaho context from the information in the given endpoint configuration.
   *
   * The extra information in the endpoint configuration is populated by
   * {@code org.pentaho.platform.web.servlet.PluginDispatchServlet.getServerWebsocketEndpointConfigurator()}.
   *
   * @return The Pentaho context.
   */
  public PentahoContext getPentahoContext() {

    String contextPath = getUserPropValue( getServletContextPathUserPropName() );
    IPentahoSession pentahoSession = getUserPropValue( PentahoSystem.PENTAHO_SESSION_KEY );
    Authentication authentication = getUserPropValue( PentahoSystem.PENTAHO_AUTH_KEY );
    MDCUtil mdcState = getUserPropValue( PentahoSystem.PENTAHO_MDC_KEY );

    return new PentahoContext( contextPath, pentahoSession, authentication, mdcState );
  }

  /**
   * Gets the maximum message length.
   * @return the maximum message length.
   */
  public int getMaxMessageLength() {
    return Integer.parseInt( getUserPropValue( getMaxMessageUserPropName() ).toString() );
  }

  /**
   * Gets the value of a user property.
   *
   * @param name The name of the user property.
   * @return The value of the property, cast to {@code TValue}, if set; {@code null}, otherwise.
   */
  @SuppressWarnings( "unchecked" )
  public <V> V getUserPropValue( String name ) {
    return (V) endpointConfig.getUserProperties().get( name );
  }

  /**
   * Gets the name of the "Maximum Message Length" user property.
   * @return The name of the property.
   */
  public static String getMaxMessageUserPropName() {
    return configToRead.getMaxMessagePropertyName();
  }

  /**
   * Gets the name of the "Servlet Context Path" user property.
   * @return The name of the property.
   */
  public static String getServletContextPathUserPropName() {
    return configToRead.getServletContextPathPropertyName();
  }
}
