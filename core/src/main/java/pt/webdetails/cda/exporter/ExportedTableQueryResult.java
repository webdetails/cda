/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.exporter;

import java.io.OutputStream;

import javax.swing.table.TableModel;

public class ExportedTableQueryResult extends ExportedQueryResult {

  private TableModel table;
  private TableExporter exporter;

  public ExportedTableQueryResult( TableExporter exporter, TableModel table ) {
    super( exporter );
    this.table = table;
    this.exporter = exporter;
  }

  @Override
  public TableExporter getExporter() {
    return exporter;
  }

  public void writeOut( OutputStream out ) throws ExporterException {
    getExporter().export( out, table );
  }
}
