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

package pt.webdetails.cda.exporter;

import java.io.OutputStream;

import javax.swing.table.TableModel;

public interface TableExporter extends Exporter {
  public void export( OutputStream out, final TableModel tableModel ) throws ExporterException;
}
