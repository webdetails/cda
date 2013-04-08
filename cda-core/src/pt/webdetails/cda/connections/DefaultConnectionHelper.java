/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;

/**
 *
 * @author pedrovale
 */
public class DefaultConnectionHelper implements IConnectionHelper {

  @Override
  public DataSourceProvider getMondrianInitializedDataSourceProvider(MondrianJndiConnectionInfo connectionInfo) throws InvalidConnectionException {
    return new JndiDataSourceProvider(connectionInfo.getJndi());
  }

  @Override
  public KettleTransformationProducer createKettleTransformationProducer(String repositoryName, String transformationFile, String stepName, String username, String password, String[] definedArgumentNames, ParameterMapping[] definedVariableNames) {
          return new KettleTransFromFileProducer(repositoryName,
              transformationFile,
              stepName, username, password, definedArgumentNames,
              definedVariableNames);

  }

  @Override
  public ConnectionProvider getSqlConnectionProvider(SqlJndiConnectionInfo connectionInfo) {
      final JndiConnectionProvider provider = new JndiConnectionProvider();
      provider.setConnectionPath(connectionInfo.getJndi());
      provider.setUsername(connectionInfo.getUser());
      provider.setPassword(connectionInfo.getPass());
      return provider;

  }
  
}
