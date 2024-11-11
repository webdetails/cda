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

import javax.swing.table.TableModel;

import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.TableModelChecker;

public class ScriptingTest extends CdaTestCase {

  @Test
  public void testSqlQuery() throws Exception {

    // Define an outputStream
    final CdaSettings cdaSettings = parseSettingsFile( "sample-scripting.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "Shipped" );

    TypedTableModel expected = new TypedTableModel( new String[] { "Region", "Q1", "Q2", "Q3", "Q4" },
      new Class<?>[] { String.class, Integer.class, Integer.class, Integer.class, Integer.class } );
    expected.addRow( "East", 10, 10, 14, 21 );
    expected.addRow( "West", 14, 34, 10, 12 );
    expected.addRow( "South", 10, 11, 14, 15 );
    expected.addRow( "Shipped", 10, 11, 14, 15 );
    TableModel result = doQuery( cdaSettings, queryOptions );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.assertEquals( expected, result );
  }

  @Test
  public void testJsonQuery() throws Exception {

    // Define an outputStream
    final CdaSettings cdaSettings = parseSettingsFile( "sample-json-scripting.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );

    TableModel result = doQuery( cdaSettings, queryOptions );
    TypedTableModel expected = new TypedTableModel( new String[] { "Year-Quarter", "Value" },
      new Class<?>[] { String.class, Double.class } );
    expected.addRow( "2006 Q1", 242.0d );
    expected.addRow( "2006 Q2", 410.0d );
    expected.addRow( "2006 Q3", 340.0d );
    expected.addRow( "2006 Q4", 353.0d );
    expected.addRow( "2007 Q1", null );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.assertEquals( expected, result );
  }

}
