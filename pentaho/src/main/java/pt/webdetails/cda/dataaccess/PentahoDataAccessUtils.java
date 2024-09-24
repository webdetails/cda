/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.dataaccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.api.engine.ActionExecutionException;
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

  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";

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
    Repository repo = null;
    try {
      repo = connectToRepository();
    } catch ( KettleException | ActionExecutionException e ) {
      logger.warn( "Failed to connect to repository. " + e.getMessage() );
    }
    CdaPentahoKettleTransFromFileProducer cdaPentahoKettleTransFromFileProducer
        = new CdaPentahoKettleTransFromFileProducer( "",
      relPath, query, null, null, connectionInfo.getDefinedArgumentNames(),
      connectionInfo.getDefinedVariableNames() );
    cdaPentahoKettleTransFromFileProducer.setRepository( repo );
    return cdaPentahoKettleTransFromFileProducer;
  }

  protected Repository connectToRepository() throws KettleException, ActionExecutionException {
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();

    // only load a default enterprise repository. If this option is set, then you cannot load
    // transformations or jobs from anywhere but the local server.
    String repositoriesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
      + "<repository><id>PentahoEnterpriseRepository</id>" //$NON-NLS-1$
      + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<repository_location_url>" + PentahoSystem.getApplicationContext().getFullyQualifiedServerURL()
      + "</repository_location_url>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
      + "</repository>" //$NON-NLS-1$
      + "</repositories>"; //$NON-NLS-1$

    ByteArrayInputStream sbis = null;
    try {
      sbis = new ByteArrayInputStream( repositoriesXml.getBytes( "UTF8" ) );
    } catch ( UnsupportedEncodingException e ) {
      logger.warn( "Failed to create single di server instance. " + e.getMessage() );
    }
    repositoriesMeta.readDataFromInputStream( sbis );

    // Find the specified repository.
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( SINGLE_DI_SERVER_INSTANCE );

    Repository repository =
            PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta.getId(),
                    Repository.class );
    repository.init( repositoryMeta );

    // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
    // external, you must provide a username and additionally specify that the IP address of the machine running this
    // code is trusted.
    repository.connect( PentahoSessionHolder.getSession().getName(), "" );

    return repository;
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
