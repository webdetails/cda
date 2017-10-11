/*!
 * Copyright 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils;

import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

  private static final Map<Locale, ResourceBundle> locales = Collections
      .synchronizedMap( new HashMap<Locale, ResourceBundle>() );

  protected static Map<Locale, ResourceBundle> getLocales() {
    return locales;
  }

  private static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = locales.get( locale );
    if ( bundle == null ) {
      bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );
      locales.put( locale, bundle );
    }
    return bundle;
  }

  public static String getString( String key ) {
    try {
      return getBundle().getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1 ) {
    return MessageUtil.getString( getBundle(), key, param1 );
  }

  public static String getString( String key, String param1, String param2 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2 );
  }

  public static String getString( String key, String param1, String param2, String param3 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2, param3 );
  }

  public static String getString( String key, String param1, String param2, String param3, String param4 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2, param3, param4 );
  }

  public static String getErrorString( String key ) {
    return MessageUtil.formatErrorMessage( key, getString( key ) );
  }

  public static String getErrorString( String key, String param1 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1 );
  }

  public static String getErrorString( String key, String param1, String param2 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1, param2 );
  }

  public static String getErrorString( String key, String param1, String param2, String param3 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1, param2, param3 );
  }

  public static void main( String[] args ) {
    ResourceBundle bundle = Messages.getBundle();
  }
}
