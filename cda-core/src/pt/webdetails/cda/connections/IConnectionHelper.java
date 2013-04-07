/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.connections;

import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;

/**
 *
 * @author pedrovale
 */
public interface IConnectionHelper {
  
  public ConnectionProvider getSqlConnectionProvider(SqlJndiConnectionInfo connectionInfo);
  
  public DataSourceProvider getMondrianInitializedDataSourceProvider(MondrianJndiConnectionInfo connectionInfo) throws InvalidConnectionException;  
  
  public KettleTransformationProducer createKettleTransformationProducer(String repositoryName, String transformationFile, String stepName, String username, String password, String[] definedArgumentNames, ParameterMapping[] definedVariableNames);
  
}
