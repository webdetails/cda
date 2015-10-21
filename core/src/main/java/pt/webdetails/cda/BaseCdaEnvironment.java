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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.config.HierarchicalConfiguration;
import org.pentaho.reporting.libraries.base.config.PropertyFileConfiguration;
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
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.messaging.PluginEvent;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
//import pt.webdetails.cpf.session.ISessionUtils;

// TODO: change bean handling, make ready for sugar version
public abstract class BaseCdaEnvironment implements ICdaEnvironment {

  protected static Log logger = LogFactory.getLog( BaseCdaEnvironment.class );

  private static final String RESOURCES_DIR = "resources";
  /**
   * file with connection and data access types
   */
  private static final String COMPONENTS_DEF = "components.properties";
  private static final String BASE_PROPERTIES = "cda.properties";


  private IBeanFactory beanFactory;

  private HierarchicalConfiguration config;

	public BaseCdaEnvironment() throws InitializationException {
		init();
	}
	
	public BaseCdaEnvironment(IBeanFactory factory) throws InitializationException {
		init(factory);
	}

	@Override
	public void init() throws InitializationException {
		initBeanFactory();
	}
	
	public void init(IBeanFactory factory) {
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

		beanFactory = new AbstractBeanFactory(){
			@Override
			public String getSpringXMLFilename(){ return "cda.spring.xml"; }
		};
	}

	@Override
	public IQueryCache getQueryCache() { //TODO: use no cache if no bean defined?
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


	@Override
	public FormulaContext getFormulaContext() {

		return new DefaultFormulaContext();
	}

  @Override
  public Properties getCdaComponents() {
    try {
      // String content = getCdaConfigFileContent("resources/components.properties");
      // StringReader sr = new StringReader(content);
      IReadAccess sysRead = getRepo().getPluginSystemReader( RESOURCES_DIR );
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


  public IContentAccessFactory getRepo() {
    return PluginEnvironment.repository();
  }

  public synchronized Configuration getBaseConfig() {
    if ( config == null ) {
      config = new HierarchicalConfiguration();
      IReadAccess sysReader = getRepo().getPluginSystemReader( "" );
      if ( sysReader.fileExists( BASE_PROPERTIES ) ) {
        PropertyFileConfiguration properties = new PropertyFileConfiguration();
        try {
          properties.load( sysReader.getFileInputStream( BASE_PROPERTIES ) );
          config.insertConfiguration( properties );
          logger.debug( BASE_PROPERTIES + " read ok." );
          
        } catch ( IOException e ) {
          logger.error( "Error reading " + BASE_PROPERTIES, e );
        }
      }
      else {
        logger.error( "Unable to load " + BASE_PROPERTIES );
      }
    }
    return config;
  }
}
