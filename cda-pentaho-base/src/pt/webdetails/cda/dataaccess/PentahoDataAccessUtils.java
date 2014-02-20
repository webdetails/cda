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

package pt.webdetails.cda.dataaccess;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.PathRelativizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.platform.plugin.connection.PentahoJndiDatasourceConnectionProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianDataSourceProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoPmdConnectionProvider;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.platform.plugin.PentahoReportEnvironment;


public class PentahoDataAccessUtils implements IDataAccessUtils {

  private static final Log logger = LogFactory.getLog( PentahoDataAccessUtils.class );

  @Override
  public ReportEnvironmentDataRow createEnvironmentDataRow( Configuration configuration ) {
    return new ReportEnvironmentDataRow( new PentahoReportEnvironment( configuration ) );
  }

  @Override
  public void setConnectionProvider( PmdDataFactory returnDataFactory ) {
    returnDataFactory.setConnectionProvider( new PentahoPmdConnectionProvider() );
  }

  @Override
  public void setMdxDataFactoryBaseConnectionProperties( MondrianConnection connection,
                                                        AbstractNamedMDXDataFactory mdxDataFactory ) {
    IMondrianCatalogService catalogService =
            PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", null );
    final List<MondrianCatalog> catalogs =
            catalogService.listCatalogs( PentahoSessionHolder.getSession(), false );

    MondrianCatalog catalog = null;
    for ( MondrianCatalog cat : catalogs ) {
      final String definition = cat.getDefinition();
      final String definitionFileName = IOUtils.getInstance().getFileName( definition );
      if ( definitionFileName.equals(
              IOUtils.getInstance().getFileName( connection.getConnectionInfo().getCatalog() ) ) ) {
        catalog = cat;
        break;
      }
    }

    if ( catalog != null ) {

      Properties props = new Properties();
      try {
        //TODO: can we drop the old version now?
        //mdxDataFactory.setBaseConnectionProperties( props );
        props.load( new StringReader( catalog.getDataSourceInfo().replace( ';', '\n' ) ) );
        try {
          // Apply the method through reflection
          Method m = AbstractNamedMDXDataFactory.class.getMethod( "setBaseConnectionProperties",
                  Properties.class );
          m.invoke( mdxDataFactory, props );
        } catch ( Exception ex ) {
          // This is a previous version - continue
        }

      } catch ( IOException ex ) {
        logger.warn( "Failed to transform DataSourceInfo string '" + catalog.getDataSourceInfo()
                + "' into properties" );
      }

    }
  }

  @Override
  public KettleTransformationProducer createKettleTransformationProducer( TransFromFileConnectionInfo connectionInfo,
                                                                         String query, CdaSettings cdaSettings ) {

    String ktrPath = connectionInfo.getTransformationFile();
    String relPath;
    if ( ktrPath.charAt( 0 ) == '/' ) {
      relPath = PathRelativizer.relativizePath( cdaSettings.getId(), ktrPath );
    } else {
      relPath = ktrPath;
    }

    return new CdaPentahoKettleTransFromFileProducer( "",
            relPath, query, null, null, connectionInfo.getDefinedArgumentNames(),
            connectionInfo.getDefinedVariableNames() );
  }

  @Override
  public ConnectionProvider getJndiConnectionProvider( SqlJndiConnectionInfo connectionInfo ) {
    final PentahoJndiDatasourceConnectionProvider provider = new PentahoJndiDatasourceConnectionProvider();
    provider.setJndiName( connectionInfo.getJndi() );
    provider.setUsername( connectionInfo.getUser() );
    provider.setPassword( connectionInfo.getPass() );
    return provider;
  }

  @Override
  public DataSourceProvider getMondrianJndiDatasourceProvider( MondrianJndiConnectionInfo connectionInfo ) {
    return new PentahoMondrianDataSourceProvider( connectionInfo.getJndi() );
  }

}
