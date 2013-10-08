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

import net.sf.ehcache.CacheManager;


import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.BeforeClass;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.CoreBeanFactory;
import pt.webdetails.cda.DefaultCdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CompoundJoinTest
{

  private static final Log logger = LogFactory.getLog(CompoundJoinTest.class);

  @BeforeClass
  public static void setUp() throws InitializationException{
      CoreBeanFactory cbf = new CoreBeanFactory("cda.spring.xml");
      DefaultCdaEnvironment env = new DefaultCdaEnvironment(cbf);
      CdaEngine.init(env);
  }
  
  @Test
  public void testCompoundQuery() throws Exception
  {
try{
    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");
    final SettingsManager settingsManager = SettingsManager.getInstance();
   // String fileString = CdaEngine.getEnvironment().getRepositoryAccess().getResourceAsString("sample-join.cda");
    
   // Assert.assertNotNull(fileString);
    
    URL file = this.getClass().getResource("sample-join.cda");
    //Assert.assertNotNull(file);//could get file  XXX currently failing
    
    File settingsFile = new File(file.toURI());
    
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.error("Doing query on Cda - Initializing CdaEngine");
    

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("3");
    queryOptions.setOutputType("json");
    //queryOptions.addParameter("status","In Process");
    logger.info("Doing query");

    CdaEngine.getInstance().doQuery(out, cdaSettings, queryOptions);
}catch(Exception e){
    e.printStackTrace();
    Assert.fail();
}


  }

}
