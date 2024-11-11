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


package pt.webdetails.cda.utils;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.util.logging.SimpleLogger;
import pt.webdetails.cda.CdaPluginEnvironment;
import pt.webdetails.cpf.audit.CpfAuditHelper;

import java.util.UUID;

/**
 * Helper for registering query operations with Pentaho's auditing system.
 */
public class AuditHelper {
  private final Class<?> forClass;
  private final ILogger auditLogger;
  private final IPentahoSession session;

  /**
   * Creates an audit helper object.
   *
   * @param forClass The class for which auditing is being performed.
   * @param session The Pentaho session.
   */
  public AuditHelper( Class<?> forClass, IPentahoSession session ) {
    this( forClass, session, null );
  }

  /**
   * Creates an audit helper object.
   *
   * @param forClass The class for which auditing is being performed.
   * @param session  The Pentaho session.
   * @param logger   The logger to use, in case of error registering the audit operation;
   *                 can be {@code null}.
   */
  public AuditHelper( Class<?> forClass, IPentahoSession session, ILogger logger ) {
    this.forClass = forClass;
    this.session = session;
    this.auditLogger = logger == null ? new SimpleLogger( forClass.getName() ) : logger;
  }

  /**
   * Gets the "object name" used for auditing purposes.
   *
   * @return The auditing object name.
   */
  private String getAuditObjectName() {
    return forClass.getName();
  }

  /**
   * Gets the name of the CDA plugin.
   *
   * @return The name of this plugin.
   */
  private String getPluginName() {
    return CdaPluginEnvironment.getInstance().getPluginId();
  }

  /**
   * Audits the start of a query and returns an automatically closeable object
   * which, when closed, audits the end of the query.
   *
   * To be user with the try-with-resources statement.
   *
   * Additionally, sets the MDC instance identifier with the returned request identifier.
   *
   * @param path          The query path.
   * @param requestParams The parameters associated with request; {@code null} if none.
   * @return The query audit object.
   */
  public QueryAudit startQuery( String path, IParameterProvider requestParams ) {

    final long start = System.currentTimeMillis();

    UUID requestId = CpfAuditHelper.startAudit(
      getPluginName(),
      path,
      getAuditObjectName(),
      session,
      auditLogger,
      requestParams );

    if ( requestId != null ) {
      MDCUtil.setInstanceId( requestId.toString() );
    }

    return new QueryAudit( requestId, path, start );
  }

  public class QueryAudit implements AutoCloseable {

    private final UUID requestId;
    private final String path;
    private final long start;

    private QueryAudit( UUID requestId, String path, long start ) {
      this.requestId = requestId;
      this.path = path;
      this.start = start;
    }

    public UUID getRequestId() {
      return requestId;
    }

    @Override
    public void close() throws Exception {

      // Audits the end of a query.

      final long end = System.currentTimeMillis();

      CpfAuditHelper.endAudit(
        getPluginName(),
        path,
        getAuditObjectName(),
        session,
        auditLogger,
        start,
        requestId,
        end );
    }
  }
}
