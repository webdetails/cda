package pt.webdetails.cda.tests;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.*;
import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

import javax.swing.table.TableModel;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.TimeZone;

//import org.pentaho.reporting.libraries.base.config.Configuration;
//import org.pentaho.reporting.libraries.base.config.HierarchicalConfiguration;


public abstract class CdaTestCase extends TestCase {

  private CdaTestEnvironment testEnvironment;

  public CdaTestCase ( String name ) {
    super( name );
  }

  public CdaTestCase () {
  }

  protected void setUp() throws Exception {
    super.setUp();
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    log().info("factory:" + factory);
    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment(factory) );

    // cda-specific environment
    testEnvironment = new CdaTestEnvironment(factory);
    // cda init
    CdaEngine.init( testEnvironment );
  }

  protected Log log() {
    return LogFactory.getLog( getClass() );
  }

  protected SettingsManager getSettingsManager() {
    return getEngine().getSettingsManager();
  }

  protected CdaEngine getEngine() {
    return CdaEngine.getInstance();
  }

  protected CdaSettings parseSettingsFile( String cdaSettingsId ) throws Exception {
    return getSettingsManager().parseSettingsFile( cdaSettingsId );
  }

  protected ICdaEnvironment getEnvironment() {
    return CdaEngine.getEnvironment();
  }

  protected TableModel doQuery(CdaSettings cdaSettings, QueryOptions queryOptions) throws Exception {
    return getEngine().doQuery( cdaSettings, queryOptions );
  }

  protected String exportTableModel(TableModel table, ExportOptions opts) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getEngine().getExporter( opts ).export( baos, table );
    return Util.toString( baos.toByteArray() );
  }

//  protected void setConfigProperty( String property, String value ) {
//    testEnvironment.setProperty( property, value );
//  }
//  protected void resetProperties() {
//    testEnvironment.resetProperties();
//  }

  protected static class CdaTestEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {

//    private HierarchicalConfiguration testConfig;
    private CdaTestingContentAccessFactory factory;

    public CdaTestEnvironment( CdaTestingContentAccessFactory factory ) throws InitializationException {
      super( new CoreBeanFactory() );
      this.factory = factory;
    }

    public void initializeDataFactory( DataFactory dataFactory, Configuration configuration, ResourceKey contextKey,
        ResourceManager resourceManager ) throws ReportDataFactoryException {
      dataFactory.initialize(configuration, resourceManager, contextKey,
          new LibLoaderResourceBundleFactory(resourceManager, contextKey, Locale.getDefault(), TimeZone.getDefault()));
      dataFactory.open();
      
    }

    @Override
    public IQueryCache getQueryCache() {
      return new EHCacheQueryCache(false);
    }

    public IContentAccessFactory getRepo() {
      return factory;
    }
//    public Configuration getBaseConfig() {
//      return testConfig;
//    }
//
//    public void setProperty(String property, String value) {
//      testConfig.setConfigProperty( property, value );
//    }
//
//    public void resetProperties() {
//      testConfig = new HierarchicalConfiguration( CdaBoot.getInstance().getGlobalConfig() );
//    }
  }

  protected static class CdaPluginTestEnvironment extends PluginEnvironment {

    private CdaTestingContentAccessFactory factory;

    public CdaPluginTestEnvironment( CdaTestingContentAccessFactory factory ) {
      this.factory = factory;
    }

    public IContentAccessFactory getContentAccessFactory() {
      return factory;
    }

    public IUrlProvider getUrlProvider() {
      throw new UnsupportedOperationException();
    }

    public PluginSettings getPluginSettings() {
      throw new UnsupportedOperationException();
    }

    public String getPluginId() {
      return "cda";
    }

    public IPluginCall getPluginCall( String pluginId, String service, String method ) {
      throw new UnsupportedOperationException();
    }

  }
}
