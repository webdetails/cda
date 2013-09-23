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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;


/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class SqlTest extends TestCase
{

	private static final Log logger = LogFactory.getLog(SqlTest.class);

  public SqlTest()
  {
    super();
  }

  public SqlTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {

    CdaBoot.getInstance().start();

    super.setUp();
  }


  public void testSqlQuery() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-sql.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2003-04-01");
    queryOptions.setOutputType(ExporterEngine.OutputType.XML);
    // queryOptions.addParameter("status","In Process");

    logger.info("Doing first query");
    engine.doQuery(out, cdaSettings, queryOptions);

    logger.info("Doing query with different parameters");
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2004-01-01");
    engine.doQuery(out, cdaSettings, queryOptions);

    // Querying 2nd time to test cache
    logger.info("Doing query using the initial parameters - Cache should be used");
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("orderDate", "2003-04-01");
    engine.doQuery(out, cdaSettings, queryOptions);

    // Querying 2nd time to test cache
    logger.info("Doing query again to see if cache expires");
    try
    {
      Thread.sleep(6000);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    engine.doQuery(out, cdaSettings, queryOptions);

  }

}
