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

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

public class OutputIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( OutputIT.class );

  @Test
  public void testCsvExport() throws Exception {
    String expectedOutput = "\"[Measures].[MeasuresLevel]\";Year;price\n\"Sales\";445094.69;564842.02\n";

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "Shipped" );

    logger.info( "Doing query" );
    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "csv" );
    String csv = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( csv ) );
    assertEquals( expectedOutput, csv );
  }

  @Test
  public void testJsonExport() throws Exception {
    String expectedOutput = "{\"queryInfo\":{\"totalRows\":\"1\"},"
      + "\"resultset\":[[\"Sales\",445094.69,564842.02]],\""
      + "metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"[Measures].[MeasuresLevel]\"},"
      + "{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"Year\"},"
      + "{\"colIndex\":2,\"colType\":\"Numeric\",\"colName\":\"price\"}]}";

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.addParameter( "status", "Shipped" );

    logger.info( "Doing query" );
    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "json" );
    String json = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( json ) );
    assertTrue( jsonEquals( expectedOutput, json ) );
  }

  @Test
  public void testJsonExportNaNValues() throws Exception {
    String expectedOutput = "{\"queryInfo\":{\"totalRows\":\"1\"},"
      + "\"resultset\":[[\"All Markets\",null]],"
      + "\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"[Markets].[(All)]\"},"
      + "{\"colIndex\":1,\"colType\":\"Numeric\",\"colName\":\"[Measures].[Invalid]\"}]}";

    final CdaSettings cdaSettings = parseSettingsFile( "sample-output.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );

    logger.info( "Doing query" );
    TableModel table = doQuery( cdaSettings, queryOptions );

    queryOptions.setOutputType( "json" );
    String json = exportTableModel( table, queryOptions );
    assertFalse( StringUtils.isEmpty( json ) );
    assertTrue( jsonEquals( expectedOutput, json ) );
  }

  protected boolean jsonEquals( String json1, String json2 ) throws Exception {
    ObjectMapper om = new ObjectMapper();
    JsonNode parsedJson1 = om.readTree( json1 );
    JsonNode parsedJson2 = om.readTree( json2 );
    return parsedJson1.equals( parsedJson2 );
  }
}
