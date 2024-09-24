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

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.NamedStaticDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 */
public class ReflectionDataAccess extends PREDataAccess {

  public ReflectionDataAccess( final Element element ) {
    super( element );
  }

  public ReflectionDataAccess() {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final NamedStaticDataFactory dataFactory = new NamedStaticDataFactory();
    dataFactory.setQuery( "query", getQuery() );
    return dataFactory;
  }

  public String getType() {
    return "reflection";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }
}
