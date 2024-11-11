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


package pt.webdetails.cda.filetests;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.datafactory.DesignTimeDataFactoryContext;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.BaseCdaEnvironment;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.IUserSession;

import java.util.Locale;
import java.util.TimeZone;

public class CdaTestEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {

  private CdaTestingContentAccessFactory factory;
  private IQueryCache cache;
  private boolean canCreateContent;
  private IUserSession mockedUserSession;

  public CdaTestEnvironment( CdaTestingContentAccessFactory factory ) throws InitializationException {
    super( new AbstractBeanFactory() {
      @Override
      public String getSpringXMLFilename() {
        return "cda.spring.xml";
      }
    } );
    this.factory = factory;
  }

  public void initializeDataFactory( DataFactory dataFactory, Configuration configuration, ResourceKey contextKey,
                                     ResourceManager resourceManager ) throws ReportDataFactoryException {
    dataFactory.initialize( new DesignTimeDataFactoryContext( configuration, resourceManager, contextKey,
      new LibLoaderResourceBundleFactory( resourceManager, contextKey, Locale.getDefault(),
        TimeZone.getDefault() ), dataFactory ) );
  }

  @Override
  public IQueryCache getQueryCache() {
    synchronized ( this ) {
      if ( cache == null ) {
        cache = new EHCacheQueryCache( false );
      }
      return cache;
    }
  }

  public void setQueryCache( IQueryCache cache ) {
    this.cache = cache;
  }

  public IContentAccessFactory getRepo() {
    return factory;
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }

  public void setMockedUserSession( IUserSession mockedUserSession ) {
    this.mockedUserSession = mockedUserSession;
  }

  public void setCanCreateContent( boolean canCreateContent ) {
    this.canCreateContent = canCreateContent;
  }

  public IUserSession getUserSession() {
    return mockedUserSession;
  }

  public boolean canCreateContent() {
    return canCreateContent;
  }
}
