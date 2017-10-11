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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.test.util.TableModelChecker;

import static pt.webdetails.cda.test.util.CdaTestHelper.*;

public class DiscoveryTest {
  private static SettingsManager settingsManager;
  private static CdaSettings discoverySettings;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    initBareEngine( new CdaTestEnvironment( new CdaTestingContentAccessFactory() ) );
    settingsManager = new SettingsManager();
    discoverySettings = settingsManager.parseSettingsFile( "sample-discovery.cda" );
  }

  @Test
  public void testListQueries() throws Exception {
    TableModel listQueries = discoverySettings.listQueries();

    TypedTableModel expected = new TypedTableModel(
      new String[] { "id", "name", "type" },
      new Class<?>[] { String.class, String.class, String.class } );
    expected.addRow( "1", "Sample sql query on sampledata", "sql" );
    expected.addRow( "2", "Sample query on SteelWheelsSales", "mdx" );
    // third is hidden, not shown
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.assertEquals( expected, listQueries );
  }

  @Test
  public void testListParameters() throws Exception {
    DataAccess dataAccess = discoverySettings.getDataAccess( "1" );
    TableModel listParameters = dataAccess.listParameters();
    TableModelChecker checker = new TableModelChecker();
    checker.assertColumnNames( listParameters, "name", "type", "defaultValue", "pattern", "access" );
    checker.assertEquals( new SimpleTableModel(
        new Object[] { "status", "String", "Shipped", null, "public" },
        new Object[] { "orderDate", "Date", "2003-03-01", "yyyy-MM-dd", "public" } ),
      listParameters );
  }

}
