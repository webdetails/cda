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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;

/**
 * Counts processed rows
 */
public class RowCountListener implements RowListener {
  private int rowsRead;
  private int rowsWritten;
  private int rowsError;

  public RowCountListener() {
  }

  public void rowReadEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    rowsRead++;
  }

  public void rowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    rowsWritten++;
  }

  public void errorRowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) {
    rowsError++;
  }

  public int getRowsRead() {
    return rowsRead;
  }

  public int getRowsWritten() {
    return rowsWritten;
  }

  public int getRowsError() {
    return rowsError;
  }
}
