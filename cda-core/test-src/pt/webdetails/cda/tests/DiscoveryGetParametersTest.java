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
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class DiscoveryGetParametersTest extends TestCase
{

  private static final Log logger = LogFactory.getLog(DiscoveryGetParametersTest.class);

  public void testGetParameters() throws Exception
  {


    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-discovery.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing discovery on the file");
    final CdaEngine engine = CdaEngine.getInstance();


    // JSON

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setDataAccessId("2");
    discoveryOptions.setOutputType("xml");
    logger.info("Doing discovery, return xml");
    engine.listParameters(out, cdaSettings, discoveryOptions);

    // XML
    discoveryOptions.setOutputType("json");
    logger.info("Doing discovery, return json");
    engine.listParameters(out, cdaSettings, discoveryOptions);


  }

}
