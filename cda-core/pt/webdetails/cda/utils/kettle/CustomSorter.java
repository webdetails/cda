/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.utils.kettle;

import java.util.ArrayList;
import javax.swing.table.TableModel;

/**
 *
 * @author pdpi
 */
public class CustomSorter extends SortTableModel
{

  @Override
  public TableModel doSort(TableModel unsorted, ArrayList<String> sortBy) throws SortException
  {
    SortableTableModel sortable = new SortableTableModel(unsorted);
    sortable.sort();
    return sortable;
  }
}
