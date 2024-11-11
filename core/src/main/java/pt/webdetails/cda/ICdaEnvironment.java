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


package pt.webdetails.cda;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.session.IUserSession;

import java.util.Locale;
import java.util.Properties;


public interface ICdaEnvironment {

  public void init() throws InitializationException;

  public ICubeFileProviderSetter getCubeFileProviderSetter();

  public IQueryCache getQueryCache();

  public IMondrianRoleMapper getMondrianRoleMapper();

  public IDataservicesLocalConnection getDataServicesLocalConnection();

  /**
   * {@link FormulaContext} exposing parameters in CDA formulas.<br> Refer to implementations for available parameters.
   */
  public FormulaContext getFormulaContext();

  public Properties getCdaComponents();

  public IEventPublisher getEventPublisher();

  public IDataAccessUtils getDataAccessUtils();

  public IContentAccessFactory getRepo();

  public Configuration getBaseConfig();

  /**
   * Differs between pentaho 4.x and 5.x
   */
  public void initializeDataFactory(
    final DataFactory dataFactory,
    final Configuration configuration,
    final ResourceKey contextKey,
    final ResourceManager resourceManager ) throws ReportDataFactoryException;

  public Locale getLocale();

  /**
   * Provides an access to plugin's resource loader.
   * The default implementation returns null, and exists for the compatibility sake.
   *
   * @return a resource loader if it is present, otherwise returns null
   * @see BaseCdaEnvironment#init() for the reference implementation
   */
  default IResourceLoader getResourceLoader() {
    throw new UnsupportedOperationException( "The method is not implemented" );
  }

  public IUserSession getUserSession();

  public boolean canCreateContent();
}
