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


package pt.webdetails.cda.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;

import mondrian.olap.CacheControl;
import mondrian.olap.Connection;
import mondrian.olap.Schema;
import mondrian.olap.Util.PropertyList;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.settings.CdaSettings;

public class MondrianSchemaFlushServiceTest {

  @Test
  public void testFlushCdaMondrianCache() throws Exception {
    IMondrianCatalogService catalogService = mock( IMondrianCatalogService.class );
    IPentahoSession session = mock( IPentahoSession.class );
    final String catalogName1 = "first-catalog";
    MondrianCatalog catalog1 = mockCatalog( catalogName1 );
    final String catalogName2 = "second -catalog";
    MondrianCatalog catalog2 = mockCatalog( catalogName2 );

    when( catalogService.getCatalog( catalogName1, session ) ).thenReturn( catalog1 );
    when( catalogService.getCatalog( catalogName2, session ) ).thenReturn( catalog2 );

    CdaSettings cda = new CdaSettings( "settings.cda", new ResourceKey( "schema", "rk", null ) );

    final String connectionId1 = "c1";
    DataSource ds1 = mock( DataSource.class );
    MondrianConnection con1 = mockConnection( connectionId1, ds1, catalogName1 );
    cda.addConnection( con1 );
    final String connectionId2 = "c2";
    DataSource ds2 = mock( DataSource.class );
    MondrianConnection con2 = mockConnection( connectionId2, ds2, catalogName2 );
    cda.addConnection( con2 );

    CacheControl cacheControl = mock( CacheControl.class );

    Schema schema1 = getSchema( "the-schema" );
    Connection connection1 = mock( Connection.class );
    when( connection1.getCacheControl( any() ) ).thenReturn( cacheControl );
    when( connection1.getSchema() ).thenReturn( schema1 );

    Schema schema2 = getSchema( "another-schema" );
    Connection connection2 = mock( Connection.class );
    when( connection2.getCacheControl( any() ) ).thenReturn( cacheControl );
    when( connection2.getSchema() ).thenReturn( schema2 );

    HashMap<List<Object>, Connection> registry = new HashMap<>();
    registry.put( Arrays.asList( ds1, catalogName1 ), connection1 );
    registry.put( Arrays.asList( ds2, catalogName2 ), connection2 );

    MondrianSchemaFlushService flusher = new MondrianSchemaFlushService() {
      protected IMondrianCatalogService getCatalogService() {
        return catalogService;
      };
      @Override
      protected IPentahoSession getSession() {
        return session;
      }

      @Override
      protected Connection getConnection( DataSource dataSource, PropertyList properties ) {
        Connection res = registry.get( Arrays.asList( dataSource, properties.get( "Catalog" ) ) );
        if ( res == null ) {
          fail( "unexpected call" );
        }
        return res;
      }
    };

    String res = flusher.flushCdaMondrianCache( cda, connectionId1 );
    assertTrue( res.contains( schema1.getName() ) );
    assertFalse( res.contains( schema2.getName() ) );
    verify( cacheControl, times(1) ).flushSchema( schema1 );

    reset( cacheControl );
    res = flusher.flushCdaMondrianCache( cda, null );
    assertTrue( res.contains( schema1.getName() ) );
    assertTrue( res.contains( schema2.getName() ) );
    verify( cacheControl, times(1) ).flushSchema( schema1 );
    verify( cacheControl, times(1) ).flushSchema( schema2 );
  }

  protected Schema getSchema( final String schemaName2 ) {
    Schema schema2 = mock( Schema.class );
    when( schema2.getName() ).thenReturn( schemaName2 );
    return schema2;
  }

  protected MondrianCatalog mockCatalog( final String catalogName1 ) {
    MondrianCatalog catalog1 = mock( MondrianCatalog.class );
    when( catalog1.getDataSourceInfo() ).thenReturn( "" );
    when( catalog1.getDefinition() ).thenReturn( catalogName1 );
    return catalog1;
  }

  protected MondrianConnection mockConnection( final String connectionId, DataSource ds, String catalog ) throws InvalidConnectionException {
    MondrianJndiConnectionInfo jndiInfo = new MondrianJndiConnectionInfo( "jndi", catalog, "cube" );
    MondrianConnection con1 = mock( MondrianConnection.class );
    when( con1.getConnectionInfo() ).thenReturn( jndiInfo );
    when( con1.getId() ).thenReturn( connectionId );
    when( con1.getInitializedDataSourceProvider() ).thenReturn( new DataSourceProvider() {
      private static final long serialVersionUID = 1L;

      @Override
      public DataSource getDataSource() throws SQLException {
        return ds;
      }
      @Override
      public Object getConnectionHash() {
        return ds;
      }
    } );
    return con1;
  }

}
