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

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.Parameter;

public class CdaPropertiesHelper {

  static Log logger = LogFactory.getLog(Parameter.class);
  
  public static String getStringProperty(String key, String defaultValue){
    return CdaEngine.getInstance().getConfigProperty( key, defaultValue );
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
