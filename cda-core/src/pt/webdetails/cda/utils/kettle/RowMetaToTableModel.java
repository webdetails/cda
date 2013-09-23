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
  private boolean                                         recordRowsRead;
  private final AtomicMarkableReference<RowMetaInterface> rowsReadMeta    = new AtomicMarkableReference<RowMetaInterface>(null, false);
  private List<Object[]>                                  rowsRead;

  private boolean                                         recordRowsWritten;
  private final AtomicMarkableReference<RowMetaInterface> rowsWrittenMeta = new AtomicMarkableReference<RowMetaInterface>(null, false); ;
  private List<Object[]>                                  rowsWritten;

  private boolean                                         recordRowsError;
  private final AtomicMarkableReference<RowMetaInterface> rowsErrorMeta   = new AtomicMarkableReference<RowMetaInterface>(null, false); ;
  private List<Object[]>                                  rowsError;

  public RowMetaToTableModel(final boolean recordRowsRead, final boolean recordRowsWritten, final boolean recordRowsError)
  {
    if (!(recordRowsWritten || recordRowsRead || recordRowsError)) throw new IllegalArgumentException("Not recording any output. Must listen to something.");
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

  public void rowReadEvent(final RowMetaInterface rowMeta, final Object[] row)
  {
    if (recordRowsRead) {
      rowsReadMeta.weakCompareAndSet(null, rowMeta, false, true);
      rowsRead.add(row);
    }
  }

  public void rowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row)
  {
    if (recordRowsWritten) {
      rowsWrittenMeta.weakCompareAndSet(null, rowMeta, false, true);
      rowsWritten.add(row);
    }
  }

  public void errorRowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row)
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

  private TableModel asTableModel(final RowMetaInterface rowMeta, final List<Object[]> rows)
  {
    if (rowMeta == null) return null;

    final TypedTableModel output = new TypedTableModel(rowMeta.getFieldNames(), getClassesForFields(rowMeta), rows.size());
    for (int i = 0; i < rows.size(); i++) {
      final Object[] row = rows.get(i);
      for (int j = 0; j < row.length; j++) {
        output.setValueAt(row[j], i, j);
      }
    }
    return output;
  }

  private Class<?>[] getClassesForFields(final RowMetaInterface rowMeta) throws IllegalArgumentException
  {
    final List<ValueMetaInterface> valueMetas = rowMeta.getValueMetaList();
    final Class<?>[] types = new Class[valueMetas.size()];
    for (int i = 0; i < valueMetas.size(); i++) {
      final ValueMetaInterface valueMeta = valueMetas.get(i);
      switch (valueMeta.getType()) {
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
