/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.utils.framework;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PluginUtils {

  private static final Log logger = LogFactory.getLog( PluginUtils.class );

  @SuppressWarnings( "unchecked" )
  public static <T> T getPluginBean( String prefix, Class<T> interfaceClass ) throws PluginBeanException {

    if ( interfaceClass == null ) {
      throw new IllegalArgumentException();
    }

    String key = prefix + interfaceClass.getSimpleName();

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    if ( pluginManager.isBeanRegistered( key ) ) {
      Object beanObject = pluginManager.getBean( key );
      try {
        return (T) beanObject;
      } catch ( ClassCastException ex ) {
        throw new PluginBeanException( MessageFormat
          .format( "The class for bean {0} must implement {1}", key, interfaceClass.getName() ) ); //$NON-NLS-1$
      }
    } else {
      logger.error( MessageFormat.format( "Bean {0} is not registered.", key ) ); //$NON-NLS-1$
      return null;
    }
  }


}
