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

import static pt.webdetails.cda.test.util.CdaTestHelper.assertJsonEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.MaturityLevel;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.DenormalizedMdxDataAccess;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;
import pt.webdetails.cda.test.util.TableModelChecker;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;


public class MdxJdbcTest extends CdaTestCase {

  private static final Class<?>[] customDataFactories = {
    CompactBandedMDXDataFactory.class, ExtBandedMDXDataFactory.class, ExtDenormalizedMDXDataFactory.class };

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerCustomDataFactories();
  }

  public void testMdxJdbcITQuery() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-mondrian.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );
    Class<?>[] classes = new Class[ 11 ];
    Arrays.fill( classes, Double.class );
    classes[ 10 ] = BigDecimal.class;
    TypedTableModel expected =
      new TypedTableModel( new String[] { "Year", "price", "[Time].[All Years].[2003].[QTR3]",
        "[Time].[All Years].[2003].[QTR4]", "[Time].[All Years].[2004].[QTR1]", "[Time].[All Years].[2004].[QTR2]",
        "[Time].[All Years].[2004].[QTR3]", "[Time].[All Years].[2004].[QTR4]", "[Time].[All Years].[2005].[QTR1]",
        "[Time].[All Years].[2005].[QTR2]", "PriceInK" }, classes );
    TableModel tm = doQuery( cdaSettings, queryOptions );
    TableModelChecker checker = new TableModelChecker( true, true );
    for ( int i = 0; i < 10; i++ ) {
      checker.setDoubleComparison( i, 1e-8 );
    }
    checker.setBigDecimalComparison( 10, "1e-8" );
    expected.addRow( 445094.69d, 564842.02d, 687268.8699999998d, 1876495.6699999985d, 877418.9699999997d,
      660518.8399999997d, 1145308.08d, 2066959.999999998d, 1013171.0199999999d, 499903.43999999977d,
      new BigDecimal( "0.56484202" ) );
    checker.assertEquals( expected, tm );
  }

  public void testMondrianCompactQueries() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-mondrian-compact.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );

    // 1
    queryOptions.setDataAccessId( "1" );
    TableModel result = doQuery( cdaSettings, queryOptions );
    TypedTableModel expected1 = new TypedTableModel(
      new String[] { "Time", "Sales", "Quantity" },
      new Class<?>[] { String.class, Double.class, Double.class } );
    expected1.addRow( "2003", 3573701.2500000023d, 35313.0 );
    expected1.addRow( "QTR1", 445094.69d, 4561.0 );
    expected1.addRow( "QTR2", 564842.02d, 5695.0 );
    expected1.addRow( "QTR3", 687268.8699999998d, 6629.0 );
    expected1.addRow( "QTR4", 1876495.6699999985d, 18428.0 );
    expected1.addRow( "2004", 4750205.889999998d, 47151.0 );
    expected1.addRow( "QTR1", 877418.9699999997d, 8694.0 );
    expected1.addRow( "QTR2", 660518.8399999997d, 6647.0 );
    expected1.addRow( "QTR3", 1145308.08d, 11311.0 );
    expected1.addRow( "QTR4", 2066959.999999998d, 20499.0 );
    expected1.addRow( "2005", 1513074.4600000002d, 14607.0 );
    expected1.addRow( "QTR1", 1013171.0199999999d, 9876.0 );
    expected1.addRow( "QTR2", 499903.43999999977d, 4731.0 );
    TableModelChecker checker = new TableModelChecker( true, true );
    checker.setDoubleComparison( 1, 1e-8 );
    checker.setDoubleComparison( 2, 1e-8 );
    checker.assertEquals( expected1, result );

    // 2
    queryOptions.setDataAccessId( "2" );
    result = doQuery( cdaSettings, queryOptions );
    for ( int i = 1; i < result.getColumnCount(); i++ ) {
      checker.setDoubleComparison( i, 1e-8 );
    }
    Class<?>[] classes2 = new Class[ 14 ];
    Arrays.fill( classes2, Double.class );
    classes2[ 0 ] = String.class;
    String[] names2 = new String[ 14 ];
    names2[ 0 ] = "Measures";
    for ( int i = 0; i < expected1.getRowCount(); i++ ) {
      names2[ i + 1 ] = (String) expected1.getValueAt( i, 0 );
    }
    // flip expected1
    TypedTableModel expected2 = new TypedTableModel( names2, classes2 );
    Object[] row1 = new Object[ 14 ];
    row1[ 0 ] = "Sales";
    for ( int i = 0; i < expected1.getRowCount(); i++ ) {
      row1[ i + 1 ] = expected1.getValueAt( i, 1 );
    }
    expected2.addRow( row1 );
    Object[] row2 = new Object[ 14 ];
    row2[ 0 ] = "Quantity";
    for ( int i = 0; i < expected1.getRowCount(); i++ ) {
      row2[ i + 1 ] = expected1.getValueAt( i, 2 );
    }
    expected2.addRow( row2 );
    checker.assertEquals( expected2, result );

    // 3...
    TypedTableModel expected3 =
      new TypedTableModel( new String[] { "Product", "Time", "Sales", "Quantity" },
        new Class<?>[] { String.class, String.class, Double.class, Double.class } );
    for ( int i = 0; i < expected1.getRowCount(); i++ ) {
      Object[] row = new Object[ 4 ];
      row[ 0 ] = "All Products";
      for ( int j = 0; j < expected1.getColumnCount(); j++ ) {
        row[ j + 1 ] = expected1.getValueAt( i, j );
      }
      expected3.addRow( row );
    }
    checker.setDefaultComparison( 1 );
    queryOptions.setDataAccessId( "3" );
    result = doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected3, result );

    // 4!
    queryOptions.setDataAccessId( "4" );
    result = doQuery( cdaSettings, queryOptions );
    for ( int i = 1; i < expected2.getColumnCount(); i++ ) {
      expected2.setColumnName( i, "All Products/" + expected2.getColumnName( i ) );
    }
    checker.setDoubleComparison( 1, 1e-8 );
    checker.assertEquals( expected2, result );
  }

  public void testMdxExceptionHandling() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-mondrian-compact.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipperyship" );

    queryOptions.setDataAccessId( "1" );
    try {
      doQuery( cdaSettings, queryOptions );
      Assert.fail( "no exception" );
    } catch ( Exception e ) {
      String msg = ExceptionUtils.getRootCauseMessage( e.getCause() );
      Assert.assertEquals(
        "MondrianException: Mondrian Error:MDX object '[Order Status].[Shipperyship]' not found in cube "
          + "'SteelWheelsSales'", msg );
    }
  }

  public void testInterfaceNoBandedMode() {
    boolean bandedFound = false;
    DenormalizedMdxDataAccess dmda = new DenormalizedMdxDataAccess();
    List<PropertyDescriptor> properties = dmda.getInterface();
    for ( PropertyDescriptor pd : properties ) {
      if ( pd.getName().equals( "bandedMode" ) ) {
        bandedFound = true;
      }
    }

    Assert.assertFalse( bandedFound );
  }

  public void testSortMdxQuery() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-mondrian-compact.cda" );
    CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );

    TableModelChecker checker = new TableModelChecker();
    checker.setDoubleComparison( 1, 1e-8 );
    checker.setDoubleComparison( 2, 1e-8 );

    queryOptions.setDataAccessId( "1" );
    queryOptions.setSortBy( Arrays.asList( new String[] { "0D", "1A" } ) );
    TableModel table = engine.doQuery( cdaSettings, queryOptions );
    TableModel expected = new SimpleTableModel(
      new Object[] { "QTR4", 1876495.6699999985, 18428.0 },
      new Object[] { "QTR4", 2066959.999999998, 20499.0 },
      new Object[] { "QTR3", 687268.8699999998, 6629.0 },
      new Object[] { "QTR3", 1145308.08, 11311.0 },
      new Object[] { "QTR2", 499903.43999999977, 4731.0 },
      new Object[] { "QTR2", 564842.02, 5695.0 },
      new Object[] { "QTR2", 660518.8399999997, 6647.0 },
      new Object[] { "QTR1", 445094.69, 4561.0 },
      new Object[] { "QTR1", 877418.9699999997, 8694.0 },
      new Object[] { "QTR1", 1013171.0199999999, 9876.0 },
      new Object[] { "2005", 1513074.4600000002, 14607.0 },
      new Object[] { "2004", 4750205.889999998, 47151.0 },
      new Object[] { "2003", 3573701.2500000023, 35313.0 } );
    checker.assertEquals( expected, table );

    queryOptions.setSortBy( Arrays.asList( new String[] {} ) );
    expected = new SimpleTableModel(
      new Object[] { "2003", 3573701.2500000023, 35313.0 },
      new Object[] { "QTR1", 445094.69, 4561.0 },
      new Object[] { "QTR2", 564842.02, 5695.0 },
      new Object[] { "QTR3", 687268.8699999998, 6629.0 },
      new Object[] { "QTR4", 1876495.6699999985, 18428.0 },
      new Object[] { "2004", 4750205.889999998, 47151.0 },
      new Object[] { "QTR1", 877418.9699999997, 8694.0 },
      new Object[] { "QTR2", 660518.8399999997, 6647.0 },
      new Object[] { "QTR3", 1145308.08, 11311.0 },
      new Object[] { "QTR4", 2066959.999999998, 20499.0 },
      new Object[] { "2005", 1513074.4600000002, 14607.0 },
      new Object[] { "QTR1", 1013171.0199999999, 9876.0 },
      new Object[] { "QTR2", 499903.43999999977, 4731.0 } );
    table = engine.doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected, table );

    queryOptions.setSortBy( Arrays.asList( new String[] { "0D", "2", "1A" } ) );
    expected = new SimpleTableModel(
      new Object[] { "QTR4", 1876495.6699999985, 18428.0 },
      new Object[] { "QTR4", 2066959.999999998, 20499.0 },
      new Object[] { "QTR3", 687268.8699999998, 6629.0 },
      new Object[] { "QTR3", 1145308.08, 11311.0 },
      new Object[] { "QTR2", 499903.43999999977, 4731.0 },
      new Object[] { "QTR2", 564842.02, 5695.0 },
      new Object[] { "QTR2", 660518.8399999997, 6647.0 },
      new Object[] { "QTR1", 445094.69, 4561.0 },
      new Object[] { "QTR1", 877418.9699999997, 8694.0 },
      new Object[] { "QTR1", 1013171.0199999999, 9876.0 },
      new Object[] { "2005", 1513074.4600000002, 14607.0 },
      new Object[] { "2004", 4750205.889999998, 47151.0 },
      new Object[] { "2003", 3573701.2500000023, 35313.0 } );
    table = engine.doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected, table );

    queryOptions.setSortBy( Arrays.asList( new String[] { "1A" } ) );
    expected = new SimpleTableModel(
      new Object[] { "QTR1", 445094.69, 4561.0 },
      new Object[] { "QTR2", 499903.43999999977, 4731.0 },
      new Object[] { "QTR2", 564842.02, 5695.0 },
      new Object[] { "QTR2", 660518.8399999997, 6647.0 },
      new Object[] { "QTR3", 687268.8699999998, 6629.0 },
      new Object[] { "QTR1", 877418.9699999997, 8694.0 },
      new Object[] { "QTR1", 1013171.0199999999, 9876.0 },
      new Object[] { "QTR3", 1145308.08, 11311.0 },
      new Object[] { "2005", 1513074.4600000002, 14607.0 },
      new Object[] { "QTR4", 1876495.6699999985, 18428.0 },
      new Object[] { "QTR4", 2066959.999999998, 20499.0 },
      new Object[] { "2003", 3573701.2500000023, 35313.0 },
      new Object[] { "2004", 4750205.889999998, 47151.0 } );
    table = engine.doQuery( cdaSettings, queryOptions );
    checker.assertEquals( expected, table );

  }

  public void testCsvExport() throws Exception {
    String expectedOutput = "\"[Measures].[MeasuresLevel]\";Year;price" + System.lineSeparator()
      + "\"Sales\";\"445094.69\";\"564842.02\"" + System.lineSeparator();

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "Shipped" );

    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "csv" );
    String csv = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( csv ) );
    assertEquals( expectedOutput, csv );
  }

  public void testJsonExport() throws Exception {
    String expectedOutput = "{\"queryInfo\":{\"totalRows\":\"1\"},"
      + "\"resultset\":[[\"Sales\",445094.69,564842.02]],\""
      + "metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"[Measures].[MeasuresLevel]\"},"
      + "{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"Year\"},"
      + "{\"colIndex\":2,\"colType\":\"Numeric\",\"colName\":\"price\"}]}";

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "Shipped" );

    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "json" );
    String json = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( json ) );
    assertJsonEquals( "", expectedOutput, json );
  }

  public void testJsonExportNaNValues() throws Exception {
    String expectedOutput = "{\"queryInfo\":{\"totalRows\":\"1\"},"
      + "\"resultset\":[[\"All Markets\",null]],"
      + "\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"[Markets].[(All)]\"},"
      + "{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"[Measures].[Invalid]\"}]}";

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );

    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "json" );
    String json = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( json ) );
    assertJsonEquals( "", expectedOutput, json );
  }

  protected static void registerCustomDataFactories() {
    for ( Class<?> clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd =
        new DefaultDataFactoryMetaData(
          clazz.getName(), "", "", true, false, true, false, false, false, false,
          MaturityLevel.Production, new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }
}
