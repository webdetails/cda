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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import javax.swing.table.TableModel;

public class UserDefinedCacheKeysTest extends CdaTestCase {

  private static final String CDA_SAMPLE_FILE = "sample-user-defined-cacheKeys.cda";

  private static final String CACHE_KEY = "foo";
  private static final String CACHE_VALUE = "bar";

  private static final Log logger = LogFactory.getLog( UserDefinedCacheKeysTest.class );

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CdaEngine.init( new FormulaTestEnvironment() );
  }

  @Test
  public void testUserDefinedCacheKeys() throws Exception {

    final CdaSettings cdaSettings = parseSettingsFile( CDA_SAMPLE_FILE );

    IQueryCache cache = getEnvironment().getQueryCache();
    logger.info( "Cache cleared." );
    cache.clearCache();

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "1" );
    logger.info(
      "Performing query with id=1 @ " + cdaSettings.getDataAccess( queryOptions.getDataAccessId() )
        .toString()
    );
    TableModel tableModel = cdaSettings.getDataAccess( queryOptions.getDataAccessId() ).doQuery( queryOptions );
    logger.info( "query done" );

    assertNotNull( cache.getKeys() );
    logger.info( "Query was cached" );

    for ( TableCacheKey key : cache.getKeys() ) {

      assertNotNull( key );
      logger.info( "key: " + key.toString() );
      assertNotNull( key.getExtraCacheKey() );
      Assert.assertTrue( key.getExtraCacheKey() != null && key.getExtraCacheKey() instanceof CacheKey );
      Assert.assertTrue( ( ( CacheKey ) key.getExtraCacheKey() ).getKeyValuePairs() != null
        && ( ( CacheKey ) key.getExtraCacheKey() ).getKeyValuePairs().size() > 0  );

      boolean hasValueAsCacheExtraKey = false;

      logger.info( "Iterating extra cache keys.." );
      for ( CacheKey.KeyValuePair pair : ( ( CacheKey ) key.getExtraCacheKey() ).getKeyValuePairs() ) {
        logger.info( "key: " + pair.toString() );
        if( pair.getKey().equals( CACHE_KEY ) && pair.getValue().equals( CACHE_VALUE ) ){
          hasValueAsCacheExtraKey = true;
        }
      }

      Assert.assertTrue( hasValueAsCacheExtraKey );

    }
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
      if ( name instanceof String && "session:value".equals( (String) name ) ) {
        return "thisIsAGoodValue";
      }
      return super.resolveReference( name );
    }
  }

}
