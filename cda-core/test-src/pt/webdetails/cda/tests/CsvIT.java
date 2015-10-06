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

import java.io.OutputStream;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.exporter.CsvExporter;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 15, 2010 Time: 7:53:13 PM
 */
public class CsvIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( CsvIT.class );

  public void testCsvExport() throws Exception {
    // Define an outputStream
    OutputStream out = System.out;

    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );
    // queryOptions.addParameter("status","In Process");
    TableModel table = engine.doQuery( cdaSettings, queryOptions );

    logger.info( "Doing csv export" );
    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );
    final TableExporter csvExporter = engine.getExporter( queryOptions );
    csvExporter.export( out, table );

    logger.info( "Doing xml export" );
    queryOptions.setOutputType( "xml" );
    final TableExporter xmlExporter = engine.getExporter( queryOptions );
    xmlExporter.export( out, table );

    logger.info( "Doing json export" );
    queryOptions.setOutputType( "json" );
    final TableExporter jsonExporter = engine.getExporter( queryOptions );
    jsonExporter.export( out, table );

    logger.info( "Doing xls export" );
    queryOptions.setOutputType( "xls" );
    final TableExporter xlsExporter = engine.getExporter( queryOptions );
    xlsExporter.export( out, table );
  }

  public void testStreamingCsvExport() throws Exception {
    OutputStream out = System.out;

    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );

    logger.info( "Doing streaming csv export" );
    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );

    logger.info( "Doing streaming xls export" );
    queryOptions.setOutputType( "xls" );
    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );
  }
}
