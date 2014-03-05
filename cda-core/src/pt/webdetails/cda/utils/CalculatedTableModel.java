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

package pt.webdetails.cda.utils;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.MetaTableModel;
import org.pentaho.reporting.engine.classic.core.wizard.DataAttributes;
import org.pentaho.reporting.engine.classic.core.wizard.EmptyDataAttributes;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.parser.ParseException;
import pt.webdetails.cda.dataaccess.ColumnDefinition;

/**
 * A {@link TableModel} extended with calculated columns.
 *
 * @author Thomas Morgner.
 */
public class CalculatedTableModel implements MetaTableModel
{
  private class DataAccessFormulaContext extends DefaultFormulaContext
  {
    private int rowIndex;
    private boolean[] columnLocks;

    private DataAccessFormulaContext(final int rowIndex)
    {
      this.rowIndex = rowIndex;
      this.columnLocks = new boolean[calculatedColumns.length];
    }

    public int getRowIndex()
    {
      return rowIndex;
    }

    public void lock(final int calcColumnIndex)
    {
      if (columnLocks[calcColumnIndex])
      {
        throw new IllegalStateException("Infinite loop while evaluating a formula");
      }

      columnLocks[calcColumnIndex] = true;
    }

    public void unlock(final int calcColumnIndex)
    {
      columnLocks[calcColumnIndex] = true;
    }

    public Object resolveReference(final Object name)
    {
      for (int column = 0; column < getColumnCount(); column++)
      {
        if (getColumnName(column).equals(name))
        {
          return getValueAt(rowIndex, column);
        }
      }
      return null;
    }
  }

  private MetaTableModel metaTableModel;
  private TableModel backend;
  private ColumnDefinition[] calculatedColumns;
  private int backendColumnCount;
  private boolean inferTypes = false;
  private Class<?>[] calculatedColumnClasses;

  /**
   * 
   * @param backend Table that provides the first columns of the table, which can be used by the calculated columns 
   * @param calculatedColumns Formula-based columns to be evaluated
   * @param inferColumnTypes Whether to attempt to determine column types as they are calculated
   */
  public CalculatedTableModel(final TableModel backend, final ColumnDefinition[] calculatedColumns, boolean inferColumnTypes){
    this(backend, calculatedColumns);
    inferTypes = inferColumnTypes;
    if(inferTypes){
      calculatedColumnClasses = new Class<?>[calculatedColumns.length];
    }
  }
  
  public CalculatedTableModel(final TableModel backend, final ColumnDefinition[] calculatedColumns)
  {
    if (backend == null)
    {
      throw new IllegalArgumentException("Attempting to create a calculated table model from null table.");
    }
    if (calculatedColumns == null)
    {
      throw new IllegalArgumentException("Null calculated columns.");
    }
    this.backend = backend;
    this.backendColumnCount = backend.getColumnCount();
    this.calculatedColumns = calculatedColumns.clone();
    if (backend instanceof MetaTableModel)
    {
      this.metaTableModel = (MetaTableModel) backend;
    }
  }

  public int getRowCount()
  {
    return backend.getRowCount();
  }

  public int getColumnCount()
  {
    return backend.getColumnCount() + calculatedColumns.length;
  }

  public String getColumnName(final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.getColumnName(columnIndex);
    }
    final int calculatedColumnIndex = columnIndex - backendColumnCount;
    return calculatedColumns[calculatedColumnIndex].getName();
  }

  /**
   * If set to infer types, result may change as cells are evaluated.
   */
  public Class<?> getColumnClass(final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.getColumnClass(columnIndex);
    }
    else if (inferTypes){
      final int calcColumnIndex = columnIndex - backendColumnCount;
      if(calcColumnIndex < calculatedColumnClasses.length && calculatedColumnClasses[calcColumnIndex] != null){
        return calculatedColumnClasses[calcColumnIndex];
      }
    }
    return Object.class;
  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.isCellEditable(rowIndex, columnIndex);
    }
    return false;
  }
  
  protected void accumulateClassAt(final int calcColumnIndex, Class<?> valueClass){
    if(calculatedColumnClasses[calcColumnIndex] == null){
      calculatedColumnClasses[calcColumnIndex] = valueClass;
    }
    else if (!calculatedColumnClasses[calcColumnIndex].isAssignableFrom(valueClass)){
      if(valueClass.isAssignableFrom(calculatedColumnClasses[calcColumnIndex])){
        calculatedColumnClasses[calcColumnIndex] = valueClass;
      }
      else {
        calculatedColumnClasses[calcColumnIndex] = Object.class;
      }
    }
  }

  protected Object getValueInternal(final int columnIndex, final DataAccessFormulaContext context)
      throws ParseException, EvaluationException
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.getValueAt(context.getRowIndex(), columnIndex);
    }

    final int calcColumnIndex = columnIndex - backendColumnCount;
    try
    {
      context.lock(calcColumnIndex);
      final String formula = calculatedColumns[calcColumnIndex].getFormula();
//      final String formulaNamespace;
      final String formulaExpression;
      if (formula.length() > 0 && formula.charAt(0) == '=')
      {
//        formulaNamespace = "report";
        formulaExpression = formula.substring(1);
      }
      else
      {
        final int separator = formula.indexOf(':');
        if (separator <= 0 || ((separator + 1) == formula.length()))
        {
          // error: invalid formula.
//          formulaNamespace = null;
          formulaExpression = null;
        }
        else
        {
//          formulaNamespace = formula.substring(0, separator);
          formulaExpression = formula.substring(separator + 1);
        }
      }
      final Formula formulaObject = new Formula(formulaExpression);
      formulaObject.initialize(context);
      Object value = formulaObject.evaluate();
      if(inferTypes && value != null){
        accumulateClassAt(calcColumnIndex, value.getClass());
      }
      return value;
    }
    finally
    {
      context.unlock(calcColumnIndex);
    }
  }

  public Object getValueAt(final int rowIndex, final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.getValueAt(rowIndex, columnIndex);
    }

    try
    {
      final DataAccessFormulaContext formulaContext = new DataAccessFormulaContext(rowIndex);
      return getValueInternal(columnIndex, formulaContext);
    }
    catch (Exception e)
    {
      throw new IllegalStateException(new CalculatedColumnException("Error in calculated column position (" + rowIndex + "," + columnIndex + ");",e));
    }
  }

  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      backend.setValueAt(aValue, rowIndex, columnIndex);
    }
  }

  public void addTableModelListener(final TableModelListener l)
  {
  }

  public void removeTableModelListener(final TableModelListener l)
  {
  }

  public DataAttributes getCellDataAttributes(final int row, final int columnIndex)
  {
    if (metaTableModel == null)
    {
      return EmptyDataAttributes.INSTANCE;
    }
    if (columnIndex < backendColumnCount)
    {
      return metaTableModel.getCellDataAttributes(row, columnIndex);
    }
    return EmptyDataAttributes.INSTANCE;
  }

  public boolean isCellDataAttributesSupported()
  {
    if (metaTableModel == null)
    {
      return false;
    }
    return metaTableModel.isCellDataAttributesSupported();
  }

  public DataAttributes getColumnAttributes(final int columnIndex)
  {
    if (metaTableModel == null)
    {
      return EmptyDataAttributes.INSTANCE;
    }
    if (columnIndex < backendColumnCount)
    {
      return metaTableModel.getColumnAttributes(columnIndex);
    }
    return EmptyDataAttributes.INSTANCE;
  }

  public DataAttributes getTableAttributes()
  {
    if (metaTableModel == null)
    {
      return EmptyDataAttributes.INSTANCE;
    }
    return metaTableModel.getTableAttributes();
  }
}
