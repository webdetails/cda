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

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.CacheKey;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.kettle.KettleConnection;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.ParameterArrayToStringEncoder;

import java.util.List;

public class KettleDataAccess extends PREDataAccess {

  private String path;


  private static final String PARAMETER_KETTLE_SEPARATOR =
    "pt.webdetails.cda.dataaccess.parameterarray.kettle.Separator";
  private static final String PARAMETER_KETTLE_QUOTE = "pt.webdetails.cda.dataaccess.parameterarray.kettle.Quote";


  public KettleDataAccess( final Element element ) {
    super( element );
  }

  public KettleDataAccess() {
    super();
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final KettleConnection connection = (KettleConnection) getCdaSettings().getConnection( getConnectionId() );

    final KettleDataFactory dataFactory = new KettleDataFactory();
    dataFactory.setQuery( "query", connection.createTransformationProducer( getQuery(), getCdaSettings() ) );
    return dataFactory;
  }

  public String getType() {
    return "kettle";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.KETTLE;
  }

  @Override
  public void setCdaSettings( CdaSettings cdaSettings ) {
    super.setCdaSettings( cdaSettings );
    final ResourceManager resourceManager = CdaEngine.getInstance().getSettingsManager().getResourceManager();
    ResourceKey fileKey;
    try {
      fileKey = resourceManager.deriveKey( getCdaSettings().getContextKey(), "" );
      path = fileKey.getIdentifierAsString();
    } catch ( ResourceKeyCreationException e ) {
      path = null; //shouldn't happen and will blow down the road
    }
  }

  /**
   * ContextKey is used to resolve the transformation file, and so must be stored in the cache key. We only use solution
   * paths, only the path needs to be stored.
   */
  @Override
  public CacheKey getExtraCacheKey() {

    CacheKey cacheKey = getCacheKey() != null ? ( getCacheKey() ).clone() : new CacheKey();

    cacheKey.addKeyValuePair( "path", path );

    return cacheKey;
  }

  private String getSeparator() {
    String stringSeparator = CdaEngine.getInstance().getConfigProperty( PARAMETER_KETTLE_SEPARATOR );
    if ( StringUtils.isEmpty( stringSeparator ) ) {
      stringSeparator = ";";
    }
    return stringSeparator;
  }


  private String getQuoteCharacter() {
    String stringQuote = CdaEngine.getInstance().getConfigProperty( PARAMETER_KETTLE_QUOTE );
    if ( StringUtils.isEmpty( stringQuote ) ) {
      stringQuote = "\"";
    }
    return stringQuote;
  }

  @Override
  protected IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException {
    if ( getParameters().size() == 0 ) {
      return super.performRawQuery( parameterDataRow );
    }

    Parameter parameter;
    final List<Parameter> parameters = getParameters();
    String[] columnNames = parameterDataRow.getColumnNames();

    Object[] values = new Object[ columnNames.length ];
    Object value;

    for ( int i = 0; i < parameters.size(); i++ ) {
      parameter = parameters.get( i );

      //CDA-55: We explicitly encode array parameters as strings so that they can be used in transformations
      if ( parameter.getType() == Parameter.Type.STRING_ARRAY
        || parameter.getType() == Parameter.Type.INTEGER_ARRAY
        || parameter.getType() == Parameter.Type.NUMERIC_ARRAY
        || parameter.getType() == Parameter.Type.DATE_ARRAY ) {
        ParameterArrayToStringEncoder encoder =
          new ParameterArrayToStringEncoder( getSeparator(), getQuoteCharacter() );
        value = encoder.encodeParameterArray( parameterDataRow.get( columnNames[ i ] ), parameter.getType() );
      } else {
        value = parameterDataRow.get( columnNames[ i ] );
      }
      values[ i ] = value;
    }

    return super.performRawQuery( new ParameterDataRow( columnNames, values ) );
  }
}
