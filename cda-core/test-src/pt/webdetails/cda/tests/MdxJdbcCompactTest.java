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

import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class MdxJdbcCompactTest extends CdaTestCase
{

  private static final Log logger = LogFactory.getLog(MdxJdbcCompactTest.class);

  public void testSqlQuery() throws Exception
  {

    final CdaSettings cdaSettings = parseSettingsFile("sample-mondrian-compact.cda");

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipped");

    logger.info("Doing query 1");
    queryOptions.setDataAccessId("1");
    doQuery(cdaSettings, queryOptions);

    logger.info("\nDoing query 2");
    queryOptions.setDataAccessId("2");
    doQuery(cdaSettings, queryOptions);

    logger.info("\nDoing query 3");
    queryOptions.setDataAccessId("3");
    doQuery(cdaSettings, queryOptions);

    logger.info("\nDoing query 4");
    queryOptions.setDataAccessId("4");
    doQuery(cdaSettings, queryOptions);

  }
  
  public void testMdxExceptionHandling() throws Exception
  {

    final CdaSettings cdaSettings = parseSettingsFile("sample-mondrian-compact.cda");

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipperyship");

    logger.info("Doing query 1");
    queryOptions.setDataAccessId("1");
    boolean failed = false;
    try {
    	doQuery(cdaSettings, queryOptions);
    } catch (Exception e) {
		String msg = ExceptionUtils.getRootCauseMessage(e.getCause());
		Assert.assertEquals("MondrianException: Mondrian Error:MDX object '[Order Status].[Shipperyship]' not found in cube 'SteelWheelsSales'", msg);
		failed = true;
    }
    // Did it fail as expected?
    Assert.assertEquals(failed, true);
    
    
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
