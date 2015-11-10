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
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

import javax.swing.table.TableModel;
import java.math.BigDecimal;

public class CalculatedTableModelTest extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( CalculatedTableModelTest.class );

  @Test
  public void testCalculatedColumnsValues() throws Exception {

    //Define an outputStream
    final CdaSettings cdaSettings = parseSettingsFile( "sample-scripting.cda" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "3" );

    logger.info( "Doing query" );
    TableModel table = doQuery( cdaSettings, queryOptions );

    assertEquals( table.getColumnCount(), 3 );
    assertEquals( table.getColumnName( 2 ), "NewValue" );

    assertEquals( table.getValueAt( 0, 2 ), new BigDecimal( 7 ) );
    assertNull( table.getValueAt( 1, 2 ) );
    assertNull( table.getValueAt( 2, 2 ) );
    assertNull( table.getValueAt( 3, 2 ) );
  }
}
