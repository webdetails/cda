/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.pentaho.platform.api.engine.IPentahoSession;
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
import pt.webdetails.cpf.session.IUserSession;

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

  //TODO: we have to clean this at some point or at least make it a reference map
  private Map<UUID, QueryOptions> wrappedQueries = new ConcurrentHashMap<UUID, QueryOptions>();

  private ICdaBeanFactory beanFactory;
  
  protected CdaEngine() throws InitializationException
  {
    logger.info("Initializing CdaEngine");
    init();

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


  public void getCdaList(final OutputStream out, final DiscoveryOptions discoveryOptions, final IUserSession userSession) throws UnsupportedExporterException, ExporterException
  {

    final TableModel tableModel = SolutionRepositoryUtils.getInstance().getCdaList(userSession);

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);

  }


  public static boolean isStandalone()
  {
    return "true".equals(CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.Standalone"));
  }


  private void init() throws InitializationException
  {

    // Start ClassicEngineBoot
    CdaBoot.getInstance().start();
    ClassicEngineBoot.getInstance().start();
    
    
    //Get beanFactory
    String className = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.beanFactoryClass");
    
    if (className != null && !className.isEmpty()) {
      try {
        final Class<?> clazz;
        clazz = Class.forName(className);
        if (!ICdaBeanFactory.class.isAssignableFrom(clazz)) {
          throw new InitializationException (
            "Plugin class specified by property pt.webdetails.cda.beanFactoryClass "
            + " must implement "
            + ICdaBeanFactory.class.getName(), null);
        }
          beanFactory = (ICdaBeanFactory) clazz.newInstance();
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
    
    
    

  }


  public ICdaBeanFactory getBeanFactory() {
    return beanFactory;
  }
  
  
  public static synchronized CdaEngine getInstance()
  {

    if (_instance == null)
    {
      try {
        _instance = new CdaEngine();
      } catch (InitializationException ie) {
        logger.fatal("Initialization failed. CDA will NOT be available", ie);
      }
    }

    return _instance;
  }
}
