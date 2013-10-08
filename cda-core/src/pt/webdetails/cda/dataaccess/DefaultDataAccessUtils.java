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
