/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cda.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.Pair;


/**
 * Serves the previewer page.
 */
public class Previewer extends ProcessedHtmlPage {


  private static Log logger = LogFactory.getLog( BaseService.class );

  private static final String SYS_PATH = "previewer";
  private static final String PAGE = "previewer.html";

  private static final String SYS_ABOUT_PATH = "static/about.html";
  private static final String UI_BACKEND_PREFIX = "PreviewerBackend.";

  private String cdaPath;

  public Previewer( IUrlProvider urlProvider, IContentAccessFactory access ) {
    super( urlProvider, access );
  }

  /**
   * exposed to page
   */
  protected Iterable<Pair<String, String>> getBackendAssignments( IUrlProvider urlProvider ) {
    String baseApi = urlProvider.getPluginBaseUrl();
    ArrayList<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_doQuery", quote( baseApi, "doQuery" ) ) );
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_unwrapQuery", quote( baseApi, "unwrapQuery" ) ) );
    pairs
      .add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_listParameters", quote( baseApi, "listParameters" ) ) );
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_listQueries", quote( baseApi, "listQueries" ) ) );
    pairs.add(
      new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_cacheController", quote( baseApi, "cacheController" ) ) );
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_about",
      quote( urlProvider.getPluginStaticBaseUrl(), SYS_ABOUT_PATH ) ) );
    Locale locale = CdaEngine.getEnvironment().getLocale();
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "PATH_page",
      quote( urlProvider.getPluginStaticBaseUrl(), SYS_PATH ) ) );
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "Path", quote( cdaPath ) ) );
    pairs.add( new Pair<String, String>( UI_BACKEND_PREFIX + "LOCALE_locale", quote( locale.toString() ) ) );
    addDataTablesLocalization( pairs, UI_BACKEND_PREFIX + "LOCALE_dataTables", locale );
    return pairs;
  }

  private void addDataTablesLocalization( ArrayList<Pair<String, String>> pairs, String name, Locale locale ) {
    //TODO: have some better and standard way of using locales, this way will be removed..
    IReadAccess reader = getSystemPath( SYS_PATH );
    String defaultDataTablesMessages = getDataTablesMessagesPath( null );
    String dataTablesMessages = getDataTablesMessagesPath( locale );
    String localization = "{}";
    try {
      if ( reader.fileExists( dataTablesMessages ) ) {

        localization = Util.toString( reader.getFileInputStream( dataTablesMessages ) );

      } else if ( reader.fileExists( defaultDataTablesMessages ) ) {
        localization = Util.toString( reader.getFileInputStream( defaultDataTablesMessages ) );
      } else {
        logger.error( "previewer: localization not found: " + defaultDataTablesMessages );
      }
    } catch ( IOException e ) {
      logger.error( e );
    }
    pairs.add( new Pair<String, String>( name, localization ) );
  }

  private String quote( String... text ) {
    return '"' + StringUtils.join( text ) + '"';
  }

  private String getDataTablesMessagesPath( Locale locale ) {
    String localeRep = ( locale == null ) ? "" : "_" + locale.toString();
    return String.format( "dataTables/languages/Messages%s.json", localeRep );
  }

  public String previewQuery( String cdaPath ) throws Exception {
    //TODO: add cache
    this.cdaPath = cdaPath;
    return processPage( new StaticSystemOrigin( SYS_PATH ), PAGE );
  }


}
