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

package pt.webdetails.cda;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.exporter.ExportedTableQueryResult;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.DoQueryParameters;

/**
 * Basic CDA functionality.<br> <ul> <li>doQuery</li> <li>listQueries</li> <li>listParameters</li>
 * <li>listDataAccessTypes</li> <li>getCdaList</li> </ul>
 */
public class CdaCoreService {

  private static Log logger = LogFactory.getLog( CdaCoreService.class );

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  private static final String JSONP_CALLBACK = "callback";

  private CdaEngine engine;
  private SettingsManager settingsManager;

  public CdaCoreService() {
    this( CdaEngine.getInstance(), CdaEngine.getInstance().getSettingsManager() );
  }

  public CdaCoreService( CdaEngine engine ) {
    this( engine, engine.getSettingsManager() );
  }

  private CdaCoreService( CdaEngine engine, SettingsManager settingsManager ) {
    this.engine = engine;
    this.settingsManager = settingsManager;
  }

  /**
   * @param parameters
   * @return
   * @throws Exception
   */
  public ExportedQueryResult doQuery( DoQueryParameters parameters ) throws Exception {
    final String path = parameters.getPath();
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile( path );
    final QueryOptions queryOptions = getQueryOptions( parameters );

    return engine.doExportQuery( cdaSettings, queryOptions );
  }

  /**
   * List data accesses available in cda settings.
   *
   * @param cdaSettingsId
   * @param exportOptions
   * @return
   * @throws Exception
   */
  public ExportedQueryResult listQueries( final String cdaSettingsId, final ExportOptions exportOptions )
    throws Exception {
    CdaSettings cda = settingsManager.parseSettingsFile( cdaSettingsId );
    TableModel table = engine.listQueries( cda );
    return exportQuery( table, exportOptions );
  }

  /**
   * List parameters accepted by the Data Access.
   *
   * @param cdaSettingsId
   * @param dataAccessId
   * @param exportOptions
   * @return
   * @throws Exception
   */
  public ExportedQueryResult listParameters( final String cdaSettingsId, final String dataAccessId,
                                             ExportOptions exportOptions ) throws Exception {
    CdaSettings cda = settingsManager.parseSettingsFile( cdaSettingsId );
    return exportQuery( engine.listParameters( cda, dataAccessId ), exportOptions );
  }

  /**
   * @param parameters
   * @return
   */
  public String wrapQuery( DoQueryParameters parameters ) throws Exception {
    return engine.wrapQuery( settingsManager.parseSettingsFile( parameters.getPath() ), getQueryOptions( parameters ) );
  }

  /**
   * @param path
   * @param uuid
   * @return
   * @throws Exception
   */
  public ExportedQueryResult unwrapQuery( String path, final String uuid ) throws Exception {
    final CdaSettings cdaSettings = settingsManager.parseSettingsFile( path );
    QueryOptions queryOptions = engine.unwrapQuery( uuid );
    if ( queryOptions != null ) {
      return engine.doExportQuery( cdaSettings, queryOptions );
    } else {
      logger.error( "unwrapQuery: uuid " + uuid + " not found." );
      return null;
    }
  }

  /**
   * List every single CDA file on the repository. Use with care.
   *
   * @param exportOptions
   * @return
   * @throws UnsupportedExporterException
   */
  public ExportedQueryResult getCdaList( ExportOptions exportOptions ) throws UnsupportedExporterException {
    return exportQuery( engine.getCdaList(), exportOptions );
  }

  private ExportedQueryResult exportQuery( TableModel table, ExportOptions opts ) throws UnsupportedExporterException {
    Exporter exporter = engine.getExporter( opts );
    return new ExportedTableQueryResult( (TableExporter) exporter, table );
  }

  /**
   * Lists available data access types as CDE data sources.
   *
   * @param refreshCache reload list of data access types
   * @return JSON
   */
  public String listDataAccessTypes( final boolean refreshCache ) {

    DataAccessConnectionDescriptor[] data = settingsManager.getDataAccessDescriptors( refreshCache );

    StringBuilder output = new StringBuilder();
    output.append( "{\n" );
    for ( DataAccessConnectionDescriptor datum : data ) {
      output.append( datum.toJSON() ).append( ",\n" );
    }
    output.append( "\n}" );
    return output.toString().replaceAll( ",\n\\z", "\n" );

  }

  public void clearCache() {
    CdaEngine.getInstance().getSettingsManager().clearCache();
    AbstractDataAccess.clearCache();
  }

  public static QueryOptions getQueryOptions( DoQueryParameters parameters ) {
    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( parameters.getDataAccessId() );

    // parameters
    for ( Map.Entry<String, Object> entry : parameters.getParameters().entrySet() ) {
      final String name = entry.getKey();
      final Object parameter = entry.getValue();
      queryOptions.addParameter( name, parameter );
    }

    // bypass cache?
    queryOptions.setCacheBypass( parameters.isBypassCache() );

    // output, sort and paginate options
    setPostProcessOptions( parameters, queryOptions );

    // output type, extra settings
    setExportOptions( parameters, queryOptions );

    // output column names
    queryOptions.setOutputColumnName( parameters.getOutputColumnName() );

    return queryOptions;
  }

  private static void setPostProcessOptions( DoQueryParameters parameters, final QueryOptions queryOptions ) {
    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = parameters.getPageSize();
    final long pageStart = parameters.getPageStart();
    final boolean paginate = parameters.isPaginateQuery();
    if ( pageSize > 0 || pageStart > 0 || paginate ) {
      if ( pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE ) {
        throw new ArithmeticException( "Paging values too large" );
      }
      queryOptions.setPaginate( true );
      queryOptions.setPageSize( pageSize > 0 ? (int) pageSize : paginate ? DEFAULT_PAGE_SIZE : 0 );
      queryOptions.setPageStart( pageStart > 0 ? (int) pageStart : paginate ? DEFAULT_START_PAGE : 0 );
    }

    try {
      queryOptions.setOutputIndexId( parameters.getOutputIndexId() );
    } catch ( NumberFormatException e ) {
      logger.error( "Illegal outputIndexId '" + parameters.getOutputIndexId() + "'" );
    }

    final ArrayList<String> sortBy = new ArrayList<String>();
    for ( String sort : parameters.getSortBy() ) {
      if ( !StringUtils.isEmpty( sort ) ) {
        sortBy.add( sort );
      }
    }
    queryOptions.setSortBy( sortBy );
  }

  private static void setExportOptions( DoQueryParameters parameters, final QueryOptions queryOptions ) {
    queryOptions.setOutputType( parameters.getOutputType() );

    for ( Map.Entry<String, Object> entry : parameters.getExtraSettings().entrySet() ) {
      final String name = entry.getKey();
      final Object parameter = entry.getValue();
      queryOptions.addSetting( name, (String) parameter );
    }
    // we'll allow for the special "callback" param to be used, and passed as settingcallback to jsonp exports
    if ( !parameters.getJsonCallback().equals( "<blank>" ) ) // XXX why <blank>?! check the ui for this
    {
      queryOptions.addSetting( JSONP_CALLBACK, parameters.getJsonCallback() );
    }
  }

}
