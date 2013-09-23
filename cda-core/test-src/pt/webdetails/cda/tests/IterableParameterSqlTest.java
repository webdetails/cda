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

public class IterableParameterSqlTest extends TestCase {
  private static final Log logger = LogFactory.getLog(IterableParameterSqlTest.class);

  public void testIterateStatus() throws Exception
  {
  	//Configuration configuration = new Configuration();
    // Define an outputStream
    OutputStream out = System.out;

    logger.info("Building CDA settings from sample file");

    final SettingsManager settingsManager = SettingsManager.getInstance();
    URL file = this.getClass().getResource("sample-iterable-sql.cda");
    File settingsFile = new File(file.toURI());
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();
    QueryOptions queryOptions;

    
    for(int i=0; i<2;i++){
    	queryOptions = new QueryOptions();
	    queryOptions.setDataAccessId("1");
	    queryOptions.addParameter("status", "$FOREACH(2,0)");
	    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2003)");
	    queryOptions.getParameter("year").setDefaultValue("2004");
	    queryOptions.setOutputType("csv");
	    // queryOptions.addParameter("status","In Process");
	
	    logger.info("Doing first query");
	    try {
	    engine.doQuery(out, cdaSettings, queryOptions);
	    } catch(Exception e){
	    	throw e;
	    }
    }
    
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("4");
    queryOptions.addParameter("status", "$FOREACH(2,0)");
    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2525)");//no results
    queryOptions.getParameter("year").setDefaultValue("2004");//this time should fallback to default
    queryOptions.setOutputType("csv");

    logger.info("Doing second query");
    try {
    engine.doQuery(out, cdaSettings, queryOptions);
    } catch(Exception e){
    	throw e;
    }
    
//    queryOptions = new QueryOptions();
//    queryOptions.setDataAccessId("1");
//    queryOptions.addParameter("status", "$FOREACH(2,0)");
//    queryOptions.addParameter("year", "$FOREACH(3,0,minYear=2525)");
//    queryOptions.getParameter("year").setDefaultValue("2004");//this time will fallback to default
//    queryOptions.setOutputType("csv");
//    // queryOptions.addParameter("status","In Process");
//
//    logger.info("Doing third query");
//    try {
//    engine.doQuery(out, cdaSettings, queryOptions);
//    } catch(Exception e){
//    	throw e;
//    }
    logger.info("FIN");
  }
}
