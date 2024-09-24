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

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.Parameter;

public class CdaPropertiesHelper {

  static Log logger = LogFactory.getLog( Parameter.class );

  public static String getStringProperty( String key, String defaultValue ) {
    return CdaEngine.getInstance().getConfigProperty( key, defaultValue );
  }

  public static boolean getBoolProperty( String key, boolean defaultValue ) {
    String value = getStringProperty( key, null );
    if ( value != null ) {
      // Boolean.parse would default to false if unparsable
      value = value.trim().toLowerCase();
      if ( value.equals( "true" ) ) {
        return true;
      }
      if ( value.equals( "false" ) ) {
        return false;
      }
    }
    return defaultValue;
  }

  public static int getIntProperty( String key, int defaultValue ) {
    String value = getStringProperty( key, null );
    if ( value != null ) {
      try {
        return Integer.parseInt( value );
      } catch ( NumberFormatException e ) {
        logger.error( "Unparsable int in property " + key );
      }
    }
    return defaultValue;
  }

}
