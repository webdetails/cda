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

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.TableModelChecker;

public class KettleTest extends CdaTestCase {

  public void testSampleKettle() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-kettle.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "myRadius", "10" );
    queryOptions.addParameter( "ZipCode", "90210" );

    TableModel result = doQuery( cdaSettings, queryOptions );
    TableModelChecker checker = new TableModelChecker( true, true );
    TypedTableModel expect =
      new TypedTableModel( new String[] { "radius", "zip" }, new Class<?>[] { Double.class, String.class } );
    expect.addRow( 10d, "90210" );
    checker.assertEquals( expect, result );
  }

  public void testKettleStringArray() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-kettle-ParamArray.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );

    queryOptions.setParameter( "countries", "Portugal;Germany" );
    queryOptions.setParameter( "Costumers", "307;369" );

    TableModel tm = doQuery( cdaSettings, queryOptions );
    assertEquals( 2, tm.getRowCount() );
    assertEquals( "307", tm.getValueAt( 0, 0 ).toString() );
    assertEquals( "Der Hund Imports", tm.getValueAt( 0, 1 ) );
  }

}
