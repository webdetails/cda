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
package org.pentaho.ctools.cda;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactoryContext;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

public class CdaEnvironment implements ICdaEnvironment {

  private Configuration config;


  //region Initialization
  @Override
  public void init() throws InitializationException {
    //NO OP
  }

  @Override
  public void initializeDataFactory( DataFactory dataFactory, Configuration configuration, ResourceKey contextKey, ResourceManager resourceManager ) throws ReportDataFactoryException {

    dataFactory.initialize( new DataFactoryContext() {
      public Configuration getConfiguration() {
        return configuration;
      }

      public ResourceManager getResourceManager() {
        return resourceManager;
      }

      public ResourceKey getContextKey() {
        return contextKey;
      }

      public ResourceBundleFactory getResourceBundleFactory() {
        return new LibLoaderResourceBundleFactory( resourceManager, contextKey,
            getLocale(), TimeZone.getDefault() );
      }

      public DataFactory getContextDataFactory() {
        return dataFactory;
      }

      @Override
      public FormulaContext getFormulaContext() {
        return null;
      }
    } );
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

  public void setDataAccessUtils( IDataAccessUtils dataAccessUtils ) {
    this.dataAccessUtils = dataAccessUtils;
  }

  private IDataAccessUtils dataAccessUtils;


  @Override
  public IQueryCache getQueryCache() {
    return this.queryCache;
  }

  public void setQueryCache( IQueryCache queryCache ) {
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

  public void setEventPublisher( IEventPublisher eventPublisher ) {
    this.eventPublisher = eventPublisher;
  }

  private IEventPublisher eventPublisher;


  @Override
  public IContentAccessFactory getRepo() {
    return this.repo;
  }

  public void setRepo( IContentAccessFactory repo ) {
    this.repo = repo;
  }

  private IContentAccessFactory repo;
  //endregion


  //region Might port code from BaseCdaEnvironment to these
  @Override
  public Properties getCdaComponents() {
    // TODO: perhaps port things from BaseCdaEnvironment
    Properties cdaComponents = new Properties();
    cdaComponents.setProperty( "dataAccesses",
            "KettleDataAccess,DataservicesDataAccess,StreamingDataservicesDataAccess" );

    cdaComponents.setProperty( "connections", "AbstractConnection,Connection,metadata.MetadataConnection,"
        + "mondrian.AbstractMondrianConnection,mondrian.JdbcConnection,mondrian.JndiConnection,"
        + "mondrian.MondrianConnection,olap4j.JdbcConnection,olap4j.DefaultOlap4jConnection,"
        + "olap4j.Olap4JConnection,scripting.ScriptingConnection,sql.AbstractSqlConnection,sql.JdbcConnection,"
        + "sql.JndiConnection,sql.SqlConnection,dataservices.dataservicesConnection" );

    return cdaComponents;
  }

  @Override
  public synchronized Configuration getBaseConfig() {
    // TODO: perhaps port things from BaseCdaEnvironment
    if ( config == null ) {

      Map<String, String> map = new HashMap<>();
      map.put( "pt.webdetails.cda.DefaultRowProductionTimeout", "120" );
      map.put( "pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit", "SECONDS" );
      map.put( "pt.webdetails.cda.exporter.csv.Separator", ";" );
      map.put( "pt.webdetails.cda.dataaccess.parameterarray.Separator", ";" );
      map.put( "pt.webdetails.cda.dataaccess.parameterarray.Quote", "\"" );
      map.put( "pt.webdetails.cda.dataaccess.parameterarray.kettle.Separator", "," );
      map.put( "pt.webdetails.cda.dataaccess.parameterarray.kettle.Quote", "'" );
      map.put( "pt.webdetails.cda.TypeSearchMaxRows", "500" );
      map.put( "pt.webdetails.cda.UseTerracotta", "false" );
      map.put( "pt.webdetails.cda.QueryTimeThreshold", "10" );
      map.put( "pt.webdetails.cda.SortingType", "DEFAULT" );
      map.put( "pt.webdetails.cda.BandedMDXMode", "compact" );
      map.put( "pt.webdetails.cda.cache.executeAtStart", "false" );
      map.put( "pt.webdetails.cda.cache.backupWarmerCron", "0 0 0/30 * * ?" );

      config = new Configuration() {
        @Override
        public String getConfigProperty( String key ) {
          return map.get( key );
        }

        @Override
        public String getConfigProperty( String key, String defaultValue ) {
          return map.get( key );
        }

        @Override
        public Iterator<String> findPropertyKeys( String prefix ) {
          return map.keySet().stream().filter( key -> key.startsWith( prefix ) ).iterator();
        }

        @Override
        public Enumeration<String> getConfigProperties() {
          return Collections.enumeration( map.values() );
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
          throw new CloneNotSupportedException();
        }
      };

    }
    return config;
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
