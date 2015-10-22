/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda;

import javax.swing.table.TableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.test.CdaTestCase;
import pt.webdetails.cda.utils.test.CdaTestHelper;
import pt.webdetails.cda.utils.test.TableModelChecker;
import pt.webdetails.cda.utils.test.CdaTestHelper.SimpleTableModel;

import java.math.BigDecimal;
import java.util.LinkedList;

public class SqlListIT extends CdaTestCase {
  //  private static final Log logger = LogFactory.getLog(SqlTest.class);

  public SqlListIT() {
    super();
  }

  public SqlListIT( final String name ) {
    super( name );
  }

  public void testStringArrayParameter() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-list.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    TableModelChecker checker = new TableModelChecker();
    checker.setDoubleComparison( 2, "1e-8" );
    checker.setBigDecimalComparison( 3, "1e-14" );
    final SimpleTableModel expected =  new SimpleTableModel(
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
}
