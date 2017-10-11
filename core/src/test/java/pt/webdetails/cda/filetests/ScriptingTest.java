/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
