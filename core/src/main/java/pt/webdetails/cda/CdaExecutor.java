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


package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.CdaSettingsReadException;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.Util;

import java.io.File;
import java.io.OutputStream;

/**
 * TODO: get tests outta here TODO: what is this? is it used? Main class to test and execute the CDA in standalone mode
 * User: pedro Date: Feb 1, 2010 Time: 12:30:41 PM
 */
@Deprecated
public class CdaExecutor {

  private static final Log logger = LogFactory.getLog( CdaExecutor.class );
  private static CdaExecutor _instance = new CdaExecutor();

  protected CdaExecutor() {

    logger.debug( "Initializing CdaExecutor" );

  }

  public static void main( final String[] args ) {

    final CdaExecutor cdaExecutor = CdaExecutor.getInstance();

    cdaExecutor.doQuery();

  }

  private void doQuery() {

    try {

      // Init CDA TODO
      CdaBoot.getInstance().start();

      // Define an outputStream
      OutputStream out = System.out;

      // This will test standard query execution
      // testQueryExecution(out);

      // This will test the block creation
      testBlocks( out );

    } catch ( ExporterException e ) {
      logger.fatal( "ExporterException " + Util.getExceptionDescription( e ) );
    } catch ( AccessDeniedException e ) {
      logger.error( "Access denied " + Util.getExceptionDescription( e ) );
    } catch ( Exception e ) {
      logger.fatal( e.getLocalizedMessage() + ": " + Util.getExceptionDescription( e ) );
    }

  }

  private void testBlocks( final OutputStream out ) throws CdaSettingsReadException, UnknownDataAccessException,
    QueryException, UnsupportedExporterException, ExporterException, AccessDeniedException {

    logger.info( "Testing CDA file interaction through blocks" );
    final SettingsManager settingsManager = CdaEngine.getInstance().getSettingsManager();

    final File settingsFile = new File( "samples/sample-gen.cda" );
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile( settingsFile.getAbsolutePath() );

    testSingleSqlQuery( out, cdaSettings );

  }

  private void testSingleSqlQuery( final OutputStream out, final CdaSettings cdaSettings )
    throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException {
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );
    queryOptions.setOutputType( "csv" );

    engine.doExportQuery( cdaSettings, queryOptions ).writeOut( out );
  }

  public static CdaExecutor getInstance() {
    return _instance;
  }

}
