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

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class SortTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(SortTest.class);

  public void testSqlQuery() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-mondrian-compact.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipped");

    logger.info("Doing query with 2 column sort");
    queryOptions.setDataAccessId("1");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"0D" , "1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with no sort");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with all combinations");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"0D" , "2", "1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("\nDoing query with only one sort");
    queryOptions.setSortBy(new ArrayList(Arrays.asList( new String[]{"1A"})));
    engine.doQuery(out, cdaSettings, queryOptions);

  }

  /*
  public void testJndiQuery() throws ExporterException, UnknownDataAccessException, UnsupportedExporterException, QueryException, UnsupportedConnectionException, DocumentException, UnsupportedDataAccessException
  {


  // Define an outputStream
  OutputStream out = System.out;

  logger.info("Building CDA settings from sample file");

  final SettingsManager settingsManager = SettingsManager.getInstance();

  final File settingsFile = new File("test/pt/webdetails/cda/tests/sample-mondrian-jndi.cda");
  final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
  logger.debug("Doing query on Cda - Initializing CdaEngine");
  final CdaEngine engine = CdaEngine.getInstance();

  QueryOptions queryOptions = new QueryOptions();
  queryOptions.setDataAccessId("2");
  queryOptions.setOutputType("json");
  queryOptions.addParameter("status", "Shipped");

  logger.info("Doing query");
  engine.doQuery(out, cdaSettings, queryOptions);


  }
   * 
   */
}
