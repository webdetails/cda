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



package pt.webdetails.cda.utils.mondrian;

import mondrian.olap.Result;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DenormalizedMDXDataFactory;

import javax.swing.table.TableModel;

public class ExtDenormalizedMDXDataFactory extends DenormalizedMDXDataFactory {

  public TableModel queryData( final String queryName, final DataRow parameters ) throws ReportDataFactoryException {
    final Result cellSet = performQuery( queryName, parameters );
    return postProcess( queryName, parameters,
      new ExtDenormalizedMDXTableModel( cellSet, extractQueryLimit( parameters ), isMembersOnAxisSorted() ) );
  }

}
