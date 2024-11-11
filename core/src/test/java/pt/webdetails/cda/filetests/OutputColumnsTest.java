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


package pt.webdetails.cda.filetests;

import org.junit.Assert;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

import javax.swing.table.TableModel;

import java.util.LinkedList;
import java.util.List;

public class OutputColumnsTest extends CdaTestCase {

  public OutputColumnsTest() {
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
