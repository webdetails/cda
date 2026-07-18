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

/**
 * Stream exporter interface
 */
public interface StreamExporter extends Exporter {
  public void export( OutputStream out ) throws ExporterException;
}
