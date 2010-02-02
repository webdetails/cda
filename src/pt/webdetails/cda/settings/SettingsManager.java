package pt.webdetails.cda.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:40:12 PM
 */
public class SettingsManager {


  private static final Log logger = LogFactory.getLog(SettingsManager.class);
  private static SettingsManager _instance;



  public static SettingsManager getInstance() {

    if (_instance == null)
      _instance = new SettingsManager();

    return _instance;
  }
}
