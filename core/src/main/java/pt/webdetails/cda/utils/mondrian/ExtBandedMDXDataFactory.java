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

package pt.webdetails.cda.utils.mondrian;


import mondrian.olap.Result;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXDataFactory;

import javax.swing.table.TableModel;

public class ExtBandedMDXDataFactory extends BandedMDXDataFactory {

  public TableModel queryData( final String queryName, final DataRow parameters ) throws ReportDataFactoryException {
    final Result cellSet = performQuery( queryName, parameters );
    return postProcess( queryName, parameters,
      new ExtBandedMDXTableModel( cellSet, extractQueryLimit( parameters ), isMembersOnAxisSorted() ) );
  }

}
