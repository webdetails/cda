/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.utils.framework;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PluginUtils {
  
  private static final Log logger = LogFactory.getLog(PluginUtils.class);

  @SuppressWarnings("unchecked")
  public static <T> T getPluginBean(String prefix, Class<T> interfaceClass) throws PluginBeanException {
    
    if(interfaceClass == null){
  throw new IllegalArgumentException();
    }
    
    String key = prefix + interfaceClass.getSimpleName();
    
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
    if (pluginManager.isBeanRegistered(key)) {
      Object beanObject = pluginManager.getBean(key);
      try {
        return (T) beanObject;
      } catch (ClassCastException ex) {
        throw new PluginBeanException(MessageFormat.format("The class for bean {0} must implement {1}", key, interfaceClass.getName())); //$NON-NLS-1$
      }
    } else {
      logger.error(MessageFormat.format("Bean {0} is not registered.",key));//$NON-NLS-1$
      return null;
    }
  }
  
  
}
