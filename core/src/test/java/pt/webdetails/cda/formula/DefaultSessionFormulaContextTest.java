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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class DefaultSessionFormulaContextTest extends TestCase {

  public void setUp() throws Exception {
    super.setUp();

  }

  public void testContructor() throws Exception {
    DefaultSessionFormulaContext sessionFormulaContext;

    sessionFormulaContext = new DefaultSessionFormulaContext( null );
    Map<String, ICdaParameterProvider> providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertEquals( providers.size(), 1 );
    assertTrue( providers.get( "system:" ) instanceof CdaSystemParameterProvider );

    Map<String, ICdaParameterProvider> inputProviders = new HashMap<String, ICdaParameterProvider>();
    sessionFormulaContext = new DefaultSessionFormulaContext( inputProviders );
    providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertEquals( providers.size(), 1 );
    assertTrue( providers.get( "system:" ) instanceof CdaSystemParameterProvider );

    inputProviders.put( "session:", mock( ICdaParameterProvider.class ) );
    inputProviders.put( "security:", mock( ICdaParameterProvider.class ) );
    sessionFormulaContext = new DefaultSessionFormulaContext( inputProviders );
    providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertEquals( providers.size(), 2 );
    assertTrue( providers.get( "session:" ) instanceof ICdaParameterProvider );
    assertTrue( providers.get( "security:" ) instanceof ICdaParameterProvider );
  }

  public void testGetProviders() throws Exception {
    DefaultSessionFormulaContext sessionFormulaContext = new DefaultSessionFormulaContext( null );
    Map<String, ICdaParameterProvider> providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertNotNull( providers );
    assertTrue( providers.get( "system:" ) instanceof CdaSystemParameterProvider );
  }

  public void testSetProviders() throws Exception {
    DefaultSessionFormulaContext sessionFormulaContext = new DefaultSessionFormulaContext( null );
    Map<String, ICdaParameterProvider> providers = null;

    sessionFormulaContext.setProviders( providers );
    providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertEquals( providers.size(), 1 );
    assertTrue( providers.get( "system:" ) instanceof CdaSystemParameterProvider );

    providers = new HashMap<String, ICdaParameterProvider>();
    providers.put( "session:", mock( ICdaParameterProvider.class ) );
    providers.put( "security:", mock( ICdaParameterProvider.class ) );
    sessionFormulaContext.setProviders( providers );

    providers = sessionFormulaContext.getProviders();
    assertNotNull( providers );
    assertEquals( providers.size(), 3 );
    assertTrue( providers.get( "system:" ) instanceof CdaSystemParameterProvider );
    assertTrue( providers.get( "session:" ) instanceof ICdaParameterProvider );
    assertTrue( providers.get( "security:" ) instanceof ICdaParameterProvider );
  }

  public void testResolveReference() throws Exception {
    DefaultSessionFormulaContext sessionFormulaContext = new DefaultSessionFormulaContext( null );
    String[] test = new String[ 2 ];
    assertNull( sessionFormulaContext.resolveReference( test ) );

    assertNotNull( sessionFormulaContext.resolveReference( "system:java.vendor" ) );
    assertNull( sessionFormulaContext.resolveReference( "session:user" ) );
  }
}
