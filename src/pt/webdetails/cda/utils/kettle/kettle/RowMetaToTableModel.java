package pt.webdetails.cda.utils.kettle.kettle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;

import javax.swing.table.TableModel;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;


/**
 * Bridge class between Kettle's RowMeta and CDA's TableModel
 *
 * @author Daniel Einspanjer
 */
public class RowMetaToTableModel implements RowListener
{
  private boolean recordRowsRead;
  private AtomicMarkableReference<RowMetaInterface> rowsReadMeta = new AtomicMarkableReference<RowMetaInterface>(null, false);
  private List<Object[]> rowsRead;

  private boolean recordRowsWritten;
  private AtomicMarkableReference<RowMetaInterface> rowsWrittenMeta = new AtomicMarkableReference<RowMetaInterface>(null, false);;
  private List<Object[]> rowsWritten;

  private boolean recordRowsError;
  private AtomicMarkableReference<RowMetaInterface> rowsErrorMeta = new AtomicMarkableReference<RowMetaInterface>(null, false);;
  private List<Object[]> rowsError;

  public RowMetaToTableModel(boolean recordRowsRead, boolean recordRowsWritten, boolean recordRowsError)
  {
    if (!(recordRowsWritten || recordRowsRead || recordRowsError))
    {
      throw new IllegalArgumentException("Not recording any output. Must listen to something.");
    }
    if (recordRowsRead) {
      this.recordRowsRead = true;
      rowsRead = new ArrayList<Object[]>();
    }
    if (recordRowsWritten) {
      this.recordRowsWritten = true;
      rowsWritten = new ArrayList<Object[]>();
    }
    if (recordRowsError) {
      this.recordRowsError = true;
      rowsError = new ArrayList<Object[]>();
    }
  }

  public void rowReadEvent(RowMetaInterface rowMeta, Object[] row)
  {
    if (recordRowsRead) {
      rowsReadMeta.weakCompareAndSet(null, rowMeta, false, true);
      rowsRead.add(row);
    }
  }

  public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
  {
    if (recordRowsWritten) {
      rowsWrittenMeta.weakCompareAndSet(null, rowMeta, false, true);
      rowsWritten.add(row);
    }
  }

  public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
  {
    if (recordRowsError) {
      rowsErrorMeta.weakCompareAndSet(null, rowMeta, false, true);
      rowsError.add(row);
    }
  }

  public TableModel getRowsRead()
  {
    return asTableModel(rowsReadMeta.getReference(), rowsRead);
  }

  public TableModel getRowsWritten()
  {
    return asTableModel(rowsWrittenMeta.getReference(), rowsWritten);
  }

  public TableModel getRowsError()
  {
    return asTableModel(rowsErrorMeta.getReference(), rowsError);
  }

  private TableModel asTableModel(RowMetaInterface rowMeta, List<Object[]> rows)
  {
    TypedTableModel output = new TypedTableModel(rowMeta.getFieldNames(), getClassesForFields(rowMeta), rows.size());
    for (int i = 0; i < rows.size(); i++)
    {
      Object[] row = rows.get(i);
      for (int j = 0; j < row.length; j++)
      {
        output.setValueAt(row[j], i, j);
      }
    }
    return output;
  }

  private Class<?>[] getClassesForFields(RowMetaInterface rowMeta) throws IllegalArgumentException
  {
    List<ValueMetaInterface> valueMetas = rowMeta.getValueMetaList();
    Class<?>[] types = new Class[valueMetas.size()];
    for (int i = 0; i < valueMetas.size(); i++)
    {
      ValueMetaInterface valueMeta = valueMetas.get(i);
      switch (valueMeta.getType())
      {
        case ValueMetaInterface.TYPE_STRING:
          types[i] = String.class;
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          types[i] = Double.class;
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          types[i] = Long.class;
          break;
        case ValueMetaInterface.TYPE_DATE:
          types[i] = java.util.Date.class;
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          types[i] = java.math.BigDecimal.class;
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          types[i] = Boolean.class;
          break;
        case ValueMetaInterface.TYPE_BINARY:
          types[i] = byte[].class;
          break;
        case ValueMetaInterface.TYPE_SERIALIZABLE:
        case ValueMetaInterface.TYPE_NONE:
        default:
          throw new IllegalArgumentException(String.format("No type conversion found for Field %d %s", i, valueMeta.toString()));
      }
    }
    return types;
  }
}