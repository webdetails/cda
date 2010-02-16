package pt.webdetails.cda;

import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;

/**
 * This class inits Cda plugin within the bi-platform
 * @author gorman
 *
 */
public class CdaLifecycleListener implements IPluginLifecycleListener
{

  public void init() throws PluginLifecycleException
  {
    // boot cda
    CdaBoot.getInstance().start();
  }

  public void loaded() throws PluginLifecycleException
  {
  }

  public void unLoaded() throws PluginLifecycleException
  {
  }

}
