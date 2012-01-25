package pt.webdetails.cda;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cda.cache.CacheScheduleManager;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.utils.PluginHibernateUtil;
import pt.webdetails.cda.utils.Util;

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
  }


  public void loaded() throws PluginLifecycleException
  {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      CacheScheduleManager.getInstance().coldInit();
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
    AbstractDataAccess.shutdowCache();
  }
}
