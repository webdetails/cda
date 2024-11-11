/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.exporter;

import java.io.OutputStream;

public class ExportedStreamQueryResult extends ExportedQueryResult {

  public ExportedStreamQueryResult( StreamExporter exporter ) {
    super( exporter );
  }

  public void writeOut( OutputStream out ) throws ExporterException {
    ( (StreamExporter) getExporter() ).export( out );
  }
}
