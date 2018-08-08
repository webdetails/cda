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

  public UnnamedDataServiceResolver( IDataServiceMetaFactory dataServiceMetaFactory, Context context ) {
    this.dataServiceMetaFactory = dataServiceMetaFactory;
    this.context = context;

    // TODO Inject this via blueprint, like in the other similar cases
    this.repoName = "Pentaho";
    this.repoUsername = "admin";
    this.repoPassword = "password";

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
    // TODO Don't forget to check and replicate the default values for streaming, rowLimit and timeLimit

    if ( !dataServiceName.equals( "unknown" ) ) {
      return new DataServiceProperties( dataServiceName, "/public/plugin-samples/pentaho-cdf-dd/legacy/realtime", "real_time", "Data generation", true, 1000, 1000L );
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
    StepMeta stepMeta = transMeta.findStep( dataServiceProperties.getStepName() );

    DataServiceMeta dataServiceMeta = dataServiceMetaFactory.createDataService( stepMeta );
    if ( dataServiceMeta != null ) {
      dataServiceMeta.setName( dataServiceProperties.getName() );
      dataServiceMeta.setStreaming( dataServiceProperties.isStreaming() );
      dataServiceMeta.setRowLimit( dataServiceProperties.getRowLimit() );
      dataServiceMeta.setTimeLimit( dataServiceProperties.getTimeLimit() );

      return dataServiceMeta;
    }

    return null;
  }

  private class DataServiceProperties {
    private final String name;
    private final String ktrPath;
    private final String ktrName;
    private final String stepName;
    private final boolean streaming;
    private final int rowLimit;
    private final long timeLimit;

    private DataServiceProperties( String name, String ktrPath, String ktrName, String stepName, boolean streaming, int rowLimit, long timeLimit ) {
      this.name = name;

      this.ktrPath = ktrPath;
      this.ktrName = ktrName;
      this.stepName = stepName;

      this.streaming = streaming;
      this.rowLimit = rowLimit;
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
