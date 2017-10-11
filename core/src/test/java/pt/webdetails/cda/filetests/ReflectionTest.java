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
