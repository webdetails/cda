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
