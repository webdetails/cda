/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.utils.kettle;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author pdpi
 */
public class SortableTableModel extends AbstractTableModel
{

  private static final long serialVersionUID = 1L;
  private static Log logger = LogFactory.getLog(SortableTableModel.class);
  
  private TableModel base;
  private Integer[] sortedIndices;


  public SortableTableModel(TableModel base)
  {
    this.base = base;
    sortedIndices = new Integer[base.getRowCount()];
    for (int i = 0; i < sortedIndices.length; i++)
    {
      sortedIndices[i] = i;
    }
  }


  @Override
  public int getRowCount()
  {
    return base.getRowCount();
  }


  @Override
  public int getColumnCount()
  {
    return base.getColumnCount();
  }


  @Override
  public String getColumnName(int idx)
  {
    return base.getColumnName(idx);
  }


  @Override
  public Class<?> getColumnClass(int idx)
  {
    return base.getColumnClass(idx);
  }


  @Override
  public void removeTableModelListener(TableModelListener l)
  {
    base.removeTableModelListener(l);
  }


  @Override
  public Object getValueAt(int i, int i1)
  {
    return base.getValueAt(sortedIndices[i], i1);
  }


  @Override
  public void setValueAt(Object val, int i, int i1)
  {
    base.setValueAt(val, sortedIndices[i], i1);
  }


  @Override
  public boolean isCellEditable(int i, int i1)
  {
    return base.isCellEditable(sortedIndices[i], i1);
  }


  public void sort()
  {
    List<Integer> idxs = Arrays.asList(sortedIndices);
    Collections.sort(idxs);
    sortedIndices = idxs.toArray(sortedIndices);
  }


  public void sort(Class<? extends Comparator<Integer>> klass, List<String> sortBy) throws ClassCastException, SortException
  {
    if (!Comparator.class.isAssignableFrom(klass))
    {

      ClassCastException e = new ClassCastException("Need a Comparator");
      logger.error(e);
      throw e;
    }
    try
    {
      Comparator<Integer> comp = klass.getConstructor(TableModel.class, List.class).newInstance(base, sortBy);
      List<Integer> idxs = Arrays.asList(sortedIndices);
      Collections.sort(idxs, comp);
      sortedIndices = idxs.toArray(sortedIndices);
    }
    catch (Exception e)
    {
      if (e instanceof NoSuchMethodException || e instanceof InstantiationException)
      {
        ClassCastException se = new ClassCastException("Invalid Comparator");
        logger.error(se);
        throw se;
      }
      else
      {
        SortException se = new SortException("Couldn't sort", e);
        logger.error(se);
        throw se;
      }
    }
  }
}
