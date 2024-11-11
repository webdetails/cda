/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.dataaccess;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXDataFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 */
public class GlobalMdxDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog( GlobalMdxDataAccess.class );

  /**
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public GlobalMdxDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name, connectionId, query );
  }


  public GlobalMdxDataAccess( final Element element ) {
    super( element );
  }


  public GlobalMdxDataAccess() {
  }

  protected AbstractNamedMDXDataFactory createDataFactory() {
    return new BandedMDXDataFactory();
  }


  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {

    logger.debug( "Creating MDXDataFactory" );

    final MondrianConnection connection = (MondrianConnection) getCdaSettings().getConnection( getConnectionId() );
    final MondrianConnectionInfo mondrianConnectionInfo = connection.getConnectionInfo();

    final AbstractNamedMDXDataFactory mdxDataFactory = createDataFactory();
    IDataAccessUtils dataAccessUtils = CdaEngine.getEnvironment().getDataAccessUtils();
    dataAccessUtils.setMdxDataFactoryBaseConnectionProperties( connection, mdxDataFactory );


    mdxDataFactory.setDataSourceProvider( connection.getInitializedDataSourceProvider() );
    mdxDataFactory.setJdbcPassword( mondrianConnectionInfo.getPass() );
    mdxDataFactory.setJdbcUser( mondrianConnectionInfo.getUser() );
    mdxDataFactory.setRole( mondrianConnectionInfo.getMondrianRole() );
    mdxDataFactory.setRoleField( mondrianConnectionInfo.getRoleField() );
    mdxDataFactory.setJdbcPasswordField( mondrianConnectionInfo.getPasswordField() );
    mdxDataFactory.setJdbcUserField( mondrianConnectionInfo.getUserField() );

    Properties baseProperties = mdxDataFactory.getBaseConnectionProperties();
    //these properties may come enclosed in quotes, cleanQuotes removes them
    String dynamicSchemaProcessor = cleanQuotes( baseProperties.getProperty( "DynamicSchemaProcessor" ) );
    String useContentChecksum = cleanQuotes( baseProperties.getProperty( "UseContentChecksum" ) );
    if ( dynamicSchemaProcessor != null ) {
      mdxDataFactory.setDynamicSchemaProcessor( dynamicSchemaProcessor );
    }
    if ( useContentChecksum != null ) {
      mdxDataFactory.setUseContentChecksum( Boolean.parseBoolean( useContentChecksum ) );
    }

    ICubeFileProviderSetter cubeFileProviderSetter = CdaEngine.getEnvironment().getCubeFileProviderSetter();
    cubeFileProviderSetter.setCubeFileProvider( mdxDataFactory, mondrianConnectionInfo.getCatalog() );


    // using deprecated method for 3.10 support
    mdxDataFactory.setQuery( "query", getQuery() );

    return mdxDataFactory;
  }


  public String getType() {
    return "";
  }


  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.MDX;
  }


  //treat special cases: allow string[]

  @Override
  protected IDataSourceQuery performRawQuery( final ParameterDataRow parameterDataRow ) throws QueryException {
    final String MDX_MULTI_SEPARATOR = ",";

    String[] columnNames = parameterDataRow.getColumnNames();
    Object[] values = new Object[ columnNames.length ];

    for ( int i = 0; i < columnNames.length; i++ ) {
      String colName = columnNames[ i ];
      Object value = parameterDataRow.get( colName );
      if ( value != null && value.getClass().isArray() ) {
        //translate value
        value = StringUtils.join( (Object[]) value, MDX_MULTI_SEPARATOR );
      }
      values[ i ] = value;
    }

    return super.performRawQuery( new ParameterDataRow( columnNames, values ) );

  }

  private String cleanQuotes( String str ) {

    if ( str != null && str.charAt( 0 ) == '"' && str.charAt( str.length() - 1 ) == '"' ) {
      return str.substring( 1, str.length() - 1 );
    }
    return str;
  }
}
