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

package pt.webdetails.cda.filetests;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.cache.CacheKey;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

public class CacheKeysTest extends CdaTestCase {

  private static final String CDA_SAMPLE_FILE = "sample-user-defined-cacheKeys.cda";

  private static final String USER_DEFINED_CACHE_KEY = "foo";
  private static final String USER_DEFINED_CACHE_VALUE = "bar";
  private static final String SYSTEM_DEFINED_CACHE_KEY = "quux";
  private static final String SYSTEM_DEFINED_CACHE_VALUE = "baz";
  private static final String SYSTEM_AND_USER_DEFINED_CACHE_KEY = "norf";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CdaEngine.init( new FormulaTestEnvironment() );
  }

  @Test
  public void testUserAndSystemDefinedCacheKeys() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( CDA_SAMPLE_FILE );

    IQueryCache cache = getEnvironment().getQueryCache();
    cache.clearCache();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    cdaSettings.getDataAccess( queryOptions.getDataAccessId() ).doQuery( queryOptions );

    assertNotNull( cache.getKeys() );

    for ( TableCacheKey key : cache.getKeys() ) {

      assertNotNull( key );
      assertNotNull( key.getExtraCacheKey() );
      Assert.assertTrue( key.getExtraCacheKey() != null && key.getExtraCacheKey() instanceof CacheKey );
      Assert.assertTrue( ( (CacheKey) key.getExtraCacheKey() ).getKeyValuePairs() != null
        && ( (CacheKey) key.getExtraCacheKey() ).getKeyValuePairs().size() > 0 );

      boolean hasValueAsCacheExtraKey = false;
      boolean hasSystemWideExtraCacheKey = false;
      boolean systemCantOverrideUser = false;

      for ( CacheKey.KeyValuePair pair : ( (CacheKey) key.getExtraCacheKey() ).getKeyValuePairs() ) {
        if ( pair.getKey().equals( USER_DEFINED_CACHE_KEY ) && pair.getValue().equals( USER_DEFINED_CACHE_VALUE ) ) {
          hasValueAsCacheExtraKey = true;
        }
        if ( pair.getKey().equals( SYSTEM_DEFINED_CACHE_KEY )
          && pair.getValue().equals( SYSTEM_DEFINED_CACHE_VALUE ) ) {
          hasSystemWideExtraCacheKey = true;
        }
        if ( pair.getKey().equals( SYSTEM_AND_USER_DEFINED_CACHE_KEY )
          && pair.getValue().equals( USER_DEFINED_CACHE_VALUE ) ) {
          systemCantOverrideUser = true;
        }
      }
      // user defined cache keys
      Assert.assertTrue( hasValueAsCacheExtraKey );

      // system wide cache keys
      Assert.assertTrue( hasSystemWideExtraCacheKey );

      // system wide cache keys do not override user defined cache keys
      Assert.assertTrue( systemCantOverrideUser );

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

}
