/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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
