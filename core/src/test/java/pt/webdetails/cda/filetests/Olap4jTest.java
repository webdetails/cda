/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cda.filetests;

import java.math.BigDecimal;
import java.util.Iterator;

import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.dataaccess.Olap4JDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.TableModelChecker;

public class Olap4jTest extends CdaTestCase {

  public Olap4jTest() {
    super();
  }

  public Olap4jTest( final String name ) {
    super( name );
  }

  public void testOlap4jQuery() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-olap4j.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );

    engine.doQuery( cdaSettings, queryOptions );
    boolean hasCache = false;

    String query = ( (Olap4JDataAccess) cdaSettings.getDataAccess( "2" ) ).getQuery();
    IQueryCache cache = getEnvironment().getQueryCache();

    cache.clearCache();
    engine.doQuery( cdaSettings, queryOptions );

    System.out.println("Cache is " + cache.getClass().getCanonicalName() );
    System.out.println("Cache bypass: " + queryOptions.isCacheBypass() );

    engine.doQuery( cdaSettings, queryOptions );

    Iterator<TableCacheKey> testIterator = cache.getKeys().iterator();
    int cacheCount = 0;
    while ( testIterator.hasNext() ) {
      cacheCount++;
      testIterator.next();
    }

    System.out.println( "Cache size is " + cacheCount );

    for ( TableCacheKey key : cache.getKeys() ) {
        System.out.println( "Here with key: " + key );
      assertEquals( key.getQuery(), query );
      hasCache = true;
    }
    assertTrue( hasCache );
  }

  /**
   * this test is the same as testOlap4jQuery(), but now calls for a new data-access id 3, which in its turn is set to
   * use the new connection id 3, that has been defined to use new type 'olap4j.defaultolap4j'
   */
  public void testDefaultOlap4jQuery() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-olap4j.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    final QueryOptions queryOptions = new QueryOptions();
    // same as data-access id 2, but using new connection type 'olap4j.defaultolap4j'
    queryOptions.setDataAccessId( "3" );
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );

    TableModel result = engine.doQuery( cdaSettings, queryOptions );
    TypedTableModel expected =
      new TypedTableModel(
        new String[] { "[Time].[(All)]", "Year", "price", "PriceInK" },
        new Class<?>[] { String.class, String.class, Double.class, BigDecimal.class }, 2 );
    expected.addRow( "All Years", "2003", 3573701.2500000023d, new BigDecimal( "3.5737012500000023" ) );
    expected.addRow( "All Years", "2004", 4750205.889999998d, new BigDecimal( "4.750205889999998" ) );
    expected.addRow( "All Years", "2005", 1513074.4600000002d, new BigDecimal( "1.5130744600000002" ) );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.setDoubleComparison( 2, 1e-7 );
    checker.setBigDecimalComparison( 3, "1e-12" );
    checker.assertEquals( expected, result );
  }
}
