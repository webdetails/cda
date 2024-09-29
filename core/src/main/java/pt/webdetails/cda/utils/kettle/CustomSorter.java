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


package pt.webdetails.cda.utils.kettle;

import java.util.List;

import javax.swing.table.TableModel;

public class CustomSorter extends SortTableModel {

  @Override
  public TableModel doSort( TableModel unsorted, List<String> sortBy ) throws SortException {
    SortableTableModel sortable = new SortableTableModel( unsorted );
    sortable.sort();
    return sortable;
  }
}
