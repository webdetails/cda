package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.Comparator;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 3:02:09 PM
 */
public class SortableTableModel extends TypedTableModel implements Comparator
{

  protected int currCol;
  protected ArrayList<Integer> ascendCol;  // this vector stores the state (ascending or descending) of each column
  protected Integer one = new Integer(1);
  protected Integer minusOne = new Integer(-1);

  public SortableTableModel()
  {
    ascendCol = new ArrayList();
    throw new IllegalStateException("TODO - SortableTableModel not implemented yet");
  }


  public void addColumn(final String name, final Class type)
  {
    super.addColumn(name, type);
    ascendCol.add(one);
  }


  /***************************************************************************
   * This method sorts the rows using Java's Collections class.
   * After sorting, it changes the state of the column -
   * if the column was ascending, its new state is descending, and vice versa.
   ***************************************************************************/
  public void sort() {
      //Collections.sort(data, this);
      Integer val = (Integer) ascendCol.get(currCol);
      ascendCol.remove(currCol);
      if(val.equals(one)) // change the state of the column
          ascendCol.add(currCol, minusOne);
      else
          ascendCol.add(currCol, one);
  }

  public void sortByColumn(int column) {
      this.currCol = column;
      sort();
      //fireTableChanged(new TableModelEvent(this));
  }




  /*****************************************************************
   * This method is the implementation of the Comparator interface.
   * It is used for sorting the rows
   *****************************************************************/
  public int compare(Object v1, Object v2) {

      // the comparison is between 2 vectors, each representing a row
      // the comparison is done between 2 objects from the different rows that are in the column that is being sorted

      int ascending = (ascendCol.get(currCol)).intValue();
      if (v1 == null && v2 == null) {
          return 0;
      } else if (v2 == null) { // Define null less than everything.
          return 1 * ascending;
      } else if (v1 == null) {
          return -1 * ascending;
      }

      Object o1 = ((ArrayList) v1).get(currCol);
      Object o2 = ((ArrayList) v2).get(currCol);

      // If both values are null, return 0.
      if (o1 == null && o2 == null) {
          return 0;
      } else if (o2 == null) { // Define null less than everything.
          return 1 * ascending;
      } else if (o1 == null) {
          return -1 * ascending;
      }

      if (o1 instanceof Number && o2 instanceof Number) {
          Number n1 = (Number) o1;
          double d1 = n1.doubleValue();
          Number n2 = (Number) o2;
          double d2 = n2.doubleValue();

          if (d1 == d2) {
              return 0;
          } else if (d1 > d2) {
              return 1 * ascending;
          } else {
              return -1 * ascending;
          }

      } else if (o1 instanceof Boolean && o2 instanceof Boolean) {
          Boolean bool1 = (Boolean) o1;
          boolean b1 = bool1.booleanValue();
          Boolean bool2 = (Boolean) o2;
          boolean b2 = bool2.booleanValue();

          if (b1 == b2) {
              return 0;
          } else if (b1) {
              return 1 * ascending;
          } else {
              return -1 * ascending;
          }

      } else {
          // default case
          if (o1 instanceof Comparable && o2 instanceof Comparable) {
              Comparable c1 = (Comparable) o1;
              Comparable c2 = (Comparable) o2; // superflous cast, no need for it!

              try {
                  return c1.compareTo(c2) * ascending;
              } catch (ClassCastException cce) {
                  // forget it... we'll deal with them like 2 normal objects below.
              }
          }

          String s1 = o1.toString();
          String s2 = o2.toString();
          return s1.compareTo(s2) * ascending;
      }
  }
  
  
}
