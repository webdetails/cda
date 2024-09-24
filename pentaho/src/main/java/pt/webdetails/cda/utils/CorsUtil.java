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
