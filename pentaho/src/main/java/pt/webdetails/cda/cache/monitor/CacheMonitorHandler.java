/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.cache.monitor;

import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.services.CacheMonitor;
import pt.webdetails.cda.utils.PentahoHelper;
import pt.webdetails.cda.utils.framework.JsonCallHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.pentaho.platform.engine.security.SecurityHelper;

//
public class CacheMonitorHandler extends JsonCallHandler {
  //
  //  private static Log logger = LogFactory.getLog(JsonCallHandler.class);

  private static CacheMonitorHandler _instance = new CacheMonitorHandler();

  public static CacheMonitorHandler getInstance() {
    return _instance;
  }

  private CacheMonitor monitor;

  public CacheMonitorHandler() {
    this.monitor = new CacheMonitor();
    registerMethods();
  }


  protected boolean hasPermission( IPentahoSession session,
                                   Method method ) { //limit all interaction besides overview to admin role
    return method.getName().equals( "cacheOverview" ) || PentahoHelper.isAdmin( session );
  }

  private void registerMethods() {

    registerMethod( "cached", new JsonCallHandler.Method() {
      /**
       * get all cached items for given cda file and data access id
       */
      public JSONObject execute( IParameterProvider params ) throws JSONException, ExporterException, IOException {
        String cdaSettingsId = params.getStringParameter( "cdaSettingsId", null );
        String dataAccessId = params.getStringParameter( "dataAccessId", null );
        return monitor.listQueriesInCache( cdaSettingsId, dataAccessId );
      }
    } );

    registerMethod( "cacheOverview", new JsonCallHandler.Method() {
      /**
       * get details on a particular cached item
       */
      public JSONObject execute( IParameterProvider params ) throws JSONException {
        String cdaSettingsId = params.getStringParameter( "cdaSettingsId", null );
        return monitor.getCachedQueriesOverview( cdaSettingsId );
      }
    } );

    registerMethod( "getDetails", new JsonCallHandler.Method() {
      /**
       * get details on a particular cached item
       */
      public JSONObject execute( IParameterProvider params )
        throws UnsupportedEncodingException, JSONException, ExporterException, IOException, ClassNotFoundException {
        String encodedCacheKey = params.getStringParameter( "key", null );
        return monitor.getCacheQueryTable( encodedCacheKey );
      }
    } );

    registerMethod( "removeCache", new JsonCallHandler.Method() {
      /**
       * Remove item from cache 
       */
      public JSONObject execute( IParameterProvider params )
        throws JSONException, UnsupportedEncodingException, IOException, ClassNotFoundException {
        String serializedCacheKey = params.getStringParameter( "key", null );
        return monitor.removeQueryFromCache( serializedCacheKey );
      }
    } );

    registerMethod( "removeAll", new JsonCallHandler.Method() {

      @Override
      public JSONObject execute( IParameterProvider params ) throws JSONException {
        String cdaSettingsId = params.getStringParameter( "cdaSettingsId", null );
        String dataAccessId = params.getStringParameter( "dataAccessId", null );
        return monitor.removeAll( cdaSettingsId, dataAccessId );
      }
    } );

    registerMethod( "shutdown", new JsonCallHandler.Method() {

      @Override
      public JSONObject execute( IParameterProvider params ) throws Exception {
        //AbstractDataAccess.shutdownCache();
        return monitor.shutdown(); // getOKJson("Cache shutdown.");
      }
    } );

  }
}
