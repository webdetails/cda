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

import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.SolutionRepositoryUtils;

/**
 * Main engine class that will answer to calls
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

  protected CdaEngine(ICdaEnvironment env) throws InitializationException
  {
    logger.info("Initializing CdaEngine");
    environment = env;
  }

  public QueryOptions unwrapQuery(String uuid) throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {
    return wrappedQueries.remove(UUID.fromString(uuid));
  }

  public String wrapQuery(
      final OutputStream out,
      final CdaSettings cdaSettings,
      final QueryOptions queryOptions)
  {
    UUID uuid = UUID.randomUUID();
    wrappedQueries.put(uuid, queryOptions);
    return uuid.toString();
  }

  public void doQuery(final OutputStream out,
          final CdaSettings cdaSettings,
          final QueryOptions queryOptions) throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {

    logger.debug("Doing query on CdaSettings [ " + cdaSettings.getId() + " (" + queryOptions.getDataAccessId() + ")]");

    TableModel tableModel = cdaSettings.getDataAccess(queryOptions.getDataAccessId()).doQuery(queryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings()).export(out, tableModel);

  }


  public void listQueries(final OutputStream out,
          final CdaSettings cdaSettings,
          final DiscoveryOptions discoveryOptions) throws UnsupportedExporterException, ExporterException
  {

    logger.debug("Getting list of queries on CdaSettings [ " + cdaSettings.getId() + ")]");


    final TableModel tableModel = cdaSettings.listQueries(discoveryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);

  }


  public void listParameters(final OutputStream out,
          final CdaSettings cdaSettings,
          final DiscoveryOptions discoveryOptions) throws UnknownDataAccessException, UnsupportedExporterException, ExporterException
  {

    logger.debug("Getting list of queries on CdaSettings [ " + cdaSettings.getId() + ")]");


    final TableModel tableModel = cdaSettings.getDataAccess(discoveryOptions.getDataAccessId()).listParameters(discoveryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);


  }


  public void getCdaList(final OutputStream out, final DiscoveryOptions discoveryOptions) throws UnsupportedExporterException, ExporterException
  {

    final TableModel tableModel = SolutionRepositoryUtils.getInstance().getCdaList();

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);

  }


  private static ICdaEnvironment getConfiguredEnvironment() throws InitializationException {
	    String className = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.environment.default");
	    
	    if (StringUtils.isNotBlank(className)) {
	      try {
	        final Class<?> clazz;
	        clazz = Class.forName(className);
	        if (!ICdaEnvironment.class.isAssignableFrom(clazz)) {
	          throw new InitializationException (
	            "Plugin class specified by property pt.webdetails.cda.beanFactoryClass "
	            + " must implement "
	            + ICdaBeanFactory.class.getName(), null);
	        }
	          return (ICdaEnvironment) clazz.newInstance();
	        } catch (ClassNotFoundException e) {
	          String errorMessage = "Class not found when loading bean factory " + className;
	          logger.error(errorMessage, e);
	          throw new InitializationException(errorMessage, e); 
	        } catch (IllegalAccessException e) {
	          String errorMessage = "Illegal access when loading bean factory from " + className;
	          logger.error(errorMessage, e);
	          throw new InitializationException(errorMessage, e); 
	        } catch (InstantiationException e) {
	          String errorMessage = "Instantiation error when loading bean factory from " + className;
	          logger.error(errorMessage, e);
	          throw new InitializationException(errorMessage, e); 
	        }
	      }
	    
	    return null;
  }

  private ICdaEnvironment getEnv() {
	  return environment;
  }
  
  public static boolean isInitialized()
  {
    return _instance != null;
  }

  public static void init() throws InitializationException
  {
	  init(null);
  }

  public static void init(ICdaEnvironment env) throws InitializationException
  {
	  if (!isInitialized()) {
		  // try to get the environment from the configuration
		  // will return the DefaultCdaEnvironment by default
		  if (env == null)
			  env = getConfiguredEnvironment();

		  if (env == null)
			  env = new DefaultCdaEnvironment();
		  
		  _instance = new CdaEngine(env);


		  // Start ClassicEngineBoot
		  CdaBoot.getInstance().start();
		  ClassicEngineBoot.getInstance().start();
	  }

  }
  
  public static synchronized ICdaEnvironment getEnvironment() {
	  return getInstance().getEnv();
  }
  
  public static synchronized CdaEngine getInstance()
  {

    if (_instance == null)
    {
      try {
        init();
      } catch (InitializationException ie) {
        logger.fatal("Initialization failed. CDA will NOT be available", ie);
      }
    }

    return _instance;
  }
}
