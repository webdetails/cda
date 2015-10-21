/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.scriptable.ScriptableDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.scripting.ScriptingConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 */
public class ScriptableDataAccess extends PREDataAccess {

  public ScriptableDataAccess( final Element element ) {
    super( element );
  }

  public ScriptableDataAccess() {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final ScriptingConnection connection = (ScriptingConnection) getCdaSettings().getConnection( getConnectionId() );

    final ScriptableDataFactory dataFactory = new ScriptableDataFactory();
    dataFactory.setLanguage( connection.getConnectionInfo().getLanguage() );
    dataFactory.setScript( connection.getConnectionInfo().getInitScript() );

    dataFactory.setQuery( "query", getQuery() );
    return dataFactory;
  }

  public String getType() {
    return "scriptable";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.SCRIPTING;
  }
}
