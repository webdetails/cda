/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;

import javax.swing.table.TableModel;

public class KettleStringArrayParamIT extends CdaTestCase {

  private static final Log logger = LogFactory.getLog( KettleStringArrayParamIT.class );


  public void testKettleStringArray() throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( "sample-kettle-ParamArray.cda" );

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );

    queryOptions.setParameter( "countries", "Portugal;Germany" );
    queryOptions.setParameter( "Costumers", "307;369" );

    logger.info( "Doing query" );
    TableModel tm = doQuery( cdaSettings, queryOptions );
    assertEquals( 2, tm.getRowCount() );
    assertEquals( "307", tm.getValueAt( 0, 0 ).toString() );
    assertEquals( "Der Hund Imports", tm.getValueAt( 0, 1 ) );
  }
}
