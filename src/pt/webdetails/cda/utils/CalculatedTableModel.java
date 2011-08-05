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
 * Todo: Document me!
 * <p/>
 * Date: 08.02.2010
 * Time: 16:21:28
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

  public Class<?> getColumnClass(final int columnIndex)
  {
    if (columnIndex < backendColumnCount)
    {
      return backend.getColumnClass(columnIndex);
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
      final String formulaNamespace;
      final String formulaExpression;
      if (formula.length() > 0 && formula.charAt(0) == '=')
      {
        formulaNamespace = "report";
        formulaExpression = formula.substring(1);
      }
      else
      {
        final int separator = formula.indexOf(':');
        if (separator <= 0 || ((separator + 1) == formula.length()))
        {
          // error: invalid formula.
          formulaNamespace = null;
          formulaExpression = null;
        }
        else
        {
          formulaNamespace = formula.substring(0, separator);
          formulaExpression = formula.substring(separator + 1);
        }
      }
      final Formula formulaObject = new Formula(formulaExpression);
      formulaObject.initialize(context);
      return formulaObject.evaluate();
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
