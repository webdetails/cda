/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.Parameter;

public class CdaPropertiesHelper {

  static Log logger = LogFactory.getLog(Parameter.class);
  
  public static String getStringProperty(String key, String defaultValue){
    String value = CdaBoot.getInstance().getGlobalConfig().getConfigProperty(key);
    return StringUtils.isEmpty(value) ? defaultValue : value;
  }
  
  public static boolean getBoolProperty(String key, boolean defaultValue){
    String value = getStringProperty(key, null);
    if(value != null){
      //Boolean.parse would default to false if unparsable
      value = value.trim().toLowerCase();
      if(value.equals("true")) return true;
      if(value.equals("false")) return false;
    }
    return defaultValue;
  }
  
  public static int getIntProperty(String key, int defaultValue){
    String value = getStringProperty(key, null);
    if(value != null){
      try{
        return Integer.parseInt(value);
      }
      catch(NumberFormatException e){
        logger.error("Unparsable int in property " + key);
      }
    }
    return defaultValue;
  }
  
  
}
