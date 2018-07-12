package org.pentaho.ctools.cda;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.IUserSession;

import java.util.Locale;
import java.util.Properties;

public class CdaEnvironment implements ICdaEnvironment {


  //region Initialization
  @Override
  public void init() throws InitializationException { }

  @Override
  public void initializeDataFactory(DataFactory dataFactory, Configuration configuration, ResourceKey contextKey, ResourceManager resourceManager) throws ReportDataFactoryException {

  }
  //endregion


  //region Injected properties
  @Override
  public ICubeFileProviderSetter getCubeFileProviderSetter() {
    return this.cubeFileProviderSetter;
  }
  public void setCubeFileProviderSetter( ICubeFileProviderSetter cubeFileProviderSetter ) {
    this.cubeFileProviderSetter = cubeFileProviderSetter;
  }
  private ICubeFileProviderSetter cubeFileProviderSetter;


  @Override
  public IDataAccessUtils getDataAccessUtils() {
    return this.dataAccessUtils;
  }
  public void setDataAccessUtils(IDataAccessUtils dataAccessUtils) {
    this.dataAccessUtils = dataAccessUtils;
  }
  private IDataAccessUtils dataAccessUtils;


  @Override
  public IQueryCache getQueryCache() {
    return this.queryCache;
  }
  public void setQueryCache(IQueryCache queryCache) {
    this.queryCache = queryCache;
  }
  private IQueryCache queryCache;


  @Override
  public IMondrianRoleMapper getMondrianRoleMapper() {
    return this.mondrianRoleMapper;
  }
  public void setMondrianRoleMapper( IMondrianRoleMapper mondrianRoleMapper ) {
    this.mondrianRoleMapper = mondrianRoleMapper;
  }
  private IMondrianRoleMapper mondrianRoleMapper;


  @Override
  public IDataservicesLocalConnection getDataServicesLocalConnection() {
    return this.dataservicesLocalConnection;
  }
  public void setDataservicesLocalConnection( IDataservicesLocalConnection dataservicesLocalConnection ) {
    this.dataservicesLocalConnection = dataservicesLocalConnection;
  }
  private IDataservicesLocalConnection dataservicesLocalConnection;


  @Override
  public FormulaContext getFormulaContext() {
    return this.formulaContext;
  }
  public void setFormulaContext( FormulaContext formulaContext ) {
    this.formulaContext = formulaContext;
  }
  private FormulaContext formulaContext;


  @Override
  public IEventPublisher getEventPublisher() {
    return this.eventPublisher;
  }
  public void setEventPublisher(IEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }
  private IEventPublisher eventPublisher;


  @Override
  public IContentAccessFactory getRepo() {
    return this.repo;
  }
  public void setRepo(IContentAccessFactory repo) {
    this.repo = repo;
  }
  private IContentAccessFactory repo;
  //endregion


  //region Might port code from BaseCdaEnvironment to these
  @Override
  public Properties getCdaComponents() {
    // TODO: perhaps port things from BaseCdaEnvironment
    return null;
  }

  @Override
  public Configuration getBaseConfig() {
    // TODO: perhaps port things from BaseCdaEnvironment
    return null;
  }
  //endregion



  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public IUserSession getUserSession() {
    return null;
  }

  @Override
  public boolean canCreateContent() {
    // let's give full power!
    return true;
  }

}
