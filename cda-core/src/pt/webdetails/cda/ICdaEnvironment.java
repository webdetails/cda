package pt.webdetails.cda;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.CubeFileProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;

import pt.webdetails.cda.cache.ICacheScheduleManager;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.settings.IResourceKeyGetter;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.session.ISessionUtils;


public interface ICdaEnvironment {
	
	public void init() throws InitializationException;

	public KettleTransformationProducer getKettleTransformationProducer(
			TransFromFileConnectionInfo connectionInfo, String query);

	public CubeFileProvider getCubeFileProvider(
			AbstractNamedMDXDataFactory mdxDataFactory,
			MondrianConnectionInfo mondrianConnectionInfo);

	public PmdConnectionProvider getPmdConnectionProvider();

	public ConnectionProvider getJndiConnectionProvider(
			SqlJndiConnectionInfo connectionInfo);

	public DataSourceProvider getMondrianJndiDatasourceProvider(
			MondrianJndiConnectionInfo connectionInfo);

	public IQueryCache getQueryCache();

	public IMondrianRoleMapper getMondrianRoleMapper();

	public byte[] getCdaConfigFile(String fileName);

	public ICdaCoreSessionFormulaContext getFormulaContext();

	public Properties getCdaComponents();
	
	public List<IRepositoryFile> getComponentsFiles();

	public IEventPublisher getEventPublisher();

	public void setMdxDataFactoryBaseConnectionProperties(MondrianConnection connection, AbstractNamedMDXDataFactory mdxDataFactory);

	public ISessionUtils getSessionUtils();

	public IRepositoryAccess getRepositoryAccess();

	public ICubeFileProviderSetter getCubeFileProviderSetter();

	public IDataAccessUtils getDataAccessUtils();

	public IResourceKeyGetter getResourceKeyGetter();

	public IPluginCall createPluginCall(String plugin, String method, Map<String, Object> params);
	
	public boolean supportsCacheScheduler();
	
	public ICacheScheduleManager getCacheScheduler();

}