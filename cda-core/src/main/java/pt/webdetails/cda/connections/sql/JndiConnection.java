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

package pt.webdetails.cda.connections.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.EvaluableConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.IDataAccessUtils;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.utils.FormulaEvaluator;
import pt.webdetails.cda.utils.Util;
        
/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:54:14
 *
 * @author Thomas Morgner.
 */
public class JndiConnection extends AbstractSqlConnection implements EvaluableConnection {

  private SqlJndiConnectionInfo connectionInfo;
  
  public JndiConnection(final Element connection)
          throws InvalidConnectionException {
    super(connection);
  }

  public JndiConnection() {
  }
  
  /**
   * TODO:new API
   * @param jndi the connection name as defined in the <code>datasources.xml</code> file
   */
  public JndiConnection(String id, String jndi){
  	super(id);
  	this.connectionInfo = new SqlJndiConnectionInfo(jndi,null,null,null,null);
  }
  
  public JndiConnection(String id, SqlJndiConnectionInfo info){
    super(id);
    this.connectionInfo = info;
  }

  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException {
	
	IDataAccessUtils dUtils = CdaEngine.getEnvironment().getDataAccessUtils();
    final ConnectionProvider connectionProvider = dUtils.getJndiConnectionProvider(connectionInfo);

    try {
      final Connection connection = connectionProvider.createConnection(null, null);
      connection.close();
    } catch (SQLException e) {

      throw new InvalidConnectionException( getClass().getName() + ": Found SQLException: " + Util.getExceptionDescription(e), e);
    }

    return connectionProvider;
  }

  protected void initializeConnection(final Element connection) throws InvalidConnectionException {
    connectionInfo = new SqlJndiConnectionInfo(connection);
  }

  public String getType() {
    return "sqlJndi";
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final JndiConnection that = (JndiConnection) o;

    if (!connectionInfo.equals(that.connectionInfo)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return connectionInfo.hashCode();
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
    ArrayList<PropertyDescriptor> properties = super.getProperties();
    properties.add(new PropertyDescriptor("jndi", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }

  public String getPasswordField()
  {
    return connectionInfo.getPasswordField();
  }

  public String getUserField()
  {
    return connectionInfo.getUserField();
  }

  @Override
  public pt.webdetails.cda.connections.Connection evaluate() {    
    SqlJndiConnectionInfo info = new SqlJndiConnectionInfo( FormulaEvaluator.replaceFormula( connectionInfo.getJndi()), 
                                                      connectionInfo.getUser(), 
                                                      connectionInfo.getPass(), 
                                                      connectionInfo.getUserField(), 
                                                      connectionInfo.getPasswordField());
    JndiConnection conn = new JndiConnection(getId(), info);
    conn.setCdaSettings(getCdaSettings());
    return conn;
  }

  public SqlJndiConnectionInfo getConnectionInfo() {
	  return connectionInfo;
  }
}
