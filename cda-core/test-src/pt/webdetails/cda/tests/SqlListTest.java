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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;

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
import junit.framework.Assert;
import junit.framework.TestCase;

public class SqlListTest extends TestCase {
  private static final Log logger = LogFactory.getLog(SqlTest.class);

  public SqlListTest()
  {
    super();
  }

  public SqlListTest(final String name)
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
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-sql-list.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("status", new String[]{"Shipped","Cancelled"});
    queryOptions.setOutputType(ExporterEngine.OutputType.XML);

    logger.info("Shipped,Cancelled through string");
    engine.doQuery(out, cdaSettings, queryOptions);
    engine.doQuery(baos, cdaSettings, queryOptions);
    String testRes = new String(baos.toByteArray(),Charset.defaultCharset());
    assertTrue(testRes.contains("Shipped"));
    assertTrue(testRes.contains("Cancelled"));
    baos = new ByteArrayOutputStream();

    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("1");
    queryOptions.addParameter("status", new String[]{"Shipped","Cancelled"});
    queryOptions.setOutputType(ExporterEngine.OutputType.XML);

    logger.info("\nShipped,Cancelled through string[]");
    engine.doQuery(out, cdaSettings, queryOptions);
    engine.doQuery(baos, cdaSettings, queryOptions);
    testRes = new String(baos.toByteArray(),Charset.defaultCharset());
    assertTrue(testRes.contains("Shipped"));
    assertTrue(testRes.contains("Cancelled"));

  }
}
