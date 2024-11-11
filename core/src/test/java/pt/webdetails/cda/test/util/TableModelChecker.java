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


package pt.webdetails.cda.test.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.table.TableModel;

import org.junit.Assert;
import org.junit.ComparisonFailure;

/**
 * Utility to check {@link TableModel} instances for data equality. Can specify different comparisons per column.
 */
public class TableModelChecker {
  public static interface Comparison extends CdaTestHelper.Comparison<Object> {
  }

  private Map<Integer, Comparison> comparators = new HashMap<>();
  private boolean checkColumnCount = true;
  private boolean checkRowCount = true;
  private boolean checkColumnNames = false;
  private boolean checkColumnClasses = false;

  private static final Comparison defaultComp = new Comparison() {
    public boolean equal( Object one, Object two ) {
      return Objects.equals( one, two );
    }
  };

  public TableModelChecker() {
  }

  public TableModelChecker( boolean checkColumnClasses, boolean checkColumnNames ) {
    this.checkColumnNames = checkColumnNames;
    this.checkColumnClasses = checkColumnClasses;
  }

  public void setCheckColumnNames( boolean checkColumnNames ) {
    this.checkColumnNames = checkColumnNames;
  }

  public void setCheckColumnClasses( boolean checkColumnClasses ) {
    this.checkColumnClasses = checkColumnClasses;
  }

  public void setCheckColumnCount( boolean enable ) {
    this.checkColumnCount = enable;
  }

  public void setCheckRowCount( boolean enable ) {
    this.checkRowCount = enable;
  }


  public void setComparison( int columnNbr, Comparison comparison ) {
    comparators.put( columnNbr, comparison );
  }

  public void setDoubleComparison( int columnNbr, String delta ) {
    setDoubleComparison( columnNbr, Double.parseDouble( delta ) );
  }

  public void setDoubleComparison( int columnNbr, final double delta ) {
    comparators.put( columnNbr, new Comparison() {
      @Override
      public boolean equal( Object one, Object two ) {
        return CdaTestHelper.numericEquals( (double) one, (double) two, delta );
      }
    } );
  }

  public void setBigDecimalComparison( int columnNbr ) {
    comparators.put( columnNbr, new Comparison() {
      @Override
      public boolean equal( Object one, Object two ) {
        return Objects.equals( one, two )
          || one != null
          && two != null
          && ( (BigDecimal) one ).compareTo( (BigDecimal) two ) == 0;
      }
    } );
  }

  public void setBigDecimalComparison( int columnNbr, String deltaExp ) {
    final BigDecimal delta = new BigDecimal( deltaExp );
    comparators.put( columnNbr, new Comparison() {
      @Override
      public boolean equal( Object one, Object two ) {
        return CdaTestHelper.numericEquals( (BigDecimal) one, (BigDecimal) two, delta );
      }
    } );
  }

  public void dontCompare( int columnNbr ) {
    comparators.put( columnNbr, null );
  }

  public void setDefaultComparison( int columnNbr ) {
    comparators.remove( columnNbr );
  }

  /**
   * Checks if cells match. Will use default equals unless custom comparisons are set.<br> Does not check names or
   * types.
   *
   * @param expected
   * @param actual
   * @see {@link #assertColumnNames}
   * @see {@link #assertColumnClasses}
   */
  public void assertEquals( TableModel expected, TableModel actual ) {
    if ( checkColumnCount ) {
      Assert.assertEquals( "number of columns", expected.getColumnCount(), actual.getColumnCount() );
    }
    if ( checkColumnClasses ) {
      for ( int i = 0; i < expected.getColumnCount(); i++ ) {
        assertClassEquals( true, i, expected.getColumnClass( i ), actual.getColumnClass( i ) );
      }
    }
    if ( checkColumnNames ) {
      for ( int i = 0; i < expected.getColumnCount(); i++ ) {
        assertColumnNameEquals( i, expected.getColumnName( i ), actual.getColumnName( i ) );
      }
    }
    for ( int col = 0; col < expected.getColumnCount(); col++ ) {
      Comparison comp = defaultComp;
      if ( comparators.containsKey( col ) ) {
        comp = comparators.get( col );
      }
      if ( comp != null ) {
        for ( int row = 0; row < expected.getRowCount(); row++ ) {
          Object expectedVal = expected.getValueAt( row, col );
          Object actualVal = actual.getValueAt( row, col );
          try {
            if ( !comp.equal( expectedVal, actualVal ) ) {
              throw new ComparisonFailure( String.format( "Mismatch at row %d, column %d.", row, col ),
                String.format( "%s (%s)", Objects.toString( expectedVal ), getClassDesc( expectedVal ) ),
                String.format( "%s (%s)", Objects.toString( actualVal ), getClassDesc( actualVal ) ) );
            }
          } catch ( ClassCastException e ) {
            throw new AssertionError( String.format( "At row %d, column %d: %s", row, col, e.getMessage() ), e );
          }
        }
      }
    }
    if ( checkRowCount ) {
      Assert.assertEquals( "number of rows", expected.getRowCount(), actual.getRowCount() );
    }
  }

  /**
   * checks if the columns names match
   */
  public void assertColumnNames( TableModel table, String... names ) {
    for ( int i = 0; i < names.length; i++ ) {
      assertColumnNameEquals( i, names[ i ], table.getColumnName( i ) );
    }
  }

  private void assertColumnNameEquals( int columnIdx, String expectedColumn, String actualColumn )
    throws ComparisonFailure {
    if ( !Objects.equals( expectedColumn, actualColumn ) ) {
      throw new ComparisonFailure( String.format( "wrong name for column %d", columnIdx ),
        expectedColumn, actualColumn );
    }
  }

  /**
   * checks if the columns names match
   */
  public void assertColumnClasses( boolean allowSubclasses, TableModel table, Class<?>... classes ) {
    for ( int i = 0; i < classes.length; i++ ) {
      Class<?> expected = classes[ i ];
      Class<?> actual = table.getColumnClass( i );
      assertClassEquals( allowSubclasses, i, expected, actual );
    }
  }

  public void assertColumnClasses( TableModel result, Class<?>... classes ) {
    assertColumnClasses( false, result, classes );
  }

  private void assertClassEquals( boolean allowSubclasses, int columnIdx, Class<?> expected, Class<?> actual )
    throws ComparisonFailure {
    if ( !( allowSubclasses && expected.isAssignableFrom( actual ) || Objects.equals( expected, actual ) ) ) {
      throw new ComparisonFailure( String.format( "wrong class for column %d", columnIdx ),
        expected.getName(), actual.getName() );
    }
  }

  private static final String getClassDesc( Object obj ) {
    return obj != null ? obj.getClass().getSimpleName() : "";
  }

}
