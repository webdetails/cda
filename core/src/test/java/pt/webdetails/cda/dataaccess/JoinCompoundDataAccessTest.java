/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.dataaccess;


import org.dom4j.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metadata.model.concept.types.JoinType;
import pt.webdetails.cda.test.util.CdaTestHelper;
import pt.webdetails.cda.test.util.TableModelChecker;

import javax.swing.table.TableModel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

public class JoinCompoundDataAccessTest {

  @BeforeClass
  public static void init() {
    AbstractDataAccess.shutdownCache();
    initBareEngine( getMockEnvironment() );
  }

  @Test
  public void testVoidMergeINNER() throws Exception {

    JoinCompoundDataAccessForTest test = new JoinCompoundDataAccessForTest( JoinType.INNER );
    CdaTestHelper.SimpleTableModel left = new CdaTestHelper.SimpleTableModel( new Object[ 0 ][ 0 ] );
    CdaTestHelper.SimpleTableModel right = new CdaTestHelper.SimpleTableModel( new Object[] { "b1" } );

    TableModel result = test.voidMerge( left, right );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( result, left );

  }

  @Test
  public void testVoidMergeFULLOUTER() throws Exception {

    JoinCompoundDataAccessForTest test = new JoinCompoundDataAccessForTest( JoinType.FULL_OUTER );
    CdaTestHelper.SimpleTableModel left = new CdaTestHelper.SimpleTableModel( new Object[ 0 ][ 0 ] );
    CdaTestHelper.SimpleTableModel right = new CdaTestHelper.SimpleTableModel( new Object[] { "b1" } );

    TableModel result = test.voidMerge( left, right );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( result, right );

  }

  @Test
  public void testVoidMergeRIGHTOUTER() throws Exception {

    JoinCompoundDataAccessForTest test = new JoinCompoundDataAccessForTest( JoinType.RIGHT_OUTER );
    CdaTestHelper.SimpleTableModel left = new CdaTestHelper.SimpleTableModel( new Object[ 0 ][ 0 ] );
    CdaTestHelper.SimpleTableModel right = new CdaTestHelper.SimpleTableModel( new Object[] { "b1" } );
    TableModel result = test.voidMerge( left, right );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( result, right );
  }

  @Test
  public void testVoidMergeLEFTOUTER() throws Exception {

    JoinCompoundDataAccessForTest test = new JoinCompoundDataAccessForTest( JoinType.LEFT_OUTER );
    CdaTestHelper.SimpleTableModel left = new CdaTestHelper.SimpleTableModel( new Object[ 0 ][ 0 ] );
    CdaTestHelper.SimpleTableModel right = new CdaTestHelper.SimpleTableModel( new Object[] { "b1" } );
    TableModel result = test.voidMerge( left, right );
    TableModelChecker checker = new TableModelChecker();
    checker.assertEquals( result, left );

  }

  private class JoinCompoundDataAccessForTest extends JoinCompoundDataAccess {
    public JoinCompoundDataAccessForTest( JoinType joinType ) {
      this.joinType = joinType;
    }
  }
}
