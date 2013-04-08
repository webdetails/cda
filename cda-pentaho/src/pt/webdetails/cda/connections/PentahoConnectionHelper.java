/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianDataSourceProvider;

import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.platform.plugin.connection.PentahoKettleTransFromFileProducer;


import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import org.pentaho.reporting.platform.plugin.connection.PentahoJndiDatasourceConnectionProvider;

/**
 *
 * @author pedrovale
 */
public class PentahoConnectionHelper implements IConnectionHelper {
  
  @Override
  public ConnectionProvider getSqlConnectionProvider(SqlJndiConnectionInfo connectionInfo) {
      final PentahoJndiDatasourceConnectionProvider provider = new PentahoJndiDatasourceConnectionProvider();
      provider.setJndiName(connectionInfo.getJndi());
      provider.setUsername(connectionInfo.getUser());
      provider.setPassword(connectionInfo.getPass());
      return provider;
  }  
  
  @Override
  public DataSourceProvider getMondrianInitializedDataSourceProvider(MondrianJndiConnectionInfo connectionInfo) throws InvalidConnectionException {
    return new PentahoMondrianDataSourceProvider(connectionInfo.getJndi());
  }
  
  @Override
  public KettleTransformationProducer createKettleTransformationProducer(String repositoryName, String transformationFile, String stepName, String username, String password, String[] definedArgumentNames, ParameterMapping[] definedVariableNames) {
          return new PentahoKettleTransFromFileProducer(repositoryName,
              transformationFile,
              stepName, username, password, definedArgumentNames,
              definedVariableNames);

  }
  
  
  
  
}
