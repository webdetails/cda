package pt.webdetails.cda;

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.core.states.datarow.EmptyTableModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.CubeFileProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;

import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.ICacheScheduleManager;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.IMondrianRoleMapper;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import pt.webdetails.cda.dataaccess.DefaultCubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.DefaultDataAccessUtils;
import pt.webdetails.cda.dataaccess.ICubeFileProviderSetter;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.settings.DefaultResourceKeyGetter;
import pt.webdetails.cda.settings.IResourceKeyGetter;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.impl.DefaultRepositoryFile;
import pt.webdetails.cpf.impl.DummyInterPluginCall;
import pt.webdetails.cpf.impl.DummySessionUtils;
import pt.webdetails.cpf.messaging.IEventPublisher;
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
		// PENTAHO
		// SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession()));
		init();
	}
	
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

		public KettleTransformationProducer getKettleTransformationProducer(

			TransFromFileConnectionInfo connectionInfo, String query) {
	      return new KettleTransFromFileProducer("",
	              connectionInfo.getTransformationFile(),
	              query, null, null, connectionInfo.getDefinedArgumentNames(),
	              connectionInfo.getDefinedVariableNames());
//	    PENTAHO
//	    return new PentahoKettleTransFromFileProducer("",
//	            connectionInfo.getTransformationFile(),
//	            query, null, null, connectionInfo.getDefinedArgumentNames(),
//	            connectionInfo.getDefinedVariableNames());

	}


	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getCubeFileProvider(org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory, pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo)
	 */
	public CubeFileProvider getCubeFileProvider(
			AbstractNamedMDXDataFactory mdxDataFactory,
			MondrianConnectionInfo mondrianConnectionInfo) {

	    return new DefaultCubeFileProvider(mondrianConnectionInfo.getCatalog());
	    
//		PENTAHO
//	      mdxDataFactory.setCubeFileProvider(new PentahoCubeFileProvider(mondrianConnectionInfo.getCatalog()));
//	      try
//	      {
//	        mdxDataFactory.setMondrianConnectionProvider((MondrianConnectionProvider) PentahoSystem.getObjectFactory().get(PentahoMondrianConnectionProvider.class, "MondrianConnectionProvider", null));
//	      }
//	      catch (ObjectFactoryException e)
//	      {//couldn't get object
//	        mdxDataFactory.setMondrianConnectionProvider(new PentahoMondrianConnectionProvider());
//	      }


	}

	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getPmdConnectionProvider()
	 */
	public PmdConnectionProvider getPmdConnectionProvider() {
		return new PmdConnectionProvider();
//		PENTAHO
//		return new new PentahoPmdConnectionProvider();

	}

	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getJndiConnectionProvider(pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo)
	 */
	public ConnectionProvider getJndiConnectionProvider(SqlJndiConnectionInfo connectionInfo) {

	      final JndiConnectionProvider provider = new JndiConnectionProvider();
	      provider.setConnectionPath(connectionInfo.getJndi());
	      provider.setUsername(connectionInfo.getUser());
	      provider.setPassword(connectionInfo.getPass());
	      return provider;
//	      PENTAHO
//	      final PentahoJndiDatasourceConnectionProvider provider = new PentahoJndiDatasourceConnectionProvider();
//	      provider.setJndiName(connectionInfo.getJndi());
//	      provider.setUsername(connectionInfo.getUser());
//	      provider.setPassword(connectionInfo.getPass());
//	      return provider;
	}

	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getMondrianJndiDatasourceProvider(pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo)
	 */
	public DataSourceProvider getMondrianJndiDatasourceProvider(
			MondrianJndiConnectionInfo connectionInfo) {

		return new JndiDataSourceProvider(connectionInfo.getJndi());
	    
//		PENTAHO
//	      return new PentahoMondrianDataSourceProvider(connectionInfo.getJndi());
	}

	/* TODO: REMOVE - using DataAccessUtilsInstead
	public ReportEnvironmentDataRow getReportEnvironmentDataRow(
			Configuration configuration) {

		return new ReportEnvironmentDataRow(new DefaultReportEnvironment(configuration));
        
//		PENTAHO - the commented section below is really commented
	      //TODO:testing, TEMP
		//        // Make sure we have the env. correctly inited
		//        if (SolutionReposHelper.getSolutionRepositoryThreadVariable() == null && PentahoSystem.getObjectFactory().objectDefined(ISolutionRepository.class.getSimpleName()))
		//        {
		//          threadVarSet = true;
		//          SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession()));
		//        }
//		          environmentDataRow = new ReportEnvironmentDataRow(new PentahoReportEnvironment(configuration));
//		        }

     
	}
	*/

	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getQueryCache()
	 */
	public IQueryCache getQueryCache() {
		try {
			return (IQueryCache) beanFactory.getBean("IQueryCache");
		} catch (Exception e) {
			logger.error("Cannot get bean IQueryCache. Using EHCacheQueryCache", e);
		}

		return new EHCacheQueryCache();
		
//		PENTAHO
//	      try {
//	          cache = PluginUtils.getPluginBean("cda.", IQueryCache.class);
//	        } catch (Exception e) {
//	          logger.error(e.getMessage());
//	        }
//	        if(cache == null){
//	          //fallback
//	          cache = new EHCacheQueryCache(); 
//	        }

	}

	public String getCdaConfigFileContent(String fileName) {
		byte[] content = getCdaConfigFile(fileName);
		return new String(content);
	}
	
	public byte[] getCdaConfigFile(String fileName) {
		try {
			IRepositoryAccess repo = getRepositoryAccess();
			IRepositoryFile ir = repo.getSettingsFile(fileName, FileAccess.READ);
			if (ir != null && ir.exists()) {
				return ir.getData();
			}

			URL is = CdaBoot.class.getResource(fileName);
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

	/* (non-Javadoc)
	 * @see pt.webdetails.cda.ICdaEnvironment#getFormulaContext()
	 */
	public ICdaCoreSessionFormulaContext getFormulaContext() {
		
		try {
			return (ICdaCoreSessionFormulaContext) beanFactory.getBean("ICdaCoreSessionFormulaContext");
		} catch (Exception e) {
			logger.error("Cannot get bean ICdaCoreSessionFormulaContext. Using DefaultCdaCoreSessionFormulaContext", e);
		}
		return new DefaultSessionFormulaContext();
	}

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
	

	  public void getCdaList(final OutputStream out, final DiscoveryOptions discoveryOptions) throws Exception
	  {

		final TableModel tableModel = new EmptyTableModel();
		ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);
		// PENTAHO
	    //final TableModel tableModel = SolutionRepositoryUtils.getInstance().getCdaList(userSession);
	    //ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);

	  }


    public IEventPublisher getEventPublisher() {
      return null;
      
      //PENTAHO
//      return new IEventPublisher() {
//        @Override
//        public void publishEvent(CdaEvent event) {
//          EventPublisher.getPublisher.publish(event);
//        }      
//      };
    }

	@Override
	public void setMdxDataFactoryBaseConnectionProperties(
			MondrianConnection connection,
			AbstractNamedMDXDataFactory mdxDataFactory) {
		// TODO Auto-generated method stub
		

		 
//	    if (!CdaEngine.isStandalone())
//	    {
//	      IMondrianCatalogService catalogService =
//	          PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", null);;
//	      final List<MondrianCatalog> catalogs =
//	          catalogService.listCatalogs(PentahoSessionHolder.getSession(), false);
//
//	      MondrianCatalog catalog = null;
//	      for (MondrianCatalog cat : catalogs)
//	      {
//	        final String definition = cat.getDefinition();
//	        final String definitionFileName = IOUtils.getInstance().getFileName(definition);
//	        if (definitionFileName.equals(IOUtils.getInstance().getFileName(connection.getConnectionInfo().getCatalog())))
//	        {
//	          catalog = cat;
//	          break;
//	        }
//	      }
//	      
//	      if ( catalog != null){
//	        
//	        Properties props = new Properties();
//	        try
//	        {
//	          props.load(new StringReader(catalog.getDataSourceInfo().replace(';', '\n')));
//	          try
//	          {
//	            // Apply the method through reflection
//	            Method m = AbstractNamedMDXDataFactory.class.getMethod("setBaseConnectionProperties",Properties.class);
//	            m.invoke(mdxDataFactory, props);
//	            
//	          }
//	          catch (Exception ex)
//	          {
//	            // This is a previous version - continue
//	          }
//	          
//	          
//	        }
//	        catch (IOException ex)
//	        {
//	          logger.warn("Failed to transform DataSourceInfo string '"+ catalog.getDataSourceInfo() +"' into properties");
//	        }
//	        
//	      }
//	      
//	      
//	    }
//	    
//	    
		
	}

	@Override
	public ISessionUtils getSessionUtils() {
		return new DummySessionUtils();
	}


	@Override
	public IMondrianRoleMapper getMondrianRoleMapper() {
		return new IMondrianRoleMapper() {
			
			@Override
			public String getRoles(String catalog) {
				return "";
			}
		};
	}

	@Override
	public IRepositoryAccess getRepositoryAccess() {
		return (IRepositoryAccess) beanFactory.getBean("IRepositoryAccess");
	}

	@Override
	public ICubeFileProviderSetter getCubeFileProviderSetter() {
		return new DefaultCubeFileProviderSetter();
		
	}

	@Override
	public IDataAccessUtils getDataAccessUtils() {
		try {
			return (IDataAccessUtils) beanFactory.getBean("IDataAccessUtils");
		} catch (Exception e) {
			logger.error("Cannot get bean IDataAccessUtils. Using DefaultDataAccessUtils", e);
		}
		return new DefaultDataAccessUtils();
	}

	@Override
	public IResourceKeyGetter getResourceKeyGetter() {
		try {
			return (IResourceKeyGetter) beanFactory.getBean("IResourceKeyGetter");
		} catch (Exception e) {
			logger.error("Cannot get bean IDataAccessUtils. Using DefaultResourceKeyGetter", e);
		}
	      // add the runtime context so that PentahoResourceData class can get access
	      // to the solution repo

		return new DefaultResourceKeyGetter();
	}

	@Override
	public IPluginCall createPluginCall(String plugin, String method, Map<String, Object> params) {
		IPluginCall pluginCall = new DummyInterPluginCall();
		pluginCall.init(new Plugin(plugin), method, params);
		return pluginCall;
	}

	@Override
	public boolean supportsCacheScheduler() {
		return false;
	}

	@Override
	public ICacheScheduleManager getCacheScheduler() {
		return null;
	}

}
