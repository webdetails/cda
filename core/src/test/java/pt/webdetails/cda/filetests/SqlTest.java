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


package pt.webdetails.cda.filetests;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import javax.swing.table.TableModel;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.CdaTestHelper;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cda.test.util.TableModelChecker;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SqlTest extends CdaTestCase {

  protected IQueryCache cache;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AbstractDataAccess.shutdownCache();
    cache = spy( getEnvironment().getQueryCache() );
    ( (CdaTestEnvironment) getEnvironment() ).setQueryCache( cache );
  }

  public SqlTest() {
    super();
  }

  public SqlTest( final String name ) {
    super( name );
  }

  public void testSqlQueryCache() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-sql.cda" );
    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );

    engine.doQuery( cdaSettings, queryOptions );

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2004-01-01" );
    engine.doQuery( cdaSettings, queryOptions );

    // Querying 2nd time to test cache, except not really
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );
    engine.doQuery( cdaSettings, queryOptions );

    verify( cache, times( 2 ) ).putTableModel( any( TableCacheKey.class ), any( TableModel.class ), anyInt(),
      any( ExtraCacheInfo.class ) );
  }

  public void testStringArrayParameter() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-list.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    TableModelChecker checker = new TableModelChecker();
    checker.setDoubleComparison( 2, "1e-8" );
    checker.setBigDecimalComparison( 3, "1e-14" );
    final SimpleTableModel expected = new SimpleTableModel(
      new Object[] { "Shipped", 2003L, 3303111.46, new BigDecimal( 3.30311146 ) },
      new Object[] { "Cancelled", 2003L, 75132.16, new BigDecimal( 0.07513216 ) },
      new Object[] { "Shipped", 2004L, 4750205.89, new BigDecimal( 4.75020589 ) },
      new Object[] { "Cancelled", 2004L, 187195.13, new BigDecimal( 0.18719513 ) },
      new Object[] { "Shipped", 2005L, 1513074.46, new BigDecimal( 1.51307446 ) } );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "status", new String[] { "Shipped", "Cancelled" } );

    TableModel table = engine.doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected, table );

    assertTrue( CdaTestHelper.columnContains( table, 0, "Shipped", "Cancelled" ) );
    assertFalse( CdaTestHelper.columnContains( table, 0, "Disputed" ) );

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    LinkedList<String> l = new LinkedList<String>();
    l.add( "Shipped" );
    l.add( "Cancelled" );
    queryOptions.addParameter( "status", l );

    table = engine.doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected, table );

  }

  public void testFormulaCacheSql() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-formula.cda" );
    SqlDataAccess dataAccess = spy( (SqlDataAccess) cdaSettings.getDataAccess( "1" ) );

    cdaSettings.addDataAccess( dataAccess );

    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "${TODAY()}" );
    queryOptions.setOutputType( "csv" );

    engine.doQuery( cdaSettings, queryOptions );

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "${DATE(2004;1;1)}" );
    engine.doQuery( cdaSettings, queryOptions );

    // Querying 2nd time to test cache (formula translated before cache check)
    // Doing query using manual TODAY - Cache should be used
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    Calendar cal = Calendar.getInstance();
    queryOptions.addParameter( "orderDate", "${DATE(" + cal.get( Calendar.YEAR ) + ";"
      + ( cal.get( Calendar.MONTH ) + 1 ) + ";" + cal.get( Calendar.DAY_OF_MONTH ) + ")}" );
    TableModel result = engine.doQuery( cdaSettings, queryOptions );

    verify( cache, times( 2 ) ).putTableModel( any( TableCacheKey.class ), any( TableModel.class ), anyInt(),
      any( ExtraCacheInfo.class ) );

    final TypedTableModel expected = new TypedTableModel(
      new String[] { "STATUS", "Year", "PRICE", "PriceInK" } );
    expected.addRow( "Shipped", 2003L, 3573701.2500000014, new BigDecimal( "3.5737012500000014" ) );
    expected.addRow( "Shipped", 2004L, 4750205.889999998, new BigDecimal( "4.750205889999998" ) );
    expected.addRow( "Shipped", 2005L, 1513074.4600000002, new BigDecimal( "1.51307446" ) );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.setDoubleComparison( 2, 1e-8 );
    checker.setBigDecimalComparison( 3, "1e-14" );
    checker.assertEquals( expected, result );
  }
}
