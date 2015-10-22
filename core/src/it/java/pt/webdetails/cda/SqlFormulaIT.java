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

import java.math.BigDecimal;
import java.util.Calendar;

import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.test.CdaTestCase;
import pt.webdetails.cda.utils.test.TableModelChecker;

public class SqlFormulaIT extends CdaTestCase {

  public SqlFormulaIT() {
    super();
  }

  public SqlFormulaIT( final String name ) {
    super( name );
  }


  public void testFormulaCacheSql() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-formula.cda" );

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
    // TODO check for cache access
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    Calendar cal = Calendar.getInstance();
    queryOptions.addParameter( "orderDate", "${DATE(" + cal.get( Calendar.YEAR ) + ";"
        + ( cal.get( Calendar.MONTH ) + 1 ) + ";" + cal.get( Calendar.DAY_OF_MONTH ) + ")}" );
    TableModel result = engine.doQuery( cdaSettings, queryOptions );

    final TypedTableModel expected = new TypedTableModel(
        new String[] { "STATUS", "Year", "PRICE", "PriceInK" } );
    expected.addRow( "Shipped", 2003L, 3573701.2500000014, new BigDecimal( "3.5737012500000014" ) );
    expected.addRow( "Shipped", 2004L, 4750205.889999998,  new BigDecimal( "4.750205889999998" ) );
    expected.addRow( "Shipped", 2005L, 1513074.4600000002, new BigDecimal( "1.51307446" ) );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.setDoubleComparison( 2, 1e-8 );
    checker.setBigDecimalComparison( 3, "1e-14" );
    checker.assertEquals( expected, result );
  }

}
