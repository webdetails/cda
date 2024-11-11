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


package pt.webdetails.cda.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.MdxDataAccess;
import pt.webdetails.cda.dataaccess.DataAccess.OutputMode;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.test.util.TableModelChecker;

import static pt.webdetails.cda.test.util.CdaTestHelper.*;
import static org.mockito.Mockito.*;

import javax.swing.table.TableModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TableModelUtilsTest {

  @BeforeClass
  public static void init() {
    initBareEngine( getMockEnvironment() );
  }

  @Test
  public void testDataAccessMapToTableModel() {

    HashMap<String, DataAccess> dams = new LinkedHashMap<String, DataAccess>( 4 );
    dams.put( "air", new MdxDataAccess( "air", "", "", "" ) );
    dams.put( "earth", new MdxDataAccess( "earth", "", "", "" ) );
    dams.put( "fire", new MdxDataAccess( "fire", "", "", "" ) );
    dams.put( "water", new MdxDataAccess( "water", "", "", "" ) );

    HashMap<String, DataAccess> damns = new LinkedHashMap<String, DataAccess>( 4 );
    damns.put( "water", new MdxDataAccess( "water", "", "", "" ) );
    damns.put( "air", new MdxDataAccess( "air", "", "", "" ) );
    damns.put( "fire", new MdxDataAccess( "fire", "", "", "" ) );
    damns.put( "earth", new MdxDataAccess( "earth", "", "", "" ) );

    TableModelUtilsForTest tmuForTest = new TableModelUtilsForTest( 4 );
    TableModel testControl = tmuForTest.createSorted( dams );
    TableModel result = TableModelUtils.dataAccessMapToTableModel( damns );

    for ( int i = 0; i < 2; i++ ) {
      Assert.assertEquals( testControl.getValueAt( i, 0 ), result.getValueAt( i, 0 ) );
    }
  }

  @Test
  public void testCopyTableModel() {
    SimpleTableModel table = new SimpleTableModel(
      new Object[] { 1, "2", null },
      new Object[] { null, "", 3 } );
    DataAccess daColDef = mock( DataAccess.class );
    final String[] names = { "X1", "nope", "X3" };
    when( daColDef.getColumnDefinition( anyInt() ) ).thenAnswer( new Answer<ColumnDefinition>() {
      public ColumnDefinition answer( InvocationOnMock invocation ) throws Throwable {
        final int i = (int) invocation.getArguments()[ 0 ];
        return i == 1 ? null : new ColumnDefinition() {
          public String getName() {
            return names[ i ];
          }

          ;
        };
      }
    } );
    TableModelChecker checker = new TableModelChecker( true, false );
    TableModel copy = TableModelUtils.copyTableModel( daColDef, table );
    checker.assertEquals( table, copy );
    checker.assertColumnNames( copy, names[ 0 ], table.getColumnName( 1 ), names[ 2 ] );
  }

  @Test
  public void testAppendTableModel() throws Exception {
    TableModel table1 = new SimpleTableModel(
      new Object[] { 1, 2, 3 },
      new Object[] { 4, 5, 6 } );
    TableModel table2 = new SimpleTableModel(
      new Object[] { 7, 8 },
      new Object[] { null, 9 } );
    TableModel appended = TableModelUtils.appendTableModel( table1, table2 );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( new SimpleTableModel(
      new Object[] { 1, 2, 3 },
      new Object[] { 4, 5, 6 },
      new Object[] { 7, 8, null },
      new Object[] { null, 9, null } ), appended );
  }

  @Test
  public void testOutputIdx() throws Exception {
    TypedTableModel tm = new TypedTableModel(
      new String[] { "c1", "c2", "c3" },
      new Class<?>[] { Long.class, String.class, Double.class }, 2 );
    tm.addRow( 1L, "one", 1.0d );
    tm.addRow( 2L, "two", 2.0d );
    DataAccess dataAccess = mock( DataAccess.class );
    when( dataAccess.getType() ).thenReturn( "any type" );
    when( dataAccess.getColumnDefinitions() ).thenReturn( new ArrayList<ColumnDefinition>( 0 ) );
    when( dataAccess.getOutputs( 6 ) ).thenReturn( new ArrayList<Integer>( Arrays.asList( 2, 1 ) ) );
    QueryOptions opts = new QueryOptions();
    opts.setOutputIndexId( 6 );
    TableModel result = TableModelUtils.postProcessTableModel( dataAccess, opts, tm );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( new SimpleTableModel(
      new Object[] { 1.0d, "one" },
      new Object[] { 2.0d, "two" } ), result );
    checker.assertColumnNames( result, "c3", "c2" );
    checker.assertColumnClasses( result, Double.class, String.class );

    when( dataAccess.getOutputMode( 6 ) ).thenReturn( OutputMode.EXCLUDE );
    result = TableModelUtils.postProcessTableModel( dataAccess, opts, tm );
    checker.assertEquals(
      new SimpleTableModel( new Object[] { 1L }, new Object[] { 2L } ),
      result );
    checker.assertColumnNames( result, "c1" );
    checker.assertColumnClasses( result, Long.class );
  }
 @Test
  public void testSpecialChars() throws Exception {
    TypedTableModel tm = new TypedTableModel(
            new String[] { "c1&c2", "c2<'>c3" },
            new Class<?>[] { Long.class, String.class}, 2 );
    tm.addRow( 1L, "one");
    tm.addRow( 2L, "two");
    DataAccess dataAccess = mock( DataAccess.class );
    when( dataAccess.getType() ).thenReturn( "any type" );
    when( dataAccess.getColumnDefinitions() ).thenReturn( new ArrayList<ColumnDefinition>( 0 ) );
    when( dataAccess.getOutputs( 6 ) ).thenReturn( new ArrayList<Integer>( Arrays.asList( 2, 1 ) ) );
    QueryOptions opts = new QueryOptions();
    opts.setOutputIndexId( 2 );
    opts.setSortBy(new ArrayList<>(Arrays.asList("1D")));
    TableModel result = TableModelUtils.postProcessTableModel( dataAccess, opts, tm );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( new SimpleTableModel(
            new Object[] { 2L, "two" },
            new Object[] { 1L, "one" } ), result );
    checker.assertColumnNames( result, "c1&c2", "c2<'>c3" );
    checker.assertColumnClasses( result, Long.class, String.class );

  }

  private class TableModelUtilsForTest {

    private TypedTableModel typedTableModel;

    public TableModelUtilsForTest( int mapSize ) {
      int rowCount = mapSize;

      // Define names and types
      final String[] colNames = {
        "id", "name", "type"
      };

      final Class<?>[] colTypes = {
        String.class, String.class, String.class
      };

      typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );
    }

    protected TypedTableModel getTypedTableModel() {
      return typedTableModel;
    }

    protected TableModel createSorted( HashMap<String, DataAccess> dam ) {

      TypedTableModel model = this.getTypedTableModel();

      for ( DataAccess dataAccess : dam.values() ) {
        model.addRow( new Object[] {
          dataAccess.getId(), dataAccess.getName(), dataAccess.getType() } );
      }

      return model;
    }

  }


}
