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

package pt.webdetails.cda.tests;

import javax.swing.table.TableModel;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;
import pt.webdetails.cda.tests.utils.CdaTestHelper;

import java.util.LinkedList;

public class SqlListIT extends CdaTestCase {
  //  private static final Log logger = LogFactory.getLog(SqlTest.class);

  public SqlListIT() {
    super();
  }

  public SqlListIT( final String name ) {
    super( name );
  }

  public void testStringArrayParameter() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-list.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "status", new String[] { "Shipped", "Cancelled" } );
    queryOptions.setOutputType( ExporterEngine.OutputType.XML );

    TableModel table = engine.doQuery( cdaSettings, queryOptions );
    assertTrue( CdaTestHelper.columnContains( table, 0, "Shipped", "Cancelled" ) );
    assertFalse( CdaTestHelper.columnContains( table, 0, "Disputed" ) );

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    LinkedList l = new LinkedList<String>();
    l.add( "Shipped" );
    l.add( "Cancelled" );
    queryOptions.addParameter( "status", l );
    queryOptions.setOutputType( ExporterEngine.OutputType.XML );

    table = engine.doQuery( cdaSettings, queryOptions );
    assertTrue( CdaTestHelper.columnContains( table, 0, "Shipped", "Cancelled" ) );
    assertFalse( CdaTestHelper.columnContains( table, 0, "Disputed" ) );

  }
}
