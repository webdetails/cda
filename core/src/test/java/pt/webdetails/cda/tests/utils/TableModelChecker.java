package pt.webdetails.cda.tests.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.table.TableModel;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

/**
 * Utility to check {@link TableModel} instances for data equality. Can specify different comparisons per column.
 */
public class TableModelChecker {
  public static interface Comparison extends pt.webdetails.cda.tests.utils.CdaTestHelper.Comparison<Object> {}

  private Map<Integer,Comparison> comparators = new HashMap<>();
  private boolean checkColumnCount = true;
  private boolean checkRowCount = true;

  private static final Comparison defaultComp = new Comparison () {
    public boolean equal( Object one, Object two ) {
      return Objects.equals( one, two );
    }
  };

  public void setCheckColumnCount( boolean compareColumns ) {
    this.checkColumnCount = compareColumns;
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
    });
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
    });
  }

  public void setBigDecimalComparison( int columnNbr, String deltaExp ) {
    final BigDecimal delta = new BigDecimal( deltaExp );
    comparators.put( columnNbr, new Comparison() {
      @Override
      public boolean equal( Object one, Object two ) {
        return CdaTestHelper.numericEquals( (BigDecimal) one, (BigDecimal) two, delta );
      }
    });
  }

  public void dontCompare( int columnNbr ) {
    comparators.put( columnNbr, null );
  }

  /**
   * Checks if cells match. Will use default equals unless custom comparisons are set.<br>
   * Does not check names or types.
   * @see {@link #assertColumnNames}
   * @see {@link #assertColumnClasses}
   * @param expected
   * @param actual
   */
  public void assertEquals( TableModel expected, TableModel actual ) {
    for (int col = 0; col < expected.getColumnCount(); col++ ) {
      Comparison comp  = defaultComp;
      if ( comparators.containsKey( col ) ) {
        comp = comparators.get( col );
      }
      if ( comp != null ) {
        for ( int row = 0; row < expected.getRowCount(); row++ ) {
          Object expectedVal = expected.getValueAt( row, col );
          Object actualVal = actual.getValueAt( row, col );
          if ( !comp.equal( expectedVal, actualVal ) ) {
            throw new ComparisonFailure( String.format( "Mismatch at row %d, column %d.", row, col ),
                String.format( "%s (%s)", Objects.toString( expectedVal ), getClassDesc( expectedVal ) ),
                String.format( "%s (%s)", Objects.toString( actualVal ), getClassDesc( actualVal ) ) );
          }
        }
      }
    }
    if ( checkRowCount ) {
      Assert.assertEquals( "number of rows", expected.getRowCount(), actual.getRowCount() );
    }
    if ( checkColumnCount ) {
      Assert.assertEquals( "number of columns", expected.getColumnCount(), actual.getColumnCount() );
    }
  }

  /**
   * checks if the columns names match
   */
  public void assertColumnNames( TableModel table, String...names ) {
    for ( int i = 0; i < names.length; i++ ) {
      if ( !Objects.equals( names[i], table.getColumnName( i ) ) ) {
        throw new ComparisonFailure( String.format( "wrong name for column %d", i ),
            names[i], table.getColumnName( i ) );
      }
    }
  }
  /**
   * checks if the columns names match
   */
  public void assertColumnClasses( boolean allowSubclasses, TableModel table, Class<?>...classes ) {
    for ( int i = 0; i < classes.length; i++ ) {
      Class<?> expected = classes[i];
      Class<?> actual = table.getColumnClass( i );
      if ( ( !allowSubclasses || Objects.equals( expected, actual ) ) || !expected.isAssignableFrom( actual ) ) {
        throw new ComparisonFailure( String.format( "wrong class for column %d", i ),
            expected.getName(), actual.getName() );
      }
    }
  }

  private static final String getClassDesc( Object obj ) {
    return obj != null ? obj.getClass().getSimpleName() : "";
  }

}
