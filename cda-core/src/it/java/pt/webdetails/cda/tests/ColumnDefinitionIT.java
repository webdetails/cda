/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;
import pt.webdetails.cda.utils.TableModelUtils;

import javax.swing.table.TableModel;

public class ColumnDefinitionIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( ColumnDefinitionIT.class );

  @Test
  public void testColumnNames() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-columnDefinition.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "status", "Shipped" );

    DataAccess dataAccess = cdaSettings.getDataAccess( "1" );

    logger.info( "Doing query" );
    TableModel table = TableModelUtils.postProcessTableModel( dataAccess, queryOptions,
      doQuery( cdaSettings, queryOptions ) );

    assertEquals( table.getColumnCount(), 3 );
    assertEquals( table.getColumnName( 0 ), "Year" );
    assertEquals( table.getColumnName( 1 ), "STATUS" );
    assertEquals( table.getColumnName( 2 ), "PriceInK" );

  }

}
