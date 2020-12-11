/*!
 * Copyright 2020 Webdetails, a Hitachi Vantara company. All rights reserved.
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
