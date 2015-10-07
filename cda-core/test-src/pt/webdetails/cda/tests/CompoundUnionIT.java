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

import javax.swing.table.TableModel;

import org.junit.Test;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;
import pt.webdetails.cda.tests.utils.CdaTestHelper;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 15, 2010 Time: 7:53:13 PM
 */
public class CompoundUnionIT extends CdaTestCase {

  @Test
  public void testCompoundQuery() throws Exception {

    final CdaSettings cdaSettings = getSettingsManager().parseSettingsFile( "sample-union.cda" );

    QueryOptions queryOptions = new QueryOptions();
    //queryOptions.addParameter("year","2005");

    queryOptions.setDataAccessId( "1" );
    final TableModel top = getEngine().doQuery( cdaSettings, queryOptions );

    queryOptions.setDataAccessId( "2" );
    final TableModel bottom = getEngine().doQuery( cdaSettings, queryOptions );

    queryOptions.setDataAccessId( "3" );
    queryOptions.setOutputType( "json" );
    // queryOptions.addParameter("status","In Process");


    final TableModel union = getEngine().doQuery( cdaSettings, queryOptions );

    assertEquals( union.getColumnCount(), Math.max( top.getColumnCount(), bottom.getColumnCount() ) );
    assertEquals( union.getRowCount(), top.getRowCount() + bottom.getRowCount() );

    assertEquals( union.getValueAt( 0, 0 ), top.getValueAt( 0, 0 ) );
    assertEquals( union.getValueAt( 0, 1 ), top.getValueAt( 0, 1 ) );
    final int lastRowUnion = union.getRowCount() - 1;
    final int lastRowBottom = bottom.getRowCount() - 1;
    assertTrue( CdaTestHelper.numericEquals(
      union.getValueAt( lastRowUnion, 1 ).toString(),
      bottom.getValueAt( lastRowBottom, 1 ).toString(), 0.000001 ) );
  }

}
