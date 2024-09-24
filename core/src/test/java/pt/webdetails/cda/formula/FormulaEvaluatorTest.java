/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.formula;

import junit.framework.TestCase;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import pt.webdetails.cda.utils.FormulaEvaluator;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormulaEvaluatorTest extends TestCase {

  private final String PROVIDER = "security:";
  private final String PARAM_KEY = "testObject";
  private final String FORMULA_TO_EVAL = "${[" + PROVIDER + PARAM_KEY + "]}";

  FormulaContext formulaContext;
  ICdaParameterProvider paramProvider;
  Map<String, ICdaParameterProvider> providers;

  public void setUp() throws Exception {
    super.setUp();

    formulaContext = new DefaultSessionFormulaContext( null );
    paramProvider = mock( ICdaParameterProvider.class );

    providers = new HashMap<String, ICdaParameterProvider>();
    providers.put( PROVIDER, paramProvider );

    ( (DefaultSessionFormulaContext) formulaContext ).setProviders( providers );
  }

  public void testFormulaEvaluatorHandlesSimpleObject() throws Exception {

    Object simpleObject = "Simple_Value";

    when( paramProvider.getParameter( PARAM_KEY ) ).thenReturn( simpleObject );

    String replacedFormula = FormulaEvaluator.replaceFormula( FORMULA_TO_EVAL, formulaContext );

    assertTrue( replacedFormula != null && !replacedFormula.isEmpty() );
    assertTrue( replacedFormula.equals( "Simple_Value" ) );
  }

  public void testFormulaEvaluatorHandlesObjectArray() throws Exception {

    Object objectArray = new ArrayList<String>();
    ( (ArrayList<String>) objectArray ).add( "Value_01" );
    ( (ArrayList<String>) objectArray ).add( "Value_02" );
    ( (ArrayList<String>) objectArray ).add( "Value_03" );

    when( paramProvider.getParameter( PARAM_KEY ) ).thenReturn( objectArray );

    String replacedFormula = FormulaEvaluator.replaceFormula( FORMULA_TO_EVAL, formulaContext );

    assertTrue( replacedFormula != null && !replacedFormula.isEmpty() );
    assertTrue( replacedFormula.equals( "Value_01,Value_02,Value_03" ) );
  }

  protected void tearDown() throws Exception {
    formulaContext = null;
    paramProvider = null;
    providers = null;
  }
}
