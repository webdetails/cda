/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cda;

import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.utils.framework.PluginUtils;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public abstract class PentahoBaseCdaEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {
  private IQueryCache cacheImpl;

  //This is kept here for legacy reasons. CDC is writing over plugin.xml to 
  //switch cache types. It should be changed to change the cda.spring.xml.
  //While we don't, we just keep the old method for getting the cache
  @Override
  public IQueryCache getQueryCache() {
    try {
      if ( cacheImpl == null ) {
        cacheImpl = PluginUtils.getPluginBean( "cda.", IQueryCache.class );
      }
      return cacheImpl;
    } catch ( Exception e ) {
      logger.error( e.getMessage() );
    }

    return super.getQueryCache();
  }

  public IContentAccessFactory getRepo() {
    return CdaPluginEnvironment.repository();
  }

  /**
   * @return {@link CdaSessionFormulaContext}
   */
  @Override
  public FormulaContext getFormulaContext() {
    return new CdaSessionFormulaContext();
  }
}
