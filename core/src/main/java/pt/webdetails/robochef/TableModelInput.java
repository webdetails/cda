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

package pt.webdetails.robochef;

import java.util.concurrent.Callable;

import javax.swing.table.TableModel;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

public class TableModelInput extends RowProducerBridge {
  public synchronized Callable<Boolean> getCallableRowProducer( final TableModel tableModel,
                                                                final boolean markFinished ) {
    final Callable<Boolean> callable = new Callable<Boolean>() {

      public Boolean call() {
        final RowMetaInterface rowMeta = getRowMetaForTableModel( tableModel );
        start( rowMeta );

        for ( int i = 0; i < tableModel.getRowCount(); i++ ) {
          final Object[] row = new Object[ tableModel.getColumnCount() ];
          for ( int j = 0; j < tableModel.getColumnCount(); j++ ) {
            row[ j ] = getDataObjectForColumn( rowMeta.getValueMeta( j ), tableModel.getValueAt( i, j ) );
          }
          putRow( row );
        }

        if ( markFinished ) {
          finish();
        }
        return true;
      }
    };
    return callable;
  }

  private Object getDataObjectForColumn( final ValueMetaInterface valueMeta, final Object value ) {
    // Handle null case
    if ( value == null ) {
      return null;
    }

    Object newValue;
    switch( valueMeta.getType() ) {
      case ValueMetaInterface.TYPE_STRING:
        newValue = String.valueOf( value );
        break;
      case ValueMetaInterface.TYPE_NUMBER:
        if ( value instanceof Double ) {
          newValue = value;
        } else {
          newValue = Double.valueOf( value.toString() );
        }
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        if ( value instanceof Long ) {
          newValue = value;
        } else {
          newValue = Long.valueOf( value.toString() );
        }
        break;
      case ValueMetaInterface.TYPE_DATE:
        newValue = value;
        break;
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( value instanceof java.math.BigDecimal ) {
          newValue = value;
        } else {
          newValue = java.math.BigDecimal.valueOf( ( (java.math.BigInteger) value ).doubleValue() );
        }
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        newValue = value;
        break;
      default:
        throw new IllegalArgumentException(
          String.format( "ValueMeta mismatch %s (%s)", valueMeta.toString(), value ) );
    }
    return newValue;
  }

  private RowMetaInterface getRowMetaForTableModel( final TableModel tableModel ) throws IllegalArgumentException {
    final RowMetaInterface rowMeta = new RowMeta();
    for ( int i = 0; i < tableModel.getColumnCount(); i++ ) {
      Class<?> columnClass = tableModel.getColumnClass( i );
      while ( columnClass != Object.class ) {
        if ( columnClass == String.class ) {
          rowMeta.addValueMeta( new ValueMetaString( tableModel.getColumnName( i ) ) );
          break;

        } else if ( columnClass == java.util.Date.class ) {
          rowMeta.addValueMeta( new ValueMetaDate( tableModel.getColumnName( i ) ) );
          break;

        } else if ( columnClass == Boolean.class ) {
          rowMeta.addValueMeta( new ValueMetaBoolean( tableModel.getColumnName( i ) ) );
          break;

        } else if ( columnClass == java.math.BigDecimal.class || columnClass == java.math.BigInteger.class ) {
          rowMeta.addValueMeta( new ValueMetaBigNumber( tableModel.getColumnName( i ) ) );
          break;

        } else if ( columnClass == Long.class || columnClass == Short.class || columnClass == Integer.class
          || columnClass == Byte.class ) {
          rowMeta.addValueMeta( new ValueMetaInteger( tableModel.getColumnName( i ) ) );
          break;

        } else if ( columnClass == Double.class || columnClass == Float.class ) {
          rowMeta.addValueMeta( new ValueMetaNumber( tableModel.getColumnName( i ) ) );
          break;

        } else {
          columnClass = columnClass.getSuperclass();
        }
      }
      if ( columnClass == Object.class ) {
        rowMeta.addValueMeta( new ValueMetaString( tableModel.getColumnName( i ) ) );
      }
    }

    return rowMeta;
  }
}
