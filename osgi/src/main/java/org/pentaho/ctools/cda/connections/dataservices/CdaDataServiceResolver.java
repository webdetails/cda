/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.ctools.cda.connections.dataservices;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.sql.SQL;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.dataservice.Context;
import org.pentaho.di.trans.dataservice.DataServiceExecutor;
import org.pentaho.di.trans.dataservice.DataServiceMeta;
import org.pentaho.di.trans.dataservice.IDataServiceMetaFactory;
import org.pentaho.di.trans.dataservice.resolvers.DataServiceResolver;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CdaDataServiceResolver implements DataServiceResolver {
  private static Log logger = LogFactory.getLog( CdaDataServiceResolver.class );
  private IDataServiceMetaFactory dataServiceMetaFactory;
  private Context context;

  private final String repoName;
  private final String repoUsername;
  private final String repoPassword;

  private String dataServiceName;
  private String ktrPath;
  private String ktrName;
  private String stepName;
  private boolean isStreaming;
  private int rowLimit;
  private long timeLimit;

  private final HashMap<String, DataServiceMetaCacheElement> dataServiceMetaCache;

  public CdaDataServiceResolver( IDataServiceMetaFactory dataServiceMetaFactory, Context context, String
      repositoryName, String username, String password ) {
    this.dataServiceMetaFactory = dataServiceMetaFactory;
    this.context = context;

    this.repoName = repositoryName;
    this.repoUsername = username;
    this.repoPassword = password;

    this.dataServiceMetaCache = new HashMap<>();
  }

  @Override
  public  DataServiceExecutor.Builder createBuilder( SQL sql ) throws KettleException {
    final String dataServiceName = sql.getServiceName();
    DataServiceMeta dataServiceMeta = getDataServiceMeta( dataServiceName );

    if ( dataServiceMeta == null ) {
      return new DataServiceExecutor.Builder( sql, dataServiceMeta, context );
    }
    if ( dataServiceMeta.isStreaming() ) {
      return new DataServiceExecutor.Builder( sql, dataServiceMeta, context )
          .rowLimit( dataServiceMeta.getRowLimit() ).timeLimit( dataServiceMeta.getTimeLimit() );
    }
    // data service not found
    return null;
  }

  private void findDataServiceProperties( String dataServiceName ) {
    this.dataServiceName = dataServiceName;

    // load recorded dataservices from local json file located in KETTLE_HOME
    File file = new File( Const.getKettleDirectory() + Const.FILE_SEPARATOR + "dataServices.json" );
    ObjectMapper mapper = new ObjectMapper();

    // get the ktr path and name for this dataservice from the loaded json file
    try {
      Map<String, LinkedHashMap<String, String>> map = mapper.readValue( file, new TypeReference<Map<String, LinkedHashMap<String, String>>>() {
      } );
      LinkedHashMap<String, String> ktrLocation = map.get( dataServiceName );
      this.ktrPath = ktrLocation.get( "ktrPath" );
      this.ktrName = ktrLocation.get( "ktrName" );
    } catch ( IOException e ) {
      logger.error( e );
    }
  }

  private DataServiceMeta getDataServiceMeta( String dataServiceName ) throws KettleException {
    DataServiceMeta dataServiceMeta = getCachedDataServiceMeta( dataServiceName );
    if ( dataServiceMeta == null ) {
      findDataServiceProperties( dataServiceName );
      dataServiceMeta = this.createDataServiceMeta();
      this.dataServiceMetaCache.put( dataServiceName, new DataServiceMetaCacheElement( dataServiceMeta ) );
    }
    return dataServiceMeta;
  }

  private DataServiceMeta getCachedDataServiceMeta( String dataServiceName ) {
    //TODO: rethink caching
    long fiveMinutesInMills = 5L * 60L * 1000L;
    DataServiceMetaCacheElement dataServiceMetaCacheElement = this.dataServiceMetaCache.get( dataServiceName );
    if ( dataServiceMetaCacheElement != null ) {
      Instant cacheInstant = dataServiceMetaCacheElement.getCacheInstant();
      if ( cacheInstant.toEpochMilli() + Instant.now().toEpochMilli() < fiveMinutesInMills ) {
        return dataServiceMetaCacheElement.getDataServiceMeta();
      }
    }
    return null;
  }

  private DataServiceMeta createDataServiceMeta() throws KettleException {
    // Connecting to the repository
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    repositoriesMeta.readData();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repoName );
    PluginRegistry registry = PluginRegistry.getInstance();
    Repository repository = registry.loadClass(
        RepositoryPluginType.class,
        repositoryMeta,
        Repository.class
    );
    repository.init( repositoryMeta );
    repository.connect( repoUsername, repoPassword );

    // Find the directory
    RepositoryDirectoryInterface rootTree = repository.loadRepositoryDirectoryTree();
    RepositoryDirectoryInterface ktrDirectory = rootTree.findDirectory( ktrPath );

    // Load the transformation
    TransMeta transMeta = repository.loadTransformation( ktrName, ktrDirectory, null, true, null );

    // Load the Metastore info and set values in dataServiceProperties
    try {
      IMetaStoreElementType iMetaStoreElementType = transMeta.getEmbeddedMetaStore().getElementTypeByName( "pentaho", "Kettle Data Service" );
      List<IMetaStoreAttribute> iMetaStoreAttributes = transMeta.getEmbeddedMetaStore().getElement( "pentaho",
          iMetaStoreElementType, this.dataServiceName ).getChildren();
      for ( IMetaStoreAttribute iMetaStoreAttribute : iMetaStoreAttributes ) {
        if ( iMetaStoreAttribute.getId().equals( "streaming" ) ) {
          this.isStreaming = iMetaStoreAttribute.getValue().toString().toUpperCase().equals( "Y" );
        }
        if ( iMetaStoreAttribute.getId().equals( "time_limit" ) ) {
          this.timeLimit = Integer.parseInt( iMetaStoreAttribute.getValue().toString() );
        }
        if ( iMetaStoreAttribute.getId().equals( "row_limit" ) ) {
          this.rowLimit = Integer.parseInt( iMetaStoreAttribute.getValue().toString() );
        }
        if ( iMetaStoreAttribute.getId().equals( "step_name" ) ) {
          this.stepName = iMetaStoreAttribute.getValue().toString();
        }
      }
    } catch ( MetaStoreException e ) {
      // Could not get the metastore
      return null;
    }

    // Get the step
    StepMeta stepMeta = transMeta.findStep( this.stepName );

    // Create the Dataservice
    DataServiceMeta dataServiceMeta = dataServiceMetaFactory.createDataService( stepMeta );
    if ( dataServiceMeta != null ) {
      dataServiceMeta.setName( this.dataServiceName );
      dataServiceMeta.setStreaming( this.isStreaming );
      dataServiceMeta.setRowLimit( this.rowLimit );
      dataServiceMeta.setTimeLimit( this.timeLimit );
    }
    return dataServiceMeta;
  }

  @Override
  public DataServiceMeta getDataService( String dataServiceName ) {
    try {
      return getDataServiceMeta( dataServiceName );
    } catch ( KettleException ignored ) {
    }

    return null;
  }

  @Override
  public List<DataServiceMeta> getDataServices( Function<Exception, Void> logger ) {
    List<DataServiceMeta> elements = new ArrayList();
    for ( DataServiceMetaCacheElement cacheElement : this.dataServiceMetaCache.values() ) {
      elements.add( cacheElement.getDataServiceMeta() );
    }
    return elements;
  }

  @Override
  public List<DataServiceMeta> getDataServices( String dataServiceName, Function<Exception, Void> logger ) {
    return this.getDataServices( logger );
  }

  @Override
  public List<String> getDataServiceNames() {
    return new ArrayList<>( this.dataServiceMetaCache.keySet() );
  }

  @Override
  public List<String> getDataServiceNames( String dataServiceName ) {
    return this.getDataServiceNames();
  }

  private class DataServiceMetaCacheElement {
    private Instant cacheInstant;
    private DataServiceMeta dataServiceMeta;

    public DataServiceMetaCacheElement( DataServiceMeta dataServiceMeta ) {
      this.cacheInstant = Instant.now();
      this.dataServiceMeta = dataServiceMeta;
    }

    public Instant getCacheInstant() {
      return cacheInstant;
    }

    public DataServiceMeta getDataServiceMeta() {
      return dataServiceMeta;
    }
  }
}
