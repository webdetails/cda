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
public class KettleTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(KettleTest.class);

  public void testSqlQuery() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-kettle.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("2");
    queryOptions.setOutputType("json");
    queryOptions.addParameter("myRadius", "10");
    queryOptions.addParameter("ZipCode", "90210");

    logger.info("Doing query");
    engine.doQuery(out, cdaSettings, queryOptions);


  }

}
