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
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.DenormalizedMDXDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.olap4j.Olap4JConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 */
public class DenormalizedOlap4JDataAccess extends Olap4JDataAccess {

  public DenormalizedOlap4JDataAccess() {
  }

  public DenormalizedOlap4JDataAccess( final Element element ) {
    super( element );
  }

  @Override
  protected AbstractNamedMDXDataFactory createDataFactory()
    throws UnknownConnectionException, InvalidConnectionException {
    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection( getConnectionId() );
    return new DenormalizedMDXDataFactory( connection.getInitializedConnectionProvider() );
  }

  @Override
  public String getType() {
    return "denormalizedOlap4j";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.OLAP4J;
  }
}
