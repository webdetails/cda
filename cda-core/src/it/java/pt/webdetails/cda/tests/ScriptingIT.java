/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

import org.junit.Test;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;


public class ScriptingIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( ScriptingIT.class );

  @Test
  public void testSqlQuery() throws Exception {

    // Define an outputStream
    final CdaSettings cdaSettings = parseSettingsFile( "sample-scripting.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.setOutputType( "json" );
    queryOptions.addParameter( "status", "Shipped" );

    logger.info( "Doing query" );
    doQuery( cdaSettings, queryOptions );
  }

  @Test
  public void testJsonQuery() throws Exception {

    // Define an outputStream
    final CdaSettings cdaSettings = parseSettingsFile( "sample-json-scripting.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "2" );
    queryOptions.setOutputType( "json" );

    logger.info( "Doing query" );
    doQuery( cdaSettings, queryOptions );
  }

}
