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

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.designtime.datafactory.DesignTimeDataFactoryContext;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.PathRelativizer;

import java.util.Locale;
import java.util.TimeZone;

public class DefaultDataAccessUtils implements IDataAccessUtils {


  private String repositoryName = "";
  private String username;
  private String password;

  public DefaultDataAccessUtils( String repositoryName, String username, String password ) {
    this.repositoryName = repositoryName;
    this.username = username;
    this.password = password;
  }

  public DefaultDataAccessUtils() {

  }

  @Override
  public void setMdxDataFactoryBaseConnectionProperties( MondrianConnection connection,
                                                         AbstractNamedMDXDataFactory mdxDataFactory ) {
  }

  @Override
  public void setConnectionProvider( PmdDataFactory returnDataFactory ) {
    returnDataFactory.setConnectionProvider( new PmdConnectionProvider() );
  }

  @Override
  public ReportEnvironmentDataRow createEnvironmentDataRow( Configuration configuration ) {
    return new ReportEnvironmentDataRow( new DefaultReportEnvironment( configuration ) );
  }

  @Override
  public KettleTransformationProducer createKettleTransformationProducer( TransFromFileConnectionInfo connectionInfo,
                                                                          String query, CdaSettings settings ) {
    String ktrPath = normalizePath( connectionInfo.getTransformationFile() );
    String cdaPath = normalizePath( settings.getId() );
    String relPath = PathRelativizer.relativizePath( cdaPath, ktrPath );
    return new KettleTransFromFileProducer( this.repositoryName, relPath, query,
        this.username, this.password, connectionInfo.getDefinedArgumentNames(), connectionInfo.getDefinedVariableNames() );
  }

  // Just in case the user typed the path without the leading "/"
  private String normalizePath( String path ) {
    if ( !path.contains( "/" ) ) {
      return path;
    }
    if ( path.charAt( 0 ) != '/' ) {
      path = "/" + path;
    }
    return path;
  }

  @Override
  public ConnectionProvider getJndiConnectionProvider( SqlJndiConnectionInfo connectionInfo ) {
    final JndiConnectionProvider provider = new JndiConnectionProvider();
    provider.setConnectionPath( connectionInfo.getJndi() );
    provider.setUsername( connectionInfo.getUser() );
    provider.setPassword( connectionInfo.getPass() );
    return provider;
  }

  @Override
  public DataSourceProvider getMondrianJndiDatasourceProvider( MondrianJndiConnectionInfo connectionInfo ) {
    return new JndiDataSourceProvider( connectionInfo.getJndi() );
  }

  public void initializeDataFactory( final DataFactory dataFactory, final Configuration configuration,
                                     ResourceKey contextKey )
    throws ReportDataFactoryException {
    final ResourceManager resourceManager = CdaEngine.getInstance().getSettingsManager().getResourceManager();
    resourceManager.registerDefaults();

    dataFactory.initialize( new DesignTimeDataFactoryContext( configuration, resourceManager, contextKey,
      new LibLoaderResourceBundleFactory( resourceManager, contextKey, Locale.getDefault(),
        TimeZone.getDefault() ), dataFactory ) );
  }

}
