/*!
 * Copyright 2017 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils;

import pt.webdetails.cda.CdaConstants;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.utils.AbstractCorsUtil;
import pt.webdetails.cpf.utils.CsvUtil;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * CDA CorsUtil implementation
 */
public class CorsUtil extends AbstractCorsUtil {

  private static CorsUtil instance;

  public static CorsUtil getInstance() {
    if ( instance == null ) {
      instance = new CorsUtil();
    }
    return instance;
  }

  /**
   * Retrieves a flag value from a plugin settings.xml
   * @return true if the flag is present and CORS is allowed, otherwise returns false
   */
  @Override public boolean isCorsAllowed() {
    return "true".equalsIgnoreCase( CdaEngine.getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
      CdaConstants.PLUGIN_SETTINGS_ALLOW_CROSS_DOMAIN_RESOURCES ) );
  }

  /**
   * Retrieves a list value from a plugin settings.xml
   * @return returns a domain white list, if it is present, otherwise returns an empty list
   */
  @Override public Collection<String> getDomainWhitelist() {
    return CsvUtil.parseCsvString( CdaEngine.getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
      CdaConstants.PLUGIN_SETTINGS_CROSS_DOMAIN_RESOURCES_WHITELIST ) );
  }

  /**
   * Returns a predicate which evaluates if a domain is in the list of whitelisted domains.
   * @return a {@link Predicate} that checks if the {@link String} domain received is present in the whitelist domains.
   */
  public Predicate<String> isCorsRequestOriginAllowedPredicate( ) {
    return domain -> isCorsAllowed() && this.getDomainWhitelist().contains( domain );
  }
}
