package pt.webdetails.cda;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.ICacheScheduleManager;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.dataaccess.DefaultCubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.DefaultDataAccessUtils;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.formula.DefaultSessionFormulaContext;
import pt.webdetails.cda.formula.ICdaCoreSessionFormulaContext;
import pt.webdetails.cda.settings.DefaultResourceKeyGetter;
import pt.webdetails.cda.settings.IResourceKeyGetter;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.impl.DefaultRepositoryFile;
import pt.webdetails.cpf.impl.DummyInterPluginCall;
import pt.webdetails.cpf.impl.SimpleSessionUtils;
import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.messaging.PluginEvent;
import pt.webdetails.cpf.plugin.Plugin;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.session.ISessionUtils;
import edu.emory.mathcs.backport.java.util.Arrays;

public class DefaultCdaEnvironment implements ICdaEnvironment {

	private static Log logger = LogFactory.getLog(DefaultCdaEnvironment.class);

	private ICdaBeanFactory beanFactory;

	public DefaultCdaEnvironment() throws InitializationException {
		init();
	}

	@Override
	public void init() throws InitializationException {
		initBeanFactory();
	}


	private void initBeanFactory() throws InitializationException {
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

		beanFactory = new CoreBeanFactory();


	}

	@Override
	public IQueryCache getQueryCache() {
		try {
			String id = "IQueryCache";
			if (beanFactory.containsBean(id)) {
				return (IQueryCache) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean IQueryCache. Using EHCacheQueryCache", e);
		}
		return new EHCacheQueryCache();
	}

	private String getCdaConfigFileContent(String fileName) {
		byte[] content = getCdaConfigFile(fileName);
		return new String(content);
	}

	@Override
	public byte[] getCdaConfigFile(String fileName) {
		try {
			IRepositoryAccess repo = getRepositoryAccess();
			if (repo != null) {
				IRepositoryFile ir = repo.getSettingsFile(fileName, FileAccess.READ);
				if (ir != null && ir.exists()) {
					return ir.getData();
				}
			}
			URL is = this.getClass().getClassLoader().getResource(fileName);
			if (is != null) {
				File f = new File(is.toURI());
				if (f.exists() && f.canRead())
					return FileUtils.readFileToByteArray(f);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return new byte[0];		
	}

	@Override
	public ICdaCoreSessionFormulaContext getFormulaContext() {

		try {
			String id ="ICdaCoreSessionFormulaContext";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				return (ICdaCoreSessionFormulaContext) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean ICdaCoreSessionFormulaContext. Using DefaultCdaCoreSessionFormulaContext", e);
		}
		return new DefaultSessionFormulaContext(null);
	}

	@Override
	public Properties getCdaComponents() {
		try {
			String content = getCdaConfigFileContent("components.properties");
			StringReader sr = new StringReader(content);
			Properties pr = new Properties();
			pr.load(sr);
			return pr;
		} catch(Exception e) {
			logger .error("Cannot load components.properties");
		}
		return new Properties();
	}

	@Override
	public List<IRepositoryFile> getComponentsFiles() {
		Properties resources = getCdaComponents();
		String[] connections = StringUtils.split(StringUtils.defaultString(resources.getProperty("connections")), ",");
		IRepositoryAccess repo = getRepositoryAccess();
		List<IRepositoryFile> componentsFiles = new ArrayList<IRepositoryFile>();

		if (repo != null) {
			IRepositoryFile[] repoFiles = repo.getSettingsFileTree("resources/components/connections", "xml",FileAccess.READ);
			if (repoFiles != null && repoFiles.length > 0)
				componentsFiles = Arrays.asList(repoFiles);
		} 
		// Ok we couldn't find the files in the repository - lets try the classpath
		if (repo == null || componentsFiles.size() < 1) {
			if(connections != null) {
				for(String con : connections) {
					try {
						URL conUrl = CdaEngine.class.getResource("resources/components/connections/" + con + ".xml");
						if (conUrl != null) {
							File conFile = new File(conUrl.toURI());
							if (conFile.exists()) {
								IRepositoryFile ir = new DefaultRepositoryFile(conFile);
								componentsFiles.add(ir);
							}
						}
					} catch(Exception e) {
						logger.debug("Cant access connections file for: " + con);
					}
				}
			}
		}
		return componentsFiles;
	}


	@Override
	public IEventPublisher getEventPublisher() {
		String id = "IEventPublisher";
		if (beanFactory != null && beanFactory.containsBean(id)) {
			return (IEventPublisher) beanFactory.getBean(id);
		}

		
		return new IEventPublisher() {

			@Override
			public void publish(PluginEvent arg0) {
				logger.debug("Event: " + arg0.getKey() + " : " + arg0.getName() + "\n" + arg0.toString());

			}
		};
	}

	@Override
	public ISessionUtils getSessionUtils() {
		String id = "ISessionUtils";
		if (beanFactory != null && beanFactory.containsBean(id)) {
			return (ISessionUtils) beanFactory.getBean(id);
		}
		SimpleUserSession su = new SimpleUserSession("", new String[0], false,  null);
		return new SimpleSessionUtils(su, new String[0], new String[0]);
	}


	@Override
	public IMondrianRoleMapper getMondrianRoleMapper() {
		String id = "IMondrianRoleMapper";
		if (beanFactory != null && beanFactory.containsBean(id)) {
			return (IMondrianRoleMapper) beanFactory.getBean(id);
		}
		logger.warn("Cannot get bean IMondrianRoleMapper. Using pseudo MondrianRoleMapper");

		return new IMondrianRoleMapper() {

			@Override
			public String getRoles(String catalog) {
				return "";
			}
		};
	}

	@Override
	public IRepositoryAccess getRepositoryAccess() {
		String id = "IRepositoryAccess";
		if (beanFactory != null && beanFactory.containsBean(id)) {
			IRepositoryAccess repAccess =  (IRepositoryAccess) beanFactory.getBean(id);
			repAccess.setPlugin(Plugin.CDA);
			return repAccess;
		}

		return null;
	}

	@Override
	public ICubeFileProviderSetter getCubeFileProviderSetter() {
		try {
			String id = "ICubeFileProviderSetter";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				return (ICubeFileProviderSetter) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean ICubeFileProviderSetter. Using DefaultCubeFileProviderSetter", e);
		}
		return new DefaultCubeFileProviderSetter();

	}

	@Override
	public IDataAccessUtils getDataAccessUtils() {
		try {
			String id = "IDataAccessUtils";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				return (IDataAccessUtils) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean IDataAccessUtils. Using DefaultDataAccessUtils", e);
		}
		return new DefaultDataAccessUtils();
	}

	@Override
	public IResourceKeyGetter getResourceKeyGetter() {
		try {
			String id = "IResourceKeyGetter";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				return (IResourceKeyGetter) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean IResourceKeyGetter. Using DefaultResourceKeyGetter", e);
		}
		// add the runtime context so that PentahoResourceData class can get access
		// to the solution repo

		return new DefaultResourceKeyGetter();
	}

	@Override
	public IPluginCall createPluginCall(String plugin, String method, Map<String, Object> params) {
		try {
			String id = "IPluginCall";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				IPluginCall pc = (IPluginCall) beanFactory.getBean(id);
				pc.init(new Plugin(plugin), method,  params);
				return pc;
			}
		} catch (Exception e) {
			logger.error("Cannot get bean IPluginCall. Using DummyInterPluginCall", e);
		}
		IPluginCall pluginCall = new DummyInterPluginCall();
		pluginCall.init(new Plugin(plugin), method, params);
		return pluginCall;
	}

	@Override
	public boolean supportsCacheScheduler() {
		if (beanFactory != null && beanFactory.containsBean("ICacheScheduleManager")) {
			return true;
		}
		return false;
	}

	@Override
	public ICacheScheduleManager getCacheScheduler() {
		try {
			String id = "ICacheScheduleManager";
			if (beanFactory != null && beanFactory.containsBean(id)) {
				return (ICacheScheduleManager) beanFactory.getBean(id);
			}
		} catch (Exception e) {
			logger.error("Cannot get bean ICacheScheduleManager. Not using a cache schedule manager", e);
		}
		return null;

	}

}
