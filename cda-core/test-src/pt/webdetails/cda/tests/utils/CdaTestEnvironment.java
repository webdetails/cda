package pt.webdetails.cda.tests.utils;

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

import java.util.Locale;
import java.util.TimeZone;

public class CdaTestEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {

  private CdaTestingContentAccessFactory factory;

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
    return new EHCacheQueryCache( false );
  }

  public IContentAccessFactory getRepo() {
    return factory;
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }
}
