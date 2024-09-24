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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.reporting.engine.classic.core.states.datarow.EmptyTableModel;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Function;


/**
 * Bridge class between Kettle's RowMeta and CDA's TableModel
 */
public class RowMetaToTableModel implements RowListener {

  private static final Log logger = LogFactory.getLog( RowMetaToTableModel.class );

  private boolean recordRowsRead;
  private final AtomicMarkableReference<RowMetaInterface> rowsReadMeta =
    new AtomicMarkableReference<RowMetaInterface>( null, false );
  private List<Object[]> rowsRead;

  private boolean recordRowsWritten;
  private final AtomicMarkableReference<RowMetaInterface> rowsWrittenMeta =
    new AtomicMarkableReference<RowMetaInterface>( null, false );

  private List<Object[]> rowsWritten;

  private boolean recordRowsError;
  private final AtomicMarkableReference<RowMetaInterface> rowsErrorMeta =
    new AtomicMarkableReference<RowMetaInterface>( null, false );

  private List<Object[]> rowsError;

  public RowMetaToTableModel( final boolean recordRowsRead, final boolean recordRowsWritten,
                              final boolean recordRowsError ) {
    if ( !( recordRowsWritten || recordRowsRead || recordRowsError ) ) {
      throw new IllegalArgumentException( "Not recording any output. Must listen to something." );
    }
    if ( recordRowsRead ) {
      this.recordRowsRead = true;
      rowsRead = new ArrayList<Object[]>();
    }
    if ( recordRowsWritten ) {
      this.recordRowsWritten = true;
      rowsWritten = new ArrayList<Object[]>();
    }
    if ( recordRowsError ) {
      this.recordRowsError = true;
      rowsError = new ArrayList<Object[]>();
    }
  }

  public void rowReadEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    if ( recordRowsRead ) {
      rowsReadMeta.weakCompareAndSet( null, rowMeta, false, true );
      rowsRead.add( row );
    }
  }

  public void rowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    if ( recordRowsWritten ) {
      rowsWrittenMeta.weakCompareAndSet( null, rowMeta, false, true );
      rowsWritten.add( row );
    }
  }

  public void errorRowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    if ( recordRowsError ) {
      rowsErrorMeta.weakCompareAndSet( null, rowMeta, false, true );
      rowsError.add( row );
    }
  }

  public TableModel getRowsRead() {
    return asTableModel( rowsReadMeta.getReference(), rowsRead );
  }

  public TableModel getRowsWritten() {
    return asTableModel( rowsWrittenMeta.getReference(), rowsWritten );
  }

  public TableModel getRowsError() {
    return asTableModel( rowsErrorMeta.getReference(), rowsError );
  }

  private TableModel asTableModel( final RowMetaInterface rowMeta, final List<Object[]> rows ) {
    if ( rowMeta == null ) {
      return null;
    }

    final TypedTableModel output =
      new TypedTableModel( rowMeta.getFieldNames(), getClassesForFields( rowMeta ), rows.size() );
    for ( int i = 0; i < rows.size(); i++ ) {
      final Object[] row = rows.get( i );
      for ( int j = 0; j < row.length; j++ ) {
        output.setValueAt( row[ j ], i, j );
      }
    }
    return output;
  }

  private static Class<?>[] getClassesForFields( final RowMetaInterface rowMeta ) throws IllegalArgumentException {
    final List<ValueMetaInterface> valueMetas = rowMeta.getValueMetaList();
    final Class<?>[] types = new Class[ valueMetas.size() ];
    for ( int i = 0; i < valueMetas.size(); i++ ) {
      final ValueMetaInterface valueMeta = valueMetas.get( i );
      switch ( valueMeta.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          types[ i ] = String.class;
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          types[ i ] = Double.class;
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          types[ i ] = Long.class;
          break;
        case ValueMetaInterface.TYPE_DATE:
          types[ i ] = java.util.Date.class;
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          types[ i ] = java.math.BigDecimal.class;
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          types[ i ] = Boolean.class;
          break;
        case ValueMetaInterface.TYPE_BINARY:
          types[ i ] = byte[].class;
          break;
        case ValueMetaInterface.TYPE_SERIALIZABLE:
        case ValueMetaInterface.TYPE_NONE:
        default:
          throw new IllegalArgumentException( String.format( "No type conversion found for Field %d %s", i, valueMeta
            .toString() ) );
      }
    }
    return types;
  }

  /**
   * 
   * @return converter from a list of {@link RowMetaAndData} to a {@link TableModel}
   */
  public static Function<List<RowMetaAndData>, TableModel> getConverter() {
    return new Function<List<RowMetaAndData>, TableModel>() {
      private TableModelHeader header;

      @Override
      public TableModel apply( final List<RowMetaAndData> rowMetaAndData ) {
        if ( logger.isTraceEnabled() ) {
          logger.trace( " new list received, " + rowMetaAndData.size() + " items " );
        }
        if ( rowMetaAndData.isEmpty() ) {
          return new EmptyTableModel();
        }
        if ( header == null ) {
          header = new TableModelHeader( rowMetaAndData.get( 0 ).getRowMeta() );
        }
        return new AbstractTableModel() {
          private static final long serialVersionUID = 1L;

          @Override
          public int getRowCount() {
            return rowMetaAndData.size();
          }

          @Override
          public int getColumnCount() {
            return header.getColumnCount();
          }

          @Override
          public String getColumnName( int column ) {
            return header.getColumnName( column );
          }

          @Override
          public Class<?> getColumnClass( int columnIndex ) {
            return header.getColumnClass( columnIndex );
          }
          @Override
          public Object getValueAt( int rowIndex, int columnIndex ) {
            RowMetaAndData rowMetaData = rowMetaAndData.get( rowIndex );
            Object value = rowMetaData.getData()[ columnIndex ];

            //getting dates as java.sql.Timestamp and not as java.util.Date makes simpler to deal with them
            //since they get formated as a simpler format when "stringded"
            if ( value != null
              && value instanceof Date
              && rowMetaData.getValueMeta( columnIndex ).getType() == ValueMetaInterface.TYPE_DATE ) {

              Calendar calendar = Calendar.getInstance();
              calendar.setTime( (Date) value );
              return new Timestamp( calendar.getTimeInMillis() );
            }

            return value;
          }
        };
      }
    };
  }

  private static class TableModelHeader {
    private final String[] columnNames;
    private final Class<?>[] columnClasses;

    public TableModelHeader( RowMetaInterface rowMeta ) {
      columnNames = rowMeta.getFieldNames();
      columnClasses = getClassesForFields( rowMeta );
    }

    public Class<?> getColumnClass( int column ) {
      return columnClasses[column];
    }

    public String getColumnName( int column ) {
      return columnNames[column];
    }

    public int getColumnCount() {
      return columnNames.length;
    }
  }
}
