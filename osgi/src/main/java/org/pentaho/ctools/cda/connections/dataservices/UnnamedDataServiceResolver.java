package org.pentaho.ctools.cda.connections.dataservices;

import com.google.common.base.Function;
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
import org.pentaho.di.trans.dataservice.DataServiceExecutor.Builder;
import org.pentaho.di.trans.dataservice.DataServiceMeta;
import org.pentaho.di.trans.dataservice.IDataServiceMetaFactory;
import org.pentaho.di.trans.dataservice.resolvers.DataServiceResolver;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UnnamedDataServiceResolver implements DataServiceResolver {
  private IDataServiceMetaFactory dataServiceMetaFactory;
  private Context context;

  private final String repoName;
  private final String repoUsername;
  private final String repoPassword;

  private final HashMap<String, DataServiceMeta> dataServiceMetaCache;

  public UnnamedDataServiceResolver( IDataServiceMetaFactory dataServiceMetaFactory, Context context, String repositoryName, String username, String password ) {
    this.dataServiceMetaFactory = dataServiceMetaFactory;
    this.context = context;

    this.repoName = repositoryName;
    this.repoUsername = username;
    this.repoPassword = password;

    this.dataServiceMetaCache = new HashMap<>();
  }

  @Override
  public Builder createBuilder( SQL sql ) throws KettleException {
    final String dataServiceName = sql.getServiceName();

    DataServiceMeta dataServiceMeta = getDataServiceMeta( dataServiceName );

    if ( dataServiceMeta != null ) {
      if ( dataServiceMeta.isStreaming() ) {
        return new DataServiceExecutor.Builder( sql, dataServiceMeta, context )
            .rowLimit( dataServiceMeta.getRowLimit() ).timeLimit( dataServiceMeta.getTimeLimit() );
      }

      return new DataServiceExecutor.Builder( sql, dataServiceMeta, context );
    }

    // data service not found
    return null;
  }

  private DataServiceProperties findDataServiceProperties( String dataServiceName ) {
    // TODO All the logic of data service registration and discover

    if ( dataServiceName.equals( "plugin_sample_real_time_require" ) ) {
      return new DataServiceProperties( dataServiceName, "/public/plugin-samples/pentaho-cdf-dd/realtime", "real_time" );
    }

    if ( dataServiceName.equals( "ricardo" ) || dataServiceName.equals( "nelson" ) ) {
      return new DataServiceProperties( dataServiceName, "/public", "mega" );
    }

    return null;
  }

  private DataServiceMeta getDataServiceMeta( String dataServiceName ) throws KettleException {
    // TODO All the logic of caching

    DataServiceMeta dataServiceMeta = this.dataServiceMetaCache.get( dataServiceName );
    if ( dataServiceMeta == null ) {
      DataServiceProperties dataServiceProperties = findDataServiceProperties( dataServiceName );

      if ( dataServiceProperties != null ) {
        dataServiceMeta = this.createDataServiceMeta( dataServiceProperties );
        this.dataServiceMetaCache.put( dataServiceName, dataServiceMeta );
      }
    }

    return dataServiceMeta;
  }

  private DataServiceMeta createDataServiceMeta( DataServiceProperties dataServiceProperties ) throws KettleException {
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
    RepositoryDirectoryInterface ktrDirectory = rootTree.findDirectory( dataServiceProperties.getKtrPath() );

    // Load the transformation
    TransMeta transMeta = repository.loadTransformation( dataServiceProperties.getKtrName(), ktrDirectory, null, true, null );

    // Load the Metastore info and set values in dataServiceProperties
    try {
      IMetaStoreElementType iMetaStoreElementType = transMeta.getEmbeddedMetaStore().getElementTypeByName( "pentaho", "Kettle Data Service" );
      List<IMetaStoreAttribute> iMetaStoreAttributes = iMetaStoreAttributes = transMeta.getEmbeddedMetaStore().getElement( "pentaho",
          iMetaStoreElementType, dataServiceProperties.getName() ).getChildren();
      for ( IMetaStoreAttribute iMetaStoreAttribute : iMetaStoreAttributes ) {
        if ( iMetaStoreAttribute.getId().equals( "streaming" ) ) {
          dataServiceProperties.setStreaming( iMetaStoreAttribute.getValue().toString().toUpperCase().equals( "Y" ) );
        }
        if ( iMetaStoreAttribute.getId().equals( "time_limit" ) ) {
          dataServiceProperties.setTimeLimit( Integer.parseInt( iMetaStoreAttribute.getValue().toString() ) );
        }
        if ( iMetaStoreAttribute.getId().equals( "row_limit" ) ) {
          dataServiceProperties.setRowLimit( Integer.parseInt( iMetaStoreAttribute.getValue().toString() ) );
        }
        if ( iMetaStoreAttribute.getId().equals( "step_name" ) ) {
          dataServiceProperties.setStepName( iMetaStoreAttribute.getValue().toString() );
        }
      }
    } catch ( MetaStoreException e ) {
      // exception muffler
    }

    // Get the step
    StepMeta stepMeta = transMeta.findStep( dataServiceProperties.getStepName() );

    DataServiceMeta dataServiceMeta = dataServiceMetaFactory.createDataService( stepMeta ); // try use createStreamingDataService ??? are we always streaming???
    if ( dataServiceMeta != null ) {
      dataServiceMeta.setName( dataServiceProperties.getName() );
      dataServiceMeta.setStreaming( dataServiceProperties.streaming );
      dataServiceMeta.setRowLimit( dataServiceProperties.rowLimit );
      dataServiceMeta.setTimeLimit( dataServiceProperties.timeLimit );
      return dataServiceMeta;
    }

    return null;
  }

  private class DataServiceProperties {
    private String name;
    private String ktrPath;
    private String ktrName;
    private String stepName;
    private boolean streaming;
    private int rowLimit;
    private long timeLimit;

    private DataServiceProperties( String name, String ktrPath, String ktrName ) {
      this.name = name;
      this.ktrPath = ktrPath;
      this.ktrName = ktrName;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public void setKtrPath( String ktrPath ) {
      this.ktrPath = ktrPath;
    }

    public void setKtrName( String ktrName ) {
      this.ktrName = ktrName;
    }

    public void setStepName( String stepName ) {
      this.stepName = stepName;
    }

    public void setStreaming( boolean streaming ) {
      this.streaming = streaming;
    }

    public void setRowLimit( int rowLimit ) {
      this.rowLimit = rowLimit;
    }

    public void setTimeLimit( long timeLimit ) {
      this.timeLimit = timeLimit;
    }

    String getName() {
      return this.name;
    }

    String getKtrPath() {
      return ktrPath;
    }

    String getKtrName() {
      return ktrName;
    }

    String getStepName() {
      return stepName;
    }

    boolean isStreaming() {
      return streaming;
    }

    int getRowLimit() {
      return rowLimit;
    }

    long getTimeLimit() {
      return timeLimit;
    }
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
    return new ArrayList<>( this.dataServiceMetaCache.values() );
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
}
