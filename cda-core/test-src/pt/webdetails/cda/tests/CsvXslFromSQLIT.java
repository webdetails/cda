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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.CsvExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class CsvXslFromSQLIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( CsvXslFromSQLIT.class );

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

  public void testCsvXlsFromSQLExportWithElevenParameters() throws Exception {
    //[CDA-112] - This test makes sure that it is possible to export with more than 10 parameters
    final CdaSettings cdaSettings = parseSettingsFile( "sample-CsvXslFromSQLWithElevenParametersTest.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = CdaEngine.getInstance();
    final int numberOfParameters = 11;

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "Ds2" );
    for ( int i = 1; i < numberOfParameters; i++ ) {
      queryOptions.setParameter( "parameter" + i, "1" );
    }

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

  public void testXlsFromSQLExportWithCalculatedColumns() throws Exception {
    //This file has 2 Datasources, Ds1 and Ds2, both share the same query, but only Ds2 sets outputIndexes
    final CdaSettings cdaSettings = parseSettingsFile( "sample-XlsFromSQLWithCalculatedColumnsTest.cda" );

    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();

    queryOptions.setDataAccessId( "Ds1" );
    String fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestXLS.xls";
    OutputStream out = new FileOutputStream( fileName );
    queryOptions.setOutputType( "xls" );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    //Ds1 does not have outputIndexes, here we extract all the column names
    String[] colNames = extractColumnNames( fileName );

    queryOptions.setDataAccessId( "Ds2" );
    out = new FileOutputStream( fileName );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    //Ds2 set outputIndexes, using them we see if all is exported ordered as intended
    //one index is actually from the calculatedColumn, effectively checking the column is exported
    List<Integer> outputIndexes = cdaSettings.getDataAccess( "Ds2" ).getOutputs();
    assertTrue( matchColumnNames( fileName, colNames, outputIndexes ) );
  }

  public String[] extractColumnNames( String fileName ) throws IOException {
    ArrayList<String> columnNames = new ArrayList<String>();
    File f = new File( fileName );
    HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) );
    HSSFSheet sheet = workbook.getSheetAt( 0 );
    HSSFRow row = sheet.getRow( 0 );
    int i = 0;
    HSSFCell cell = row.getCell( i );
    while ( cell != null ) {
      columnNames.add( cell.getStringCellValue() );
      cell = row.getCell( ++i );
    }
    return columnNames.toArray( new String[ columnNames.size() ] );
  }

  public boolean matchColumnNames( String fileName, String[] names, List<Integer> outputIndexes ) throws IOException {
    File f = new File( fileName );
    HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) );
    HSSFSheet sheet = workbook.getSheetAt( 0 );
    HSSFRow row = sheet.getRow( 0 );
    for ( int i = 0; i < outputIndexes.size(); i++ ) {
      if ( !names[ outputIndexes.get( i ) ].equals( row.getCell( i ).getStringCellValue() ) ) {
        return false;
      }
    }
    return true;
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
