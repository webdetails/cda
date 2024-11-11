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

import java.math.BigDecimal;

import javax.swing.table.TableModel;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.util.Assert;
import org.pentaho.metadata.model.concept.types.JoinType;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.JoinCompoundDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cda.test.util.TableModelChecker;

import static org.junit.Assert.*;

public class CompoundQueryTest {

  @BeforeClass
  public static void init() {
    CdaEngine.init( new CdaTestEnvironment( new CdaTestingContentAccessFactory() ) );
  }

  @Test
  public void testCompoundJoin() throws Exception {

    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-join.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "3" );

    TableModelChecker checker = new TableModelChecker();
    checker.setDoubleComparison( 2, 1e-8 );
    checker.setDoubleComparison( 3, 1e-8 );
    checker.setBigDecimalComparison( 4, "1e-8" );
    TableModel expected = new SimpleTableModel(
      new Object[] { 2003L, "Cancelled", 75132.15999999999, 225396.48000000007, new BigDecimal( 150264.32000000008 ) },
      new Object[] { 2003L, "Resolved", 28550.59, 85651.76999999999, new BigDecimal( 57101.17999999999 ) },
      new Object[] { 2003L, "Shipped", 3573701.2500000014, 1.0721103750000002E7, new BigDecimal( 7147402.5000000006 ) },
      new Object[] { 2004L, "Cancelled", 187195.13000000003, 561585.3900000001, new BigDecimal( 374390.26000000007 ) },
      new Object[] { 2004L, "On Hold", 26260.210000000003, 78780.62999999999, new BigDecimal( 52520.419999999987 ) },
      new Object[] { 2004L, "Resolved", 24078.610000000004, 72235.82999999999, new BigDecimal( 48157.219999999986 ) },
      new Object[] { 2004L, "Shipped", 4750205.889999998, 1.4250617669999992E7, new BigDecimal( 9500411.779999994 ) },
      new Object[] { 2005L, "Disputed", 72212.86, 216638.58, new BigDecimal( 144425.72 ) },
      new Object[] { 2005L, "In Process", 144729.96000000002, 434189.87999999995,
        new BigDecimal( 289459.91999999993 ) },
      new Object[] { 2005L, "On Hold", 152718.97999999995, 458156.94, new BigDecimal( 305437.96000000005 ) },
      new Object[] { 2005L, "Resolved", 98089.08000000002, 294267.24, new BigDecimal( 196178.15999999998 ) },
      new Object[] { 2005L, "Shipped", 1513074.4600000002, 4539223.38, new BigDecimal( 3026148.9199999998 ) } );
    TableModel result = getEngine().doQuery( cdaSettings, queryOptions );
    checker
      .assertColumnClasses( false, result, Long.class, String.class, Double.class, Double.class, BigDecimal.class );
    checker.assertEquals( expected, result );
  }

  @Test
  public void testCompoundJoinCaseSensitive() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-join.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "6" );
    TableModel result = getEngine().doQuery( cdaSettings, queryOptions );

    TableModelChecker checker = new TableModelChecker();

    TableModel expected = new SimpleTableModel(
      new Object[] { "2003", "Shipped", "2003", "Shipped" },
      new Object[] { null,   null,      "2004", "SHIPPED" },
      new Object[] { "2004", "Shipped", null,   null },
      new Object[] { "2005", "Shipped", null,   null },
      new Object[] { null,   null,      "2005", "shipped" }
    );

    checker.assertEquals( expected, result );
  }

  @Test
  public void testCDA43_CreationWithNullJoinType() throws Exception {
    CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-join-null-jointype.cda" );
    JoinCompoundDataAccess jcda = (JoinCompoundDataAccess) cdaSettings.getDataAccess( "3" );

    Assert.assertTrue( jcda.getJoinType() == JoinType.FULL_OUTER );
  }

  @Test
  public void testCompoundUnion() throws Exception {

    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-union.cda" );

    QueryOptions queryOptions = new QueryOptions();

    TableModelChecker checker = new TableModelChecker();
    checker.setDoubleComparison( 1, 1e-8 );
    checker.setBigDecimalComparison( 2, "1e-14" );

    queryOptions.setDataAccessId( "1" );
    final TableModel top = getEngine().doQuery( cdaSettings, queryOptions );
    SimpleTableModel expected = new SimpleTableModel( new Object[] { 2003L, 3677.384 } );
    checker.assertEquals( expected, top );

    queryOptions.setDataAccessId( "2" );
    final TableModel bottom = getEngine().doQuery( cdaSettings, queryOptions );
    expected = new SimpleTableModel(
      new Object[] { 2005L, 3961.65068, new BigDecimal( "0.0039616506800000034" ) },
      new Object[] { 2004L, 9975.47968, new BigDecimal( "0.009975479679999995" ) },
      new Object[] { 2003L, 7354.768, new BigDecimal( "0.007354768000000003" ) } );
    checker.assertEquals( expected, bottom );

    queryOptions.setDataAccessId( "3" );
    queryOptions.setOutputType( "json" );

    final TableModel union = getEngine().doQuery( cdaSettings, queryOptions );
    expected = new SimpleTableModel(
      new Object[] { 2003L, 3677.384, null },
      new Object[] { 2005L, 3961.65068, new BigDecimal( "0.0039616506800000034" ) },
      new Object[] { 2004L, 9975.47968, new BigDecimal( "0.009975479679999995" ) },
      new Object[] { 2003L, 7354.768, new BigDecimal( "0.007354768000000003" ) } );
    checker.assertEquals( expected, union );
    assertEquals( union.getColumnCount(), Math.max( top.getColumnCount(), bottom.getColumnCount() ) );
    assertEquals( union.getRowCount(), top.getRowCount() + bottom.getRowCount() );
  }


  private SettingsManager getSettingsManager() {
    return getEngine().getSettingsManager();
  }

  private CdaEngine getEngine() {
    return CdaEngine.getInstance();
  }
}
