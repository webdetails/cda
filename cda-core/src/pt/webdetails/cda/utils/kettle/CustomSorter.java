/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
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
