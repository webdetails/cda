/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;

public class DefaultDataAccessUtils implements IDataAccessUtils {

	@Override
	public void setMdxDataFactoryBaseConnectionProperties(MondrianConnection connection, AbstractNamedMDXDataFactory mdxDataFactory) {    
	}

	@Override
	public void setConnectionProvider(PmdDataFactory returnDataFactory) {
		returnDataFactory.setConnectionProvider(new PmdConnectionProvider());
	}

	@Override
	public ReportEnvironmentDataRow createEnvironmentDataRow(Configuration configuration) {
		return new ReportEnvironmentDataRow(new DefaultReportEnvironment(configuration));
	}

	@Override
	public KettleTransformationProducer createKettleTransformationProducer(TransFromFileConnectionInfo connectionInfo, String query) 
	{
		return new KettleTransFromFileProducer("",
				connectionInfo.getTransformationFile(),
				query, null, null, connectionInfo.getDefinedArgumentNames(),
				connectionInfo.getDefinedVariableNames());

	}

	@Override
	public ConnectionProvider getJndiConnectionProvider(SqlJndiConnectionInfo connectionInfo) {
		final JndiConnectionProvider provider = new JndiConnectionProvider();
		provider.setConnectionPath(connectionInfo.getJndi());
		provider.setUsername(connectionInfo.getUser());
		provider.setPassword(connectionInfo.getPass());
		return provider;
	}
	
	@Override
	public DataSourceProvider getMondrianJndiDatasourceProvider(MondrianJndiConnectionInfo connectionInfo)
	{
		return new JndiDataSourceProvider(connectionInfo.getJndi());
	}

}
