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

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

public class SqlFormulaIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( SqlIT.class );

  public SqlFormulaIT() {
    super();
  }

  public SqlFormulaIT( final String name ) {
    super( name );
  }


  public void testFormulaCacheSql() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-sql-formula.cda" );

    final CdaEngine engine = getEngine();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "${TODAY()}" );
    queryOptions.setOutputType( "csv" );

    logger.info( "Doing first query --> TODAY()" );
    engine.doQuery( cdaSettings, queryOptions );

    logger.info( "Doing query with different parameters" );
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    queryOptions.addParameter( "orderDate", "${DATE(2004;1;1)}" );
    engine.doQuery( cdaSettings, queryOptions );

    // Querying 2nd time to test cache (formula translated before cache check)
    logger.info( "Doing query using manual TODAY - Cache should be used" );
    queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    Calendar cal = Calendar.getInstance();
    queryOptions.addParameter( "orderDate",
      "${DATE(" + cal.get( Calendar.YEAR ) + ";" + ( cal.get( Calendar.MONTH ) + 1 ) + ";" + cal
        .get( Calendar.DAY_OF_MONTH ) + ")}" );
    engine.doQuery( cdaSettings, queryOptions );

  }

}
