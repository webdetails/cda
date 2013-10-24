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

package pt.webdetails.cda;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.Util;

/**
 * TODO: get tests outta here
 * Main class to test and execute the CDA in standalone mode
 * User: pedro
 * Date: Feb 1, 2010
 * Time: 12:30:41 PM
 */
public class CdaExecutor
{

  private static final Log logger = LogFactory.getLog(CdaExecutor.class);
  private static CdaExecutor _instance;


  protected CdaExecutor()
  {

    logger.debug("Initializing CdaExecutor");


  }

  public static void main(final String[] args)
  {


    final CdaExecutor cdaExecutor = CdaExecutor.getInstance();

    cdaExecutor.doQuery();

  }

  private void doQuery()
  {


    try
    {

      // Init CDA      
      CdaBoot.getInstance().start();


      // Define an outputStream
      OutputStream out = System.out;

      // This will test standard query execution
      //testQueryExecution(out);


      // This will test the block creation
      testBlocks(out);


    }
    catch (DocumentException e)
    {
      logger.fatal("Unable to parse settings dom: " + Util.getExceptionDescription(e));
    }
    catch (UnsupportedConnectionException e)
    {
      logger.fatal("ConnectionException " + Util.getExceptionDescription(e));
    }
    catch (UnsupportedDataAccessException e)
    {
      logger.fatal("DataAccessException " + Util.getExceptionDescription(e));
    }
    catch (UnknownDataAccessException e)
    {
      logger.fatal("DataAccess id not found " + Util.getExceptionDescription(e));
    }
    catch (QueryException e)
    {
      logger.fatal("QueryException " + Util.getExceptionDescription(e));
    }
    catch (UnsupportedExporterException e)
    {
      logger.fatal("UnsupportedExporterException " + Util.getExceptionDescription(e));
    }
    catch (ExporterException e)
    {
      logger.fatal("ExporterException " + Util.getExceptionDescription(e));
    }


  }

  private void testBlocks(final OutputStream out)
      throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException, UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {

    logger.info("Testing CDA file interaction through blocks");
    final SettingsManager settingsManager = SettingsManager.getInstance();

    final File settingsFile = new File("samples/sample-gen.cda");
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());


    testSingleSqlQuery(out, cdaSettings);

  }

//  private void testQueryExecution(final OutputStream out)
//      throws DocumentException, UnsupportedConnectionException, UnsupportedDataAccessException, UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
//  {
//
//    logger.info("Building CDA settings from sample file");
//
//    final SettingsManager settingsManager = SettingsManager.getInstance();
//
//    final File settingsFile = new File("samples/sample.cda");
//    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
//
//    testSqlQuery(out, cdaSettings);
//
//    //testMondrianQuery(out, cdaSettings);
//
//  }


//  private void testSqlQuery(final OutputStream out, final CdaSettings cdaSettings)
//      throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
//  {
//    logger.debug("Doing query on Cda - Initializing CdaEngine");
//    final CdaEngine engine = CdaEngine.getInstance();
//
//    QueryOptions queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("1");
//    queryOptions.addParameter("orderDate", "2003-04-01");
//    queryOptions.setOutputType("csv");
//    // queryOptions.addParameter("status","In Process");
//
//    logger.info("Doing first query");
//    engine.doQuery(out, cdaSettings, queryOptions);
//
//    logger.info("Doing query with different parameters");
//    queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("1");
//    queryOptions.addParameter("orderDate", "2004-01-01");
//    engine.doQuery(out, cdaSettings, queryOptions);
//
//    // Querying 2nd time to test cache
//    logger.info("Doing query using the initial parameters - Cache should be used");
//    queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("1");
//    queryOptions.addParameter("orderDate", "2003-04-01");
//    engine.doQuery(out, cdaSettings, queryOptions);
//
//    // Querying 2nd time to test cache
//    logger.info("Doing query again to see if cache expires");
//    try
//    {
//      Thread.sleep(6000);
//    }
//    catch (InterruptedException e)
//    {
//      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//    }
//    engine.doQuery(out, cdaSettings, queryOptions);
//  }

//
  private void testSingleSqlQuery(final OutputStream out, final CdaSettings cdaSettings) 
      throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();
    
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2003-04-01");
    queryOptions.setOutputType("csv");

    engine.getExporter( queryOptions ).export( out, engine.doQuery( cdaSettings, queryOptions ) );
  }


//  private void testMondrianQuery(final OutputStream out, final CdaSettings cdaSettings)
//      throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
//  {
//    logger.debug("Doing query on Cda - Initializing CdaEngine");
//    final CdaEngine engine = CdaEngine.getInstance();
//
//    QueryOptions queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("2");
//    queryOptions.setOutputType("json");
//    queryOptions.addParameter("status", "Shipped");
//
//    logger.info("Doing query");
//    engine.doQuery(out, cdaSettings, queryOptions);
//
//  }


  public static synchronized CdaExecutor getInstance()
  {

    if (_instance == null)
    {
      _instance = new CdaExecutor();
    }

    return _instance;
  }

}
