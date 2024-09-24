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

package pt.webdetails.cda;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import org.pentaho.reporting.libraries.base.config.Configuration;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.kettle.DataAccessKettleAdapter;
import pt.webdetails.cda.dataaccess.kettle.DataAccessKettleAdapterFactory;
import pt.webdetails.cda.exporter.AbstractKettleExporter;
import pt.webdetails.cda.exporter.DefaultStreamExporter;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.exporter.ExportedStreamQueryResult;
import pt.webdetails.cda.exporter.ExportedTableQueryResult;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.StreamExporter;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.resources.IResourceLoader;

import javax.swing.table.TableModel;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main singleton, brokering access to most functionality.
 */
public class CdaEngine {

  private static final Log logger = LogFactory.getLog( CdaEngine.class );
  private static CdaEngine _instance;
  private final ICdaEnvironment environment;

  //TODO: we have to clean this at some point or at least make it a reference map
  private Map<UUID, QueryOptions> wrappedQueries = new ConcurrentHashMap<UUID, QueryOptions>();
  private ExporterEngine exporterEngine;
  private SettingsManager defaultSettingsManager;

  /**
   * Must have been initialized at least once first;
   *
   * @return
   */
  public static synchronized CdaEngine getInstance() {

    if ( _instance == null ) {
      throw new InitializationException( "CdaEngine not initialized", null );
    }

    return _instance;
  }

  public static synchronized CdaEngine init( ICdaEnvironment env ) throws InitializationException {
    assert env != null;
    _instance = new CdaEngine( env );

    // Start ClassicEngineBoot
    ClassicEngineBoot.getInstance().start();
    return _instance;
  }

  /**
   * Init without reporting engine, TEST ONLY
   */
  protected static synchronized void initTestBare( CdaEngine engine ) throws InitializationException {
    assert engine != null;
    _instance = engine;
  }

  protected CdaEngine( ICdaEnvironment env ) throws InitializationException {
    logger.info( "Initializing CdaEngine" );
    environment = env;
    exporterEngine = new ExporterEngine();
    defaultSettingsManager = new SettingsManager();
  }

  public SettingsManager getSettingsManager() {
    return defaultSettingsManager;
  }

  public TableExporter getExporter( ExportOptions opts ) throws UnsupportedExporterException {
    return getExporter( opts.getOutputType(), opts.getExtraSettings() );
  }

  public TableExporter getExporter( String outputType ) throws UnsupportedExporterException {
    return getExporter( outputType, null );
  }

  public TableExporter getExporter( String outputType, Map<String, String> options )
    throws UnsupportedExporterException {
    return getExporterEngine().getExporter( outputType, options );
  }

  private ExporterEngine getExporterEngine() {
    return exporterEngine;
  }

  /**
   * Perform all steps of a doQuery except for export
   *
   * @param cdaSettings
   * @param queryOptions
   * @return
   * @throws UnknownDataAccessException
   * @throws QueryException
   */
  public TableModel doQuery( CdaSettings cdaSettings, QueryOptions queryOptions )
    throws UnknownDataAccessException, QueryException {
    DataAccess dataAccess = cdaSettings.getDataAccess( queryOptions.getDataAccessId() );
    return dataAccess.doQuery( queryOptions );
  }

  public ExportedQueryResult doExportQuery( CdaSettings cdaSettings, QueryOptions queryOptions )
    throws QueryException, UnknownDataAccessException, UnsupportedExporterException {
    DataAccess dataAccess = cdaSettings.getDataAccess( queryOptions.getDataAccessId() );
    TableExporter exporter = getExporter( queryOptions );

    if ( isLegacyStreamingExport( queryOptions, dataAccess, exporter ) ) {
      // Try to initiate a streaming Kettle transformation:
      DataAccessKettleAdapter dataAccessKettleAdapter =
        DataAccessKettleAdapterFactory.create( dataAccess, queryOptions );
      if ( dataAccessKettleAdapter != null ) {
        return createStreamingKettleResult( exporter, dataAccessKettleAdapter );
      }
    }

    TableModel table = doQuery( cdaSettings, queryOptions );
    return new ExportedTableQueryResult( exporter, table );
  }

  private boolean isLegacyStreamingExport( QueryOptions queryOptions, DataAccess dataAccess, TableExporter exporter )
    throws QueryException {
    //[CDA-124] - Exporting queries with parameters and output indexes was failing when done with a
    //streaming Kettle Transformation. In this case CDA 'doQuery' method should be used instead.
    return !dataAccess.hasIterableParameterValues( queryOptions ) && exporter instanceof AbstractKettleExporter
      && ( queryOptions.getParameters().isEmpty() || dataAccess.getOutputs().isEmpty() )
      && queryOptions.getOutputColumnName().isEmpty();
  }

  private ExportedQueryResult createStreamingKettleResult( TableExporter exporter,
      DataAccessKettleAdapter dataAccessKettleAdapter ) {
    StreamExporter streamingExporter =
        new DefaultStreamExporter( (AbstractKettleExporter) exporter, dataAccessKettleAdapter );
    return new ExportedStreamQueryResult( streamingExporter );
  }

  /**
   * @param cdaSettings
   * @param dataAccessId
   * @return
   * @throws UnknownDataAccessException
   */
  public TableModel listParameters( CdaSettings cdaSettings, String dataAccessId ) throws UnknownDataAccessException {
    return cdaSettings.getDataAccess( dataAccessId ).listParameters();
  }

  /**
   * @param cdaSettings
   * @return
   */
  public TableModel listQueries( CdaSettings cdaSettings ) {
    return cdaSettings.listQueries();
  }

  public synchronized QueryOptions unwrapQuery( String uuid )
    throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException {
    return wrappedQueries.remove( UUID.fromString( uuid ) );
  }

  public synchronized String wrapQuery( final CdaSettings cdaSettings, final QueryOptions queryOptions ) {
    UUID uuid = UUID.randomUUID();
    wrappedQueries.put( uuid, queryOptions );
    return uuid.toString();
  }

  /**
   * List ALL available cda files in the repository. Handle with care.
   *
   * @return
   */
  public TableModel getCdaList() {
    IUserContentAccess userRepo = this.getEnv().getRepo().getUserContentAccess( "/" );
    List<IBasicFile> cdaFiles = userRepo.listFiles( "", new IBasicFileFilter() {
      public boolean accept( IBasicFile file ) {
        return StringUtils.equals( file.getExtension(), "cda" );
      }
    }, IReadAccess.DEPTH_ALL, false );


    final int rowCount = cdaFiles.size();

    // Define names and types
    final String[] colNames = { "name", "path" };
    final Class<?>[] colTypes = { String.class, String.class };
    final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );

    for ( IBasicFile file : cdaFiles ) {
      typedTableModel.addRow( new Object[] { file.getName(), file.getFullPath() } );
    }
    return typedTableModel;
  }

  public ExecutorService getExecutorService() {
    return Executors.newCachedThreadPool();
  }

  private ICdaEnvironment getEnv() {
    return environment;
  }


  public static boolean isInitialized() {
    return _instance != null;
  }


  public static IContentAccessFactory getRepo() {
    return getInstance().getEnv().getRepo();
  }

  public static ICdaEnvironment getEnvironment() {
    return getInstance().getEnv();
  }

  public String getConfigProperty( String property ) {
    return getConfig().getConfigProperty( property, null );
  }

  public String getConfigProperty( String property, String defaultValue ) {
    return getConfig().getConfigProperty( property, defaultValue );
  }

  public Configuration getConfig() {
    return getEnv().getBaseConfig();
  }

  public IResourceLoader getResourceLoader() {
    return this.environment.getResourceLoader();
  }

}
