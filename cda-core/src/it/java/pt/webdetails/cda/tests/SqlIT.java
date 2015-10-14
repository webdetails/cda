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
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;


/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 15, 2010 Time: 7:53:13 PM
 */
public class SqlIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( SqlIT.class );

  public SqlIT() {
    super();
  }

  public SqlIT( final String name ) {
    super( name );
  }


  public void testSqlQueryCache() throws Exception {
    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-sql.cda" );
    logger.debug( "Doing query on Cda - Initializing CdaEngine" );
    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );

    logger.info( "Doing first query" );
    engine.doQuery( cdaSettings, queryOptions );

    logger.info( "Doing query with different parameters" );
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2004-01-01" );
    engine.doQuery( cdaSettings, queryOptions );

    // Querying 2nd time to test cache
    logger.info( "Doing query using the initial parameters - Cache should be used" );
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "2003-04-01" );
    engine.doQuery( cdaSettings, queryOptions );

    //    // Querying 2nd time to test cache
    //    logger.info("Doing query again to see if cache expires"); //TODO: and how are we checking if it does?
    //    Thread.sleep(6000);
    //    engine.doQuery( cdaSettings, queryOptions );

  }

}
