/*!
 * Copyright 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.services;

import javax.sql.DataSource;

import mondrian.olap.CacheControl;
import mondrian.olap.DriverManager;
import mondrian.olap.Schema;
import mondrian.olap.Util;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownConnectionException;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;


public class MondrianSchemaFlushService {

  private static final String CATALOG_PROP = "Catalog";

  public String flushCdaMondrianCache( CdaSettings cdaSettings, String connectionId )
    throws UnknownConnectionException, InvalidConnectionException {
    Iterable<MondrianConnection> connections = getMondrianConnections( cdaSettings, connectionId );
    try {
      Set<String> schemas = new HashSet<>();
      for ( MondrianConnection mc : connections ) {
        schemas.add( flushMondrianSchema( mc ) );
      }
      if ( schemas.isEmpty() ) {
        return "no mondrian connections found";
      }
      return "schema cache flushed for " + StringUtils.join( schemas, ", " );
    } catch ( SQLException | InvalidConnectionException e ) {
      throw new InvalidConnectionException( "Error accessing DataSource", e );
    }
  }

  private Iterable<MondrianConnection> getMondrianConnections( CdaSettings cda, String connectionId )
    throws UnknownConnectionException {
    return ( connectionId == null
        ? cda.getConnectionsMap().values().stream()
        : Stream.of( cda.getConnection( connectionId ) ) )
            .filter( con -> con instanceof MondrianConnection )
            .map( con -> (MondrianConnection) con )::iterator;
  }

  private String flushMondrianSchema( MondrianConnection conn ) throws SQLException, InvalidConnectionException {
    DataSource dataSource = conn.getInitializedDataSourceProvider().getDataSource();
    String catalog = conn.getConnectionInfo().getCatalog();
    mondrian.olap.Connection monCon = getOlapConnection( catalog, dataSource );
    CacheControl cacheCtrl = monCon.getCacheControl( null );
    Schema schema = monCon.getSchema();
    cacheCtrl.flushSchema( schema );
    return schema.getName();
  }

  private mondrian.olap.Connection getOlapConnection( String catalog, DataSource dataSource ) {
    IMondrianCatalogService mondrianCatalogService = getCatalogService();
    MondrianCatalog selectedCatalog = mondrianCatalogService.getCatalog( catalog, getSession() );
    Util.PropertyList properties = Util.parseConnectString( selectedCatalog.getDataSourceInfo() );
    properties.put( CATALOG_PROP, selectedCatalog.getDefinition() );
    return getConnection( dataSource, properties );
  }

  protected mondrian.olap.Connection getConnection( DataSource dataSource, Util.PropertyList properties ) {
    return DriverManager.getConnection( properties, null, dataSource );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected IMondrianCatalogService getCatalogService() {
    return PentahoSystem.get( IMondrianCatalogService.class );
  }
}
