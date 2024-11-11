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


package pt.webdetails.cda.services;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.utils.Pair;

/**
 * manageCache entry point, frontend for {@link CacheMonitor} and {@link CacheScheduler}
 */
public class CacheManager extends ProcessedHtmlPage {

  public CacheManager( IUrlProvider urlProvider, IContentAccessFactory access ) {
    super( urlProvider, access );
  }

  public String manageCache() throws AccessDeniedException, IOException {
    return processPage( new StaticSystemOrigin( "cachemanager" ), "cache.html" );
  }

  protected Iterable<Pair<String, String>> getBackendAssignments( IUrlProvider urlProvider ) {
    String baseApi = urlProvider.getPluginBaseUrl();
    ArrayList<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
    pairs.add( new Pair<String, String>( "CacheManagerBackend.CACHE_MONITOR", quote( baseApi, "cacheMonitor" ) ) );
    pairs.add( new Pair<String, String>( "CacheManagerBackend.CACHE_SCHEDULER", quote( baseApi, "cacheController" ) ) );
    pairs.add( new Pair<>( "CacheManagerBackend.LOCALE_locale", quote( CdaEngine.getEnvironment().getLocale().toString() ) ) );
    return pairs;
  }

  private String quote( String... text ) {

    return '"' + StringUtils.join( text ) + '"';
  }
}
