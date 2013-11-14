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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

/**
 * Main singleton, brokering access to most functionality.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:24:16 PM
 */
public class CdaEngine
{

  private static final Log logger = LogFactory.getLog(CdaEngine.class);
  private static CdaEngine _instance;
  private final ICdaEnvironment environment;

  //TODO: we have to clean this at some point or at least make it a reference map
  private Map<UUID, QueryOptions> wrappedQueries = new ConcurrentHashMap<UUID, QueryOptions>();
  private ExporterEngine exporterEngine;

  public static synchronized CdaEngine getInstance()
  {

    if (_instance == null)
    {
      throw new InitializationException( "CdaEngine not initialized", null );
    }

    return _instance;
  }

  public synchronized static void init(ICdaEnvironment env) throws InitializationException {
      assert env != null;
      _instance = new CdaEngine(env);

      // Start ClassicEngineBoot
      ClassicEngineBoot.getInstance().start();
  }

  protected CdaEngine(ICdaEnvironment env) throws InitializationException
  {
    logger.info("Initializing CdaEngine");
    environment = env;
    exporterEngine = new ExporterEngine();
  }

  public SettingsManager getSettingsManager() {
    return SettingsManager.getInstance();
  }

  public Exporter getExporter( ExportOptions opts ) throws UnsupportedExporterException  {
    return getExporter( opts.getOutputType(), opts.getExtraSettings() );
  }

  public Exporter getExporter( String outputType ) throws UnsupportedExporterException {
    return getExporter( outputType, null );
  }

  public Exporter getExporter( String outputType, Map<String, String> options ) throws UnsupportedExporterException {
    return getExporterEngine().getExporter( outputType, options );
  }

  private ExporterEngine getExporterEngine() {
    return exporterEngine;
  }
  /**
   * Perform all steps of a doQuery except for export
   * @param cdaSettings
   * @param queryOptions
   * @return
   * @throws UnknownDataAccessException
   * @throws QueryException
   */
  public TableModel doQuery (CdaSettings cdaSettings, QueryOptions queryOptions ) throws UnknownDataAccessException, QueryException {
    DataAccess dataAccess = cdaSettings.getDataAccess(queryOptions.getDataAccessId());
    return dataAccess.doQuery( queryOptions );
  }

  /**
   * 
   * @param cdaSettings
   * @param dataAccessId
   * @return
   * @throws UnknownDataAccessException
   */
  public TableModel listParameters(CdaSettings cdaSettings, String dataAccessId) throws UnknownDataAccessException {
    return cdaSettings.getDataAccess(dataAccessId).listParameters();
  }

  /**
   * 
   * @param cdaSettings
   * @return
   */
  public TableModel listQueries ( CdaSettings cdaSettings ) {
    return cdaSettings.listQueries();
  }

  public synchronized QueryOptions unwrapQuery(String uuid) throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {
    return wrappedQueries.remove(UUID.fromString(uuid));
  }

  public synchronized String wrapQuery(
      final CdaSettings cdaSettings,
      final QueryOptions queryOptions)
  {
    UUID uuid = UUID.randomUUID();
    wrappedQueries.put(uuid, queryOptions);
    return uuid.toString();
  }

  /**
   * List ALL available cda files in the repository. Handle with care.
   * @return
   */
  public TableModel getCdaList() {
    IUserContentAccess userRepo = PluginEnvironment.env().getContentAccessFactory().getUserContentAccess("/");
    List<IBasicFile> cdaFiles = userRepo.listFiles("", new IBasicFileFilter() {
      public boolean accept(IBasicFile file) {
        return StringUtils.equals(file.getExtension(), "cda");
      }
    }, IReadAccess.DEPTH_ALL, false) ;


    final int rowCount = cdaFiles.size();

    // Define names and types
    final String[] colNames = {"name", "path"};
    final Class<?>[] colTypes = {String.class, String.class};
    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (IBasicFile file : cdaFiles)
    {
      typedTableModel.addRow(new Object[]{file.getName(), file.getFullPath()});
    }
    return typedTableModel;
  }

  private ICdaEnvironment getEnv() {
	  return environment;
  }


  public static boolean isInitialized()
  {
    return _instance != null;
  }


  public static IContentAccessFactory getRepo() {
    return getInstance().getEnv().getRepo();
  }
  
  public static ICdaEnvironment getEnvironment() {
	  return getInstance().getEnv();
  }

  public String getConfigProperty(String property) {
    return getConfig().getConfigProperty( property, null );
  }
  public String getConfigProperty(String property, String defaultValue) {
    return getConfig().getConfigProperty( property, defaultValue );
  }
  public Configuration getConfig() {
    return getEnv().getBaseConfig();
  }
}
