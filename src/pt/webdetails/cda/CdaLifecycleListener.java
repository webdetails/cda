package pt.webdetails.cda;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cda.cache.CacheScheduleManager;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;


/**
 * This class inits Cda plugin within the bi-platform
 * @author gorman
 *
 */
public class CdaLifecycleListener implements IPluginLifecycleListener
{

  static Log logger = LogFactory.getLog(CacheScheduleManager.class);


  public void init() throws PluginLifecycleException
  {
    // boot cda
    CdaBoot.getInstance().start();

    PluginHibernateUtil.initialize();


    Class yourDataFactoryClass = CompactBandedMDXDataFactory.class; // CHANGE ME!
    DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData
            (yourDataFactoryClass.getName(), "", "", true, false, true, false, false, false, false, false, new DefaultDataFactoryCore(), 0);
    DataFactoryRegistry.getInstance().register(dmd);

  }


  public void loaded() throws PluginLifecycleException
  {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      CdaBoot.getInstance().getGlobalConfig(); //Load configuration to ensure class loader is correct
      //CacheScheduleManager.getInstance().coldInit();
    }
    catch (Exception e)
    {
      logger.error(Util.getExceptionDescription(e));
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(contextCL);
    }
  }


  public void unLoaded() throws PluginLifecycleException
  {
  }
}
