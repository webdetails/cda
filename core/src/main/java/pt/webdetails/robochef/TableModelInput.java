/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.robochef;

import java.util.concurrent.Callable;
import javax.swing.table.TableModel;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

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
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_STRING ) );
          break;

        } else if ( columnClass == java.util.Date.class ) {
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_DATE ) );
          break;

        } else if ( columnClass == Boolean.class ) {
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_BOOLEAN ) );
          break;

        } else if ( columnClass == java.math.BigDecimal.class || columnClass == java.math.BigInteger.class ) {
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_BIGNUMBER ) );
          break;

        } else if ( columnClass == Long.class || columnClass == Short.class || columnClass == Integer.class
          || columnClass == Byte.class ) {
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_INTEGER ) );
          break;

        } else if ( columnClass == Double.class || columnClass == Float.class ) {
          rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_NUMBER ) );
          break;

        } else {
          columnClass = columnClass.getSuperclass();
        }
      }
      if ( columnClass == Object.class )
      //TODO Maybe a warning log entry or something for this case?
      {
        rowMeta.addValueMeta( new ValueMeta( tableModel.getColumnName( i ), ValueMetaInterface.TYPE_STRING ) );
      }
    }

    return rowMeta;
  }
}
