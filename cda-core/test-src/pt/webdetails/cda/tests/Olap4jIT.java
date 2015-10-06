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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.dataaccess.Olap4JDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;


/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class Olap4jIT extends CdaTestCase
{

  private static final Log logger = LogFactory.getLog(Olap4jIT.class);

  public Olap4jIT()
  {
    super();
  }

  public Olap4jIT( final String name )
  {
    super(name);
  }

  public void testOlap4jQuery() throws Exception
  {

    final CdaSettings cdaSettings = parseSettingsFile("sample-olap4j.cda");
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("2");
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipped");

    logger.info("Doing query");
    engine.doQuery(cdaSettings, queryOptions);
    logger.info("\nChecking cache");
    boolean hasCash = false;
    
    String query = ((Olap4JDataAccess) cdaSettings.getDataAccess("2")).getQuery();
    IQueryCache cache = getEnvironment().getQueryCache();
    logger.info("Cache cleared!");
    cache.clearCache();
    engine.doQuery(cdaSettings, queryOptions);
    engine.doQuery(cdaSettings, queryOptions);
    for (TableCacheKey key : cache.getKeys()) {
	   assertEquals(key.getQuery(), query);
	   logger.info("Found query in cache! Query:" + query);
	   hasCash = true;
    }
    assertTrue(hasCash);
    logger.info("Found Query in Cache!");

  }

  /**
   * this test is the same as testOlap4jQuery(), but now calls for a new data-access id 3, which in its turn
   * is set to use the new connection id 3, that has been defined to use new type 'olap4j.defaultolap4j'
   * @throws Exception should something go wrong
   */
  public void testDefaultOlap4jQuery() throws Exception
  {

    final CdaSettings cdaSettings = parseSettingsFile("sample-olap4j.cda");
    logger.debug("Doing query on Cda - Initializing CdaEngine");
    final CdaEngine engine = CdaEngine.getInstance();

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId("3"); // same as data-access id 2, but using new connection type 'olap4j.defaultolap4j'
    queryOptions.setOutputType("json");
    queryOptions.addParameter("status", "Shipped");

    logger.info("Doing query");
    engine.doQuery(cdaSettings, queryOptions);
  }
}
