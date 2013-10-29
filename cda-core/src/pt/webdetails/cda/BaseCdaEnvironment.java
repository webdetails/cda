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

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.dataaccess.DefaultCubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.DefaultDataAccessUtils;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
//import pt.webdetails.cda.formula.DefaultSessionFormulaContext;
//import pt.webdetails.cda.settings.DefaultResourceKeyGetter;
//import pt.webdetails.cda.settings.IResourceKeyGetter;
import pt.webdetails.cpf.PluginEnvironment;
//import pt.webdetails.cpf.impl.SimpleSessionUtils;
//import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.messaging.PluginEvent;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
//import pt.webdetails.cpf.session.ISessionUtils;

// TODO: change bean handling, make ready for sugar version
public class BaseCdaEnvironment implements ICdaEnvironment {

  protected static Log logger = LogFactory.getLog( BaseCdaEnvironment.class );

  private static final String RESOURCES_DIR = "resources";
  /**
   * file with connection and data access types
   */
  private static final String COMPONENTS_DEF = "components.properties";


	private ICdaBeanFactory beanFactory;

	public BaseCdaEnvironment() throws InitializationException {
		init();
	}
	
	public BaseCdaEnvironment(ICdaBeanFactory factory) throws InitializationException {
		init(factory);
	}

	@Override
	public void init() throws InitializationException {
		initBeanFactory();
	}
	
	public void init(ICdaBeanFactory factory) {
		this.beanFactory = factory;
	}


	private void initBeanFactory() throws InitializationException {
//		//Get beanFactory
//		String className = CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.beanFactoryClass" );
//
//		if (className != null && !className.isEmpty()) {
//			try {
//				final Class<?> clazz;
//				clazz = Class.forName(className);
//				if (!ICdaBeanFactory.class.isAssignableFrom(clazz)) {
//					throw new InitializationException (
//							"Plugin class specified by property pt.webdetails.cda.beanFactoryClass "
//									+ " must implement "
//									+ ICdaBeanFactory.class.getName(), null);
//				}
//				beanFactory = (ICdaBeanFactory) clazz.newInstance();
//			} catch (ClassNotFoundException e) {
//				String errorMessage = "Class not found when loading bean factory " + className;
//				logger.error(errorMessage, e);
//				throw new InitializationException(errorMessage, e); 
//			} catch (IllegalAccessException e) {
//				String errorMessage = "Illegal access when loading bean factory from " + className;
//				logger.error(errorMessage, e);
//				throw new InitializationException(errorMessage, e); 
//			} catch (InstantiationException e) {
//				String errorMessage = "Instantiation error when loading bean factory from " + className;
//				logger.error(errorMessage, e);
//				throw new InitializationException(errorMessage, e); 
//			}
//		}

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

//	/**
//	 * @deprecated
//	 */
//	private String getCdaConfigFileContent(String fileName) {
//		byte[] content = getCdaConfigFile(fileName);
//		return new String(content);
//	}
//
//	public byte[] getCdaConfigFile( String fileName ) {
//	  throw new NotImplementedException();
//	}
//	@Override
//	public byte[] getCdaConfigFile(String fileName) {
//		try {
//			IRepositoryAccess repo = getRepositoryAccess();
//			if (repo != null) {
//				IRepositoryFile ir = repo.getSettingsFile(fileName, FileAccess.READ);
//				if (ir != null && ir.exists()) {
//					return ir.getData();
//				}
//			}
//			URL is = this.getClass().getClassLoader().getResource(fileName);
//			if (is != null) {
//				File f = new File(is.toURI());
//				if (f.exists() && f.canRead())
//					return FileUtils.readFileToByteArray(f);
//			}
//		} catch (Exception e) {
//			logger.error(e);
//		}
//		return new byte[0];		
//	}

	@Override
	public FormulaContext getFormulaContext() {

//		try {
//			String id ="ICdaCoreSessionFormulaContext";
//			if (beanFactory != null && beanFactory.containsBean(id)) {
//				return (FormulaContext) beanFactory.getBean(id);
//			}
//		} catch (Exception e) {
//			logger.error("Cannot get bean ICdaCoreSessionFormulaContext. Using DefaultFormulaContext", e);
//		}
		return new DefaultFormulaContext();
//		return new DefaultSessionFormulaContext(null);
	}

  @Override
  public Properties getCdaComponents() {
    try {
      // String content = getCdaConfigFileContent("resources/components.properties");
      // StringReader sr = new StringReader(content);
      IReadAccess sysRead = getRepo().getPluginRepositoryReader( RESOURCES_DIR );
      Properties pr = new Properties();
      // file with connection and data access types
      InputStream propertiesFile = null;
      try {
        propertiesFile = sysRead.getFileInputStream( COMPONENTS_DEF );
        pr.load( propertiesFile );
      }
      finally {
        IOUtils.closeQuietly( propertiesFile );
      }
      return pr;
    } catch ( Exception e ) {
      logger.error( "Cannot load " + COMPONENTS_DEF );
    }
    return new Properties();
  }

//	@Override
//	public List<IRepositoryFile> getComponentsFiles() {
//		Properties resources = getCdaComponents();
//		String[] connections = StringUtils.split(StringUtils.defaultString(resources.getProperty("connections")), ",");
//		IRepositoryAccess repo = getRepositoryAccess();
//		List<IRepositoryFile> componentsFiles = new ArrayList<IRepositoryFile>();
//
//		if (repo != null) {
//			IRepositoryFile[] repoFiles = repo.getSettingsFileTree("resources/components/connections", "xml",FileAccess.READ);
//			if (repoFiles != null && repoFiles.length > 0)
//				componentsFiles = Arrays.asList(repoFiles);
//		} 
//		// Ok we couldn't find the files in the repository - lets try the classpath
//		if (repo == null || componentsFiles.size() < 1) {
//			if(connections != null) {
//				for(String con : connections) {
//					try {
//						URL conUrl = CdaEngine.class.getResource("resources/components/connections/" + con + ".xml");
//						if (conUrl != null) {
//							File conFile = new File(conUrl.toURI());
//							if (conFile.exists()) {
//								IRepositoryFile ir = new DefaultRepositoryFile(conFile);
//								componentsFiles.add(ir);
//							}
//						}
//					} catch(Exception e) {
//						logger.debug("Cant access connections file for: " + con);
//					}
//				}
//			}
//		}
//		return componentsFiles;
//	}


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

//	@Override
//	public ISessionUtils getSessionUtils() {
//		String id = "ISessionUtils";
//		if (beanFactory != null && beanFactory.containsBean(id)) {
//			return (ISessionUtils) beanFactory.getBean(id);
//		}
//		SimpleUserSession su = new SimpleUserSession("", new String[0], false,  null);
//		return new SimpleSessionUtils(su, new String[0], new String[0]);
//	}


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

//	@Override
//	public IRepositoryAccess getRepositoryAccess() {
//		String id = "IRepositoryAccess";
//		if (beanFactory != null && beanFactory.containsBean(id)) {
//			IRepositoryAccess repAccess =  (IRepositoryAccess) beanFactory.getBean(id);
//			repAccess.setPlugin(CorePlugin.CDA);
//			return repAccess;
//		}
//
//		return null;
//	}

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

//	@Override
//	public IPluginCall createPluginCall(String plugin, String method, Map<String, Object> params) {
//		try {
//			String id = "IPluginCall";
//			if (beanFactory != null && beanFactory.containsBean(id)) {
//				IPluginCall pc = (IPluginCall) beanFactory.getBean(id);
//				pc.init(new CorePlugin(plugin), method,  params);
//				return pc;
//			}
//                        throw new UnsupportedOperationException("Couldn't get bean factory.");
//		} catch (Exception e) {
//                    throw new UnsupportedOperationException("Couldn't create plugin call for " + plugin + ",method: " + method);
//		}
//	}

  public IContentAccessFactory getRepo() {
    return PluginEnvironment.repository();
  }

  public Configuration getBaseConfig() {
    return CdaBoot.getInstance().getGlobalConfig();
  }
}
