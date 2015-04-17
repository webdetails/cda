package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;

import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;

import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;


/**
 * This class inits Cda plugin within the bi-platform
 *
 * @author gorman
 */
public class CdaLifecycleListener extends SimpleLifeCycleListener implements IPlatformReadyListener {

  static Log logger = LogFactory.getLog( CdaLifecycleListener.class );


  public void init() throws PluginLifecycleException {

  }


  public void loaded() throws PluginLifecycleException {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try {
      super.loaded();
      PluginEnvironment.init( CdaPluginEnvironment.getInstance() );
      CdaEngine.init( new PentahoCdaEnvironment() );
      CdaEngine.getInstance().getConfigProperty( "just load", null );

      Class<CompactBandedMDXDataFactory> factoryClass = CompactBandedMDXDataFactory.class; // CHANGE ME!
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
          factoryClass.getName(), "", "", true, false, true, false, false, false, false, false,
          new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    } catch ( Exception e ) {
      logger.error( "loading error", e );
    } finally {
      Thread.currentThread().setContextClassLoader( contextCL );
    }
  }


  public void unLoaded() throws PluginLifecycleException {
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return PentahoPluginEnvironment.getInstance();
  }

  @Override public void ready() throws PluginLifecycleException {

  }
}
