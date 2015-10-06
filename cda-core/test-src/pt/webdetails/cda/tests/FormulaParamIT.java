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

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.tests.utils.CdaTestCase;
import pt.webdetails.cda.tests.utils.CdaTestEnvironment;
import pt.webdetails.cda.tests.utils.CdaTestingContentAccessFactory;

public class FormulaParamIT extends CdaTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CdaEngine.init( new FormulaTestEnvironment() );
  }

  public static class FormulaTestEnvironment extends CdaTestEnvironment {

    public FormulaTestEnvironment() throws InitializationException {
      super( new CdaTestingContentAccessFactory() );
    }

    public FormulaContext getFormulaContext() {
      return new TestFormulaContext();
    }
  }

  public static class TestFormulaContext extends DefaultFormulaContext {
    public Object resolveReference( Object name ) {
      if ( name instanceof String && StringUtils.startsWith( (String) name, "session:" ) ) {
        return "thisIsAGoodValue";
      }
      return super.resolveReference( name );
    }
  }

  @Test
  public void testParam() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( "sample-securityParam.cda" );

    QueryOptions queryOptions = new QueryOptions();

    queryOptions.setDataAccessId( "junitDataAccess" );
    TableModel tableModel = cdaSettings.getDataAccess( queryOptions.getDataAccessId() ).doQuery( queryOptions );
    assertEquals( 1, tableModel.getRowCount() );
    assertEquals( 1, tableModel.getColumnCount() );
    String result = (String) tableModel.getValueAt( 0, 0 );
    assertEquals( "thisIsAGoodValue", result );
  }

}
