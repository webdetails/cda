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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.CsvExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CsvXslFromSQLTest extends CdaTestCase {

  public void setUp() throws Exception {
    PluginRegistry.init( true );
    StepPluginType.getInstance().handlePluginAnnotation(
      ExcelOutputMeta.class,
      ExcelOutputMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
    super.setUp();
  }

  public void testCsvXlsFromSQLExport() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-CsvXslFromSQLTest.cda" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "Ds1" );

    String fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestCSV.csv";
    OutputStream out = new FileOutputStream( fileName );
    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );

    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    assertEquals( countCSVColumns( fileName ), 2 );

    fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestXLS.xls";
    out = new FileOutputStream( fileName );
    queryOptions.setOutputType( "xls" );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    assertEquals( countXLSColumns( fileName ), 2 );
  }

  public void testCsvXlsFromSQLExportWithElevenParameters() throws Exception {
    //[CDA-112] - This test makes sure that it is possible to export with more than 10 parameters
    final CdaSettings cdaSettings = parseSettingsFile( "sample-CsvXslFromSQLWithElevenParametersTest.cda" );
    final CdaEngine engine = CdaEngine.getInstance();
    final int numberOfParameters = 11;

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "Ds2" );
    for ( int i = 1; i < numberOfParameters; i++ ) {
      queryOptions.setParameter( "parameter" + i, "1" );
    }

    String fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestCSV.csv";
    OutputStream out = new FileOutputStream( fileName );
    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );
    // (ExportedTableQueryResult)
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    assertEquals( countCSVColumns( fileName ), 2 );

    fileName = System.getProperty( "java.io.tmpdir" ) + File.separator + "TestXLS.xls";
    out = new FileOutputStream( fileName );
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

  protected String[] extractColumnNames( String fileName ) throws IOException {
    ArrayList<String> columnNames = new ArrayList<String>();
    File f = new File( fileName );
    try ( HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) ) ) {
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
  }

  protected boolean matchColumnNames( String fileName, String[] names, List<Integer> outputIndexes )
    throws IOException {
    File f = new File( fileName );
    try ( HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) ) ) {
      HSSFSheet sheet = workbook.getSheetAt( 0 );
      HSSFRow row = sheet.getRow( 0 );
      for ( int i = 0; i < outputIndexes.size(); i++ ) {
        if ( !names[ outputIndexes.get( i ) ].equals( row.getCell( i ).getStringCellValue() ) ) {
          return false;
        }
      }
      return true;
    }
  }

  protected int countXLSColumns( String filename ) throws IOException {

    File f = new File( filename );
    try ( HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( f ) ) ) {
      HSSFSheet sheet = workbook.getSheetAt( 0 );
      int noOfColumns = sheet.getRow( 0 ).getPhysicalNumberOfCells();
      return noOfColumns;
    } finally {
      f.delete();
    }
  }

  protected int countCSVColumns( String filename ) throws IOException {
    int count = 0;
    File f = new File( filename );
    try ( BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) ) ) {
      String line;
      if ( ( line = br.readLine() ) != null ) {
        count = line.split( "," ).length;
      }
      return count;
    } finally {
      f.delete();
    }
  }
}
