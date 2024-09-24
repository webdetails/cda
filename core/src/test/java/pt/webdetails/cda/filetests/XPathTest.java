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
import org.pentaho.reporting.engine.classic.core.modules.misc.tablemodel.SubSetTableModel;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.test.util.TableModelChecker;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;

import static org.junit.Assert.*;

public class XPathTest {

  @BeforeClass
  public static void init() {
    CdaEngine.init( new CdaTestEnvironment( new CdaTestingContentAccessFactory() ) );
  }

  @Test
  public void testQuery() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-xpath.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "0" );

    final TableModel result = doQuery( cdaSettings, queryOptions );

    TableModelChecker checker = new TableModelChecker();
    checker.assertColumnNames( result, "CUSTOMERS_CUSTOMERNUMBER", "CUSTOMERS_CUSTOMERNAME" );
    checker.assertColumnClasses( false, result, Integer.class, String.class );
    assertEquals( "row count", 122, result.getRowCount() );
    checker.setCheckRowCount( false );
    TableModel expected = new SimpleTableModel(
      new Object[] { 103, "Atelier graphique" },
      new Object[] { 112, "Signal Gift Stores" },
      new Object[] { 114, "Australian Collectors, Co." }
    );
    checker.assertEquals( expected, new SubSetTableModel( 0, 2, result ) );

    expected = new SimpleTableModel(
      new Object[] { 256, "Auto Associés & Cie." },
      new Object[] { 259, "Toms Spezialitäten, Ltd" }
    );
    checker.assertEquals( expected, new SubSetTableModel( 51, 52, result ) );

    expected = new SimpleTableModel(
      new Object[] { 489, "Double Decker Gift Stores, Ltd" },
      new Object[] { 495, "Diecast Collectables" },
      new Object[] { 496, "Kelly's Gift Shop" }
    );
    checker.assertEquals( expected, new SubSetTableModel( 119, 121, result ) );
  }

  @Test
  public void testXPathQueryCda15() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "xPath_CDA_15.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "xPath_CDA_15" );

    queryOptions.setParameter( "theme", "Engagement" );
    queryOptions.setParameter( "level", "2" );

    TableModel tm = doQuery( cdaSettings, queryOptions );

    assertEquals( "Commentaire pour le theme Engagement et le niveau 2", tm.getValueAt( 0, 0 ) );
  }

  protected TableModel doQuery( CdaSettings cdaSettings, QueryOptions queryOptions ) throws Exception {
    return CdaEngine.getInstance().doQuery( cdaSettings, queryOptions );
  }

  protected CdaSettings parseSettingsFile( String cdaSettingsId ) throws Exception {
    return CdaEngine.getInstance().getSettingsManager().parseSettingsFile( cdaSettingsId );
  }
}
