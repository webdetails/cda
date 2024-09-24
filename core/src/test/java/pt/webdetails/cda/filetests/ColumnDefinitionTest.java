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

package pt.webdetails.cda.filetests;

import org.junit.Test;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.TableModelUtils;

import javax.swing.table.TableModel;

public class ColumnDefinitionTest extends CdaTestCase {

  @Test
  public void testColumnNames() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-columnDefinition.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "status", "Shipped" );

    DataAccess dataAccess = cdaSettings.getDataAccess( "1" );

    TableModel table =
      TableModelUtils.postProcessTableModel( dataAccess, queryOptions, doQuery( cdaSettings, queryOptions ) );

    assertEquals( table.getColumnCount(), 3 );
    assertEquals( table.getColumnName( 0 ), "Year" );
    assertEquals( table.getColumnName( 1 ), "STATUS" );
    assertEquals( table.getColumnName( 2 ), "PriceInK" );

  }

}
