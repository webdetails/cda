/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jfree.io.IOUtils;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.platform.plugin.connection.PentahoJndiDatasourceConnectionProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoKettleTransFromFileProducer;
import org.pentaho.reporting.platform.plugin.connection.PentahoPmdConnectionProvider;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import org.pentaho.reporting.platform.plugin.PentahoReportEnvironment;


public class PentahoDataAccessUtils implements IDataAccessUtils {

  private static final Log logger = LogFactory.getLog(PentahoDataAccessUtils.class);
  
  @Override
  public ReportEnvironmentDataRow createEnvironmentDataRow(Configuration configuration) {
    return new ReportEnvironmentDataRow(new PentahoReportEnvironment(configuration));   
  }
  
  
  @Override
  public void setConnectionProvider(PmdDataFactory returnDataFactory) {
    returnDataFactory.setConnectionProvider(new PentahoPmdConnectionProvider());
  }
  
  
  @Override
  public void setMdxDataFactoryBaseConnectionProperties(MondrianConnection connection, AbstractNamedMDXDataFactory mdxDataFactory) {
      IMondrianCatalogService catalogService =
          PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", null);
      final List<MondrianCatalog> catalogs =
          catalogService.listCatalogs(PentahoSessionHolder.getSession(), false);

      MondrianCatalog catalog = null;
      for (MondrianCatalog cat : catalogs)
      {
        final String definition = cat.getDefinition();
        final String definitionFileName = IOUtils.getInstance().getFileName(definition);
        if (definitionFileName.equals(IOUtils.getInstance().getFileName(connection.getConnectionInfo().getCatalog())))
        {
          catalog = cat;
          break;
        }
      }
      
      if ( catalog != null){
        
        Properties props = new Properties();
        try
        {
          props.load(new StringReader(catalog.getDataSourceInfo().replace(';', '\n')));
          try
          {
            // Apply the method through reflection
            Method m = AbstractNamedMDXDataFactory.class.getMethod("setBaseConnectionProperties",Properties.class);
            m.invoke(mdxDataFactory, props);
            
          }
          catch (Exception ex)
          {
            // This is a previous version - continue
          }
          
          
        }
        catch (IOException ex)
        {
          logger.warn("Failed to transform DataSourceInfo string '"+ catalog.getDataSourceInfo() +"' into properties");
        }
        
      }
  }
  
	@Override
	public KettleTransformationProducer createKettleTransformationProducer(TransFromFileConnectionInfo connectionInfo, String query) 
	{
		return new PentahoKettleTransFromFileProducer("",
				connectionInfo.getTransformationFile(),
				query, null, null, connectionInfo.getDefinedArgumentNames(),
				connectionInfo.getDefinedVariableNames());
	}
	
	@Override
	public ConnectionProvider getJndiConnectionProvider(SqlJndiConnectionInfo connectionInfo) {
		final PentahoJndiDatasourceConnectionProvider provider = new PentahoJndiDatasourceConnectionProvider();
		provider.setJndiName(connectionInfo.getJndi());
		provider.setUsername(connectionInfo.getUser());
		provider.setPassword(connectionInfo.getPass());
		return provider;
	}
	
	@Override
	public DataSourceProvider getMondrianJndiDatasourceProvider(MondrianJndiConnectionInfo connectionInfo)
	{
		return new PentahoMondrianDataSourceProvider(connectionInfo.getJndi());
	}
  
}
