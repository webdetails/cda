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
import org.junit.Assert;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

import javax.swing.table.TableModel;
import java.util.LinkedList;
import java.util.List;

public class OutputColumnsIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( OutputColumnsIT.class );

  public OutputColumnsIT() {
    super();
  }

  public void testSingleOutputColumn() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-sql.cda" );
    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    List<String> outputColumnNames = new LinkedList<String>();
    outputColumnNames.add( "Year" );
    queryOptions.setOutputColumnName( outputColumnNames );
    TableModel tm = engine.doQuery( cdaSettings, queryOptions );

    Assert.assertEquals( tm.getColumnCount(), 1 );
    Assert.assertEquals( tm.getColumnName( 0 ), "Year" );
  }

  public void testMultipleOutputColumn() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-sql.cda" );
    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    List<String> outputColumnNames = new LinkedList<String>();
    outputColumnNames.add( "Year" );
    outputColumnNames.add( "STATUS" );
    queryOptions.setOutputColumnName( outputColumnNames );
    TableModel tm = engine.doQuery( cdaSettings, queryOptions );

    Assert.assertEquals( tm.getColumnCount(), 2 );
    Assert.assertEquals( tm.getColumnName( 0 ), "Year" );
    Assert.assertEquals( tm.getColumnName( 1 ), "STATUS" );
  }

  public void testMultipleOutputColumnOrder() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-sql.cda" );
    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    List<String> outputColumnNames = new LinkedList<String>();
    outputColumnNames.add( "Year" );
    outputColumnNames.add( "STATUS" );
    outputColumnNames.add( "PRICE" );
    queryOptions.setOutputColumnName( outputColumnNames );
    TableModel tm = engine.doQuery( cdaSettings, queryOptions );

    Assert.assertEquals( tm.getColumnName( 0 ), "Year" );
    Assert.assertEquals( tm.getColumnName( 1 ), "STATUS" );
    Assert.assertEquals( tm.getColumnName( 2 ), "PRICE" );

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    outputColumnNames = new LinkedList<String>();
    outputColumnNames.add( "PRICE" );
    outputColumnNames.add( "Year" );
    outputColumnNames.add( "STATUS" );
    queryOptions.setOutputColumnName( outputColumnNames );
    tm = engine.doQuery( cdaSettings, queryOptions );

    Assert.assertEquals( tm.getColumnName( 0 ), "PRICE" );
    Assert.assertEquals( tm.getColumnName( 1 ), "Year" );
    Assert.assertEquals( tm.getColumnName( 2 ), "STATUS" );
  }
}
