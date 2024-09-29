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


package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.SimpleDataAccess.IDataSourceQuery;
import pt.webdetails.cda.events.QueryErrorEvent;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitor;
import pt.webdetails.cpf.messaging.IEventPublisher;

import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

public class DataAccessTest {

  @BeforeClass
  public static void init() {
    AbstractDataAccess.shutdownCache();
    initBareEngine( getMockEnvironment() );
  }

  @Test
  public void testGetLabel() {
    DataAccess dataAccessTestInterface = new SimpleDataAccess() {
      @Override public String getType() {
        return "expected type";
      }

      @Override public ConnectionCatalog.ConnectionType getConnectionType() {
        return null;
      }

      @Override protected IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow )
        throws QueryException {
        return null;
      }
    };

    assertEquals( "expected type", dataAccessTestInterface.getLabel() );
    assertEquals( dataAccessTestInterface.getType(), dataAccessTestInterface.getLabel() );
  }

  @Test
  public void testIterableParameters() throws Exception {
    DataAccess da = new TestDataAccess( "a", "a" ) {
      protected TableModel queryDataSource( QueryOptions queryOptions ) throws QueryException {
        if ( queryOptions.getParameter( "p" ).getStringValue().equals( "baah" )
          && queryOptions.getParameter( "a" ).getStringValue().equals( "meh" ) ) {
          return new SimpleTableModel( new Object[] { "a1" }, new Object[] { "a2" } );
        }
        throw new AssertionError( "no params" );
      }
    };
    DataAccess db = new TestDataAccess( "b", "b" ) {
      protected TableModel queryDataSource( QueryOptions queryOptions ) throws QueryException {
        return new SimpleTableModel( new Object[] { null, "b1" }, new Object[] { null, "b2" } );
      }
    };
    CdaSettings settings = mock( CdaSettings.class );
    when( settings.getDataAccess( "a" ) ).thenReturn( da );
    when( settings.getDataAccess( "b" ) ).thenReturn( db );
    final ArrayList<String> params = new ArrayList<>();
    DataAccess dataAccess = new TestDataAccess( "dataAccess", "test" ) {
      protected TableModel queryDataSource( QueryOptions queryOptions ) throws QueryException {
        params.add( queryOptions.getParameter( "p1" ).getStringValue()
          + ":" + queryOptions.getParameter( "p2" ).getStringValue() );
        return new SimpleTableModel( new Object[ 0 ] );
      }
    };
    dataAccess.setCdaSettings( settings );
    when( settings.getDataAccess( "dataAccess" ) ).thenReturn( dataAccess );
    QueryOptions opts = new QueryOptions();
    opts.setDataAccessId( "dataAccess" );
    opts.addParameter( "p1", "$FOREACH(a, 0, p=baah, a=meh)" );
    opts.addParameter( "p2", "$FOREACH(b, 1)" );
    dataAccess.doQuery( opts );
    assertTrue( params.contains( "a1:b1" ) );
    assertTrue( params.contains( "a1:b2" ) );
    assertTrue( params.contains( "a2:b1" ) );
    assertTrue( params.contains( "a2:b2" ) );
    assertEquals( 4, params.size() );
  }

  @Test
  public void testQueryDataSource() throws Exception {
    ICdaEnvironment env = getMockEnvironment();
    IQueryCache cache = mock( IQueryCache.class );
    when( env.getQueryCache() ).thenReturn( cache );
    IEventPublisher pub = mock( IEventPublisher.class );
    when( env.getEventPublisher() ).thenReturn( pub );
    initBareEngine( env );

    final IDataSourceQuery dsQuery = mock( IDataSourceQuery.class );
    when( dsQuery.getTableModel() ).thenReturn( new SimpleTableModel( new Object[ 0 ] ) );

    CdaSettings settings = mock( CdaSettings.class );
    TestSimpleDataAccess dataAccess = spy( new TestSimpleDataAccess( "id", "name", null, "q", "test" ) {
      protected IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException {
        assertTrue( parameterDataRow.get( "p1" ).equals( "val" ) );
        return dsQuery;
      }
    } );
    dataAccess.setCdaSettings( settings );
    dataAccess.setParameters( Collections.singletonList( new Parameter( "p1", "String", "", "", "public" ) ) );
    QueryOptions queryOpts = new QueryOptions();
    queryOpts.addParameter( "p1", "val" );
    dataAccess.setCacheEnabled( true );

    dataAccess.doQuery( queryOpts );
    verify( dataAccess, times( 1 ) ).performRawQuery( any( ParameterDataRow.class ) );
    verify( cache, times( 1 ) ).getTableModel( any( TableCacheKey.class ) );
    verify( cache, times( 1 ) ).putTableModel( any( TableCacheKey.class ), any( TableModel.class ), anyInt(),
      any( ExtraCacheInfo.class ) );

  }

  @Test
  public void testQueryDataSourceError() throws Exception {
    ICdaEnvironment env = getMockEnvironment();
    IQueryCache cache = mock( IQueryCache.class );
    when( env.getQueryCache() ).thenReturn( cache );
    IEventPublisher pub = mock( IEventPublisher.class );
    when( env.getEventPublisher() ).thenReturn( pub );
    initBareEngine( env );
    CdaSettings settings = mock( CdaSettings.class );
    final QueryException toThrow = new QueryException( "test", new Exception( "cause" ) );
    TestSimpleDataAccess dataAccess = spy( new TestSimpleDataAccess( "id", "name", null, "q", "test" ) {
      protected IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException {
        throw toThrow;
      }
    } );
    dataAccess.setCdaSettings( settings );
    QueryOptions queryOpts = new QueryOptions();
    try {
      dataAccess.doQuery( queryOpts );
      fail( "no exception" );
    } catch ( Exception e ) {
      assertEquals( toThrow, e );
    }
    verify( pub, times( 1 ) ).publish( any( QueryErrorEvent.class ) );
  }

  abstract static class TestDataAccess extends AbstractDataAccess {
    public TestDataAccess( String id, String name ) {
      super( id, name );
    }

    public void setQuery( String query ) {
    }

    public void accept( DomVisitor v, Element ele ) {
    }

    public String getType() {
      return "test";
    }

    public ConnectionType getConnectionType() {
      return ConnectionType.NONE;
    }
  }

  abstract static class TestSimpleDataAccess extends SimpleDataAccess {

    public TestSimpleDataAccess( String id, String name, String connectionId, String query, String queryType ) {
      super( id, name, connectionId, query, queryType );
    }

    @Override
    public TableModel queryDataSource( QueryOptions queryOptions ) throws QueryException {
      return super.queryDataSource( queryOptions );
    }

    @Override
    public String getType() {
      return getQueryType();
    }

    @Override
    public ConnectionType getConnectionType() {
      return ConnectionType.NONE;
    }

  }
}
