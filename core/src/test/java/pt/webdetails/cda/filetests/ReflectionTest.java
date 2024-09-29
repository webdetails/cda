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


package pt.webdetails.cda.filetests;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.TableModelChecker;

import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;


public class ReflectionTest extends CdaTestCase {

  public void testSqlQuery() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-reflection.cda" );

    final CdaEngine engine = getEngine();

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "0" );

    TableModel result = engine.doQuery( cdaSettings, queryOptions );
    TypedTableModel expected =
      new TypedTableModel(
        new String[] { "ID", "NUMBER", "DESCRIPTION" },
        new Class<?>[] { Long.class, Long.class, String.class }, 2 );
    expected.addRow( 0L, 0L, "Look, you got a new dataset." );
    expected.addRow( 0L, 1L, "So Subreport queries work too.." );
    // FIXME reports wrong type for second column: String instead of Long
    TableModelChecker checker = new TableModelChecker( false, true );
    checker.assertEquals( expected, result );
  }

}
