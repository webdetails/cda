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

import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The {@code PentahoContext} class implements a memento pattern
 * of various thread-local state which constitute a Pentaho session and request context.
 */
class PentahoContext {
  private final IPentahoRequestContext requestContext;
  private final IPentahoSession session;
  private final Authentication authentication;
  private final MDCUtil mdcState;

  public PentahoContext( String contextPath, IPentahoSession session, Authentication authentication, MDCUtil mdcState ) {
    this( () -> contextPath, session, authentication, mdcState );
  }

  public PentahoContext( IPentahoRequestContext requestContext, IPentahoSession session, Authentication authentication, MDCUtil mdcState ) {
    this.requestContext = requestContext;
    this.session = session;
    this.authentication = authentication;
    this.mdcState = mdcState;
  }

  public void run( Runnable runnable ) {
    PentahoContext original = PentahoContext.capture();
    try {
      setCurrent();
      runnable.run();
    } finally {
      original.setCurrent();
    }
  }

  private void setCurrent() {
    PentahoRequestContextHolder.setRequestContext( requestContext );
    PentahoSessionHolder.setSession( session );
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication( authentication );
    mdcState.setContextMap();
  }

  public IPentahoSession getSession() {
    return session;
  }

  public static PentahoContext capture() {

    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    IPentahoSession session = PentahoSessionHolder.getSession();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    MDCUtil mdcState = new MDCUtil();

    return new PentahoContext( requestContext, session, authentication, mdcState );
  }
}
