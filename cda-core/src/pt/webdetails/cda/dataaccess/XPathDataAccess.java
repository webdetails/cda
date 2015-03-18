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

import java.lang.reflect.Method;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.extensions.datasources.xpath.XPathDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.xpath.XPathConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.CdaPropertyLookupParser;

/**
 * Todo: Document me!
 */
public class XPathDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( XPathDataAccess.class );

  public XPathDataAccess( final Element element ) {
    super( element );
  }

  public XPathDataAccess() {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final XPathConnection connection = (XPathConnection) getCdaSettings().getConnection( getConnectionId() );

    final XPathDataFactory dataFactory = new XPathDataFactory();
    dataFactory.setXqueryDataFile( connection.getXqueryDataFile() );

    // incompatible versions of setQuery in 4.x and 5.x
    legacyFallbackInvoke( dataFactory, "setQuery",
      new Class<?>[] { String.class, String.class }, new Object[] { "query", getQuery() },
      new Class<?>[] { String.class, String.class, boolean.class }, new Object[] { "query", getQuery(), true } );

    return dataFactory;
  }

  private static boolean legacyFallbackInvoke(
    Object object, String methodName,
    Class<?>[] argTypes, Object[] args,
    Class<?>[] argTypesFallback, Object[] argsFallback ) {
    Method method = null;
    try {
      try {
        method = object.getClass().getMethod( methodName, argTypes );
      } catch ( NoSuchMethodException e1 ) {
        logger.debug( String
          .format( "failed to find %s(%s): ", methodName, ArrayUtils.toString( argTypes ), e1.getLocalizedMessage() ) );
        try {
          method = object.getClass().getMethod( methodName, argTypesFallback );
          args = argsFallback;
        } catch ( NoSuchMethodException e2 ) {
          logger.error(
            String.format( "failed to find %1$s(%2$s) or %1$s(%3$s) ",
              methodName,
              ArrayUtils.toString( argTypes ),
              ArrayUtils.toString( argTypesFallback ) ) );
          throw e2;
        }
      }
      method.invoke( object, args );
      return true;
    } catch ( Exception e ) {
      logger.error( String.format( "%s call failed ", methodName ), e );
    }
    return false;
  }

  public String getType() {
    return "xPath";
    //return "XPath";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.XPATH;
  }

  // this change allows xPath parameters parsing
  @Override
  protected IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException {
    String origQuery = query;

    CdaPropertyLookupParser lookupParser = new CdaPropertyLookupParser( parameterDataRow );
    query = lookupParser.translateAndLookup( query, parameterDataRow );
    IDataSourceQuery dataSourceQuery = super.performRawQuery( parameterDataRow );
    query = origQuery;
    return ( dataSourceQuery );
  }
}
