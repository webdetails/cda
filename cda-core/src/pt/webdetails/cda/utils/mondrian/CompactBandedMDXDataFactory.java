/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.utils.mondrian;

import javax.swing.table.TableModel;

import mondrian.olap.Result;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;

/**
 * This data-factory operates in Legacy-Mode providing a preprocessed view on the mondrian result.
 * It behaves exactly as known from the Pentaho-Platform and the Pentaho-Report-Designer. This mode
 * of operation breaks the structure of the resulting table as soon as new rows are returned by the
 * server.
 *
 * @author Thomas Morgner
 */
public class CompactBandedMDXDataFactory extends AbstractNamedMDXDataFactory
{
  public CompactBandedMDXDataFactory()
  {
  }

  /**
   * Queries a datasource. The string 'query' defines the name of the query. The Parameterset given here may contain
   * more data than actually needed for the query.
   * <p/>
   * The parameter-dataset may change between two calls, do not assume anything, and do not hold references to the
   * parameter-dataset or the position of the columns in the dataset.
   *
   * @param queryName  the query name
   * @param parameters the parameters for the query
   * @return the result of the query as table model.
   * @throws ReportDataFactoryException if an error occured while performing the query.
   */
  public TableModel queryData(final String queryName, final DataRow parameters) throws ReportDataFactoryException
  {
    final Result cellSet = performQuery(queryName, parameters);
    return new CompactBandedMDXTableModel(cellSet, extractQueryLimit(parameters));
  }
}
