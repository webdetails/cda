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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;


/**
 * Utility class for internationalization
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class Messages {
  private static final Map<Locale,ResourceBundle> locales = Collections.synchronizedMap(new HashMap<Locale,ResourceBundle>());

  private static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = Messages.locales.get(locale);
    if (bundle == null) {
//      IPentahoSession session = new StandaloneSession("dashboards messages"); //$NON-NLS-1$
      InputStream in = null;
      try {
        in = CdaEngine.getRepo().getPluginSystemReader( null ).getFileInputStream( "messages.properties" ); //$NON-NLS-1$
        bundle = new PropertyResourceBundle(in);
        Messages.locales.put(locale, bundle);
      } catch (Exception e) {
        Logger.error(Messages.class.getName(), "Could not get localization bundle", e); //$NON-NLS-1$
      } finally {
        IOUtils.closeQuietly(in);
      }
    }
    return bundle;
  }

  public static String getEncodedString(final String rawValue) {
    if (rawValue == null) {
      return (""); //$NON-NLS-1$
    }

    StringBuffer value = new StringBuffer();
    for (int n = 0; n < rawValue.length(); n++) {
      int charValue = rawValue.charAt(n);
      if (charValue >= 0x80) {
        value.append("&#x"); //$NON-NLS-1$
        value.append(Integer.toString(charValue, 0x10));
        value.append(";"); //$NON-NLS-1$
      } else {
        value.append((char) charValue);
      }
    }
    return value.toString();

  }

  public static String getXslString(final String key) {
    String rawValue = Messages.getString(key);
    return Messages.getEncodedString(rawValue);
  }
  
  public static String getString(final String key, final String... params){
  	if(params.length == 0){
      try {
        return Messages.getBundle().getString(key);
      } catch (MissingResourceException e) {
        return '!' + key + '!';
      }
  	}
  	return MessageUtil.getString(Messages.getBundle(), key, params);
  }
  
  public static String getErrorString(final String key, final String... params){
  	if (params.length == 0) return MessageUtil.formatErrorMessage(key, Messages.getString(key));
  	else return MessageUtil.getErrorString(Messages.getBundle(), key, params);
  }

}
