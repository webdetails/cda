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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.CsvExporter;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class CsvXslFromSQLTest extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( CsvXslFromSQLTest.class );

  public void testCsvXlsFromSQLExport() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-CsvXslFromSQLTest.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "Ds1" );

    String fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestCSV.csv";
    OutputStream out = new FileOutputStream( fileName );
    logger.info( "Doing streaming csv export" );
    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );
    // (ExportedTableQueryResult)
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    assertEquals( countCSVColumns( fileName ), 2 );

    fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestXLS.xls";
    out = new FileOutputStream( fileName );
    logger.info( "Doing streaming xls export" );
    queryOptions.setOutputType( "xls" );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    assertEquals( countXLSColumns( fileName ), 2 );
  }

  public int countXLSColumns( String filename ) throws IOException {

    File f = new File( filename );

    HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) );
    HSSFSheet sheet = workbook.getSheetAt( 0 );
    int noOfColumns = sheet.getRow( 0 ).getPhysicalNumberOfCells();
    f.delete();
    return noOfColumns;
  }

  public int countCSVColumns( String filename ) throws IOException {

    int count = 0;
    String line;
    File f = new File( filename );

    BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );
    if ( ( line = br.readLine() ) != null ) {
      count = line.split( "," ).length;
    }
    br.close();
    f.delete();
    return count;
  }
}
