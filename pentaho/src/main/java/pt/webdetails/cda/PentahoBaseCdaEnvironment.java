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


package pt.webdetails.cda;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.utils.framework.PluginUtils;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.session.PentahoSessionUtils;

public abstract class PentahoBaseCdaEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {
  private IQueryCache cacheImpl;
  private IAuthorizationPolicy authorizationPolicy;

  @Override
  public void init( IBeanFactory beanFactory ) {
    super.init( beanFactory );
    if ( beanFactory.containsBean( IAuthorizationPolicy.class.getSimpleName() ) ) {
      authorizationPolicy = (IAuthorizationPolicy) beanFactory.getBean( IAuthorizationPolicy.class.getSimpleName() );
    }
  }

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

  public IUserSession getUserSession() {
    return new PentahoSessionUtils().getCurrentSession();
  }

  public boolean canCreateContent() {
    if ( authorizationPolicy == null ) {
      authorizationPolicy = PentahoSystem.get( IAuthorizationPolicy.class );
      if ( authorizationPolicy == null ) {
        logger.warn( "Couldn't retrieve Authorization Policy" );
        return getUserSession().isAdministrator();
      }
    }
    return authorizationPolicy.isAllowed( RepositoryCreateAction.NAME );
  }
}
