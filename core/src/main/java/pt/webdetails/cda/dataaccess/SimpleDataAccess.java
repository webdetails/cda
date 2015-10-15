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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.DummyConnection;
import pt.webdetails.cda.events.CdaEvent;
import pt.webdetails.cda.events.QueryErrorEvent;
import pt.webdetails.cda.events.QueryTooLongEvent;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.TableModelUtils;
import pt.webdetails.cda.xml.DomVisitable;
import pt.webdetails.cda.xml.DomVisitor;
import pt.webdetails.cpf.messaging.IEventPublisher;

/**
 * Implementation of the SimpleDataAccess
 */
public abstract class SimpleDataAccess extends AbstractDataAccess implements DomVisitable {

  private static final Log logger = LogFactory.getLog( SimpleDataAccess.class );
  protected String connectionId;
  protected String query;
  protected String queryType;
  private IEventPublisher eventPublisher;

  private static final String QUERY_TIME_THRESHOLD_PROPERTY = "pt.webdetails.cda.QueryTimeThreshold";
  private static int queryTimeThreshold = getQueryTimeThresholdFromConfig( 3600 ); //seconds

  public SimpleDataAccess() {
    this.eventPublisher = CdaEngine.getEnvironment().getEventPublisher();
  }


  public SimpleDataAccess( final Element element ) {

    super( element );
    this.connectionId = element.attributeValue( "connection" );
    this.query = element.selectSingleNode( "./Query" ).getText();
    this.eventPublisher = CdaEngine.getEnvironment().getEventPublisher();
    this.queryType = element.attributeValue( "type" );
  }


  /**
   * @param id           DataAccess ID
   * @param name
   * @param connectionId
   * @param query
   */
  public SimpleDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name );
    this.query = query;
    this.connectionId = connectionId;
    this.eventPublisher = CdaEngine.getEnvironment().getEventPublisher();
  }

  /**
   * @param id           DataAccess ID
   * @param name
   * @param connectionId
   * @param query
   * @param queryType
   */
  public SimpleDataAccess( String id, String name, String connectionId, String query, String queryType ) {
    super( id, name );
    this.query = query;
    this.connectionId = connectionId;
    this.eventPublisher = CdaEngine.getEnvironment().getEventPublisher();
    this.queryType = queryType;
  }


  protected TableModel queryDataSource( final QueryOptions queryOptions ) throws QueryException {
    final List<Parameter> parameters = getFilledParameters( queryOptions );

    final ParameterDataRow parameterDataRow;
    try {
      parameterDataRow = Parameter.createParameterDataRowFromParameters( parameters );
    } catch ( InvalidParameterException e ) {
      throw new QueryException( "Error parsing parameters ", e );
    }

    // create the cache-key which is both query and parameter values
    TableCacheKey key;
    TableModel tableModelCopy;
    IDataSourceQuery rawQueryExecution = null;
    Long queryTime = null;
    try {
      key = createCacheKey( parameters );

      if ( isCacheEnabled() && !queryOptions.isCacheBypass() ) {
        try {
          final TableModel cachedTableModel = getCdaCache().getTableModel( key );
          if ( cachedTableModel != null ) {
            logger.debug( "Found table in cache, returning." );
            return cachedTableModel;
          }
        } catch ( Exception e ) {
          logger.error( "Error while attempting to load from cache, bypassing cache (cause: " + e.getClass() + ")", e );
        }
      }

      //start timing query
      long beginTime = System.currentTimeMillis();

      rawQueryExecution = performRawQuery( parameterDataRow );

      final TableModel tableModel = postProcessTableModel( rawQueryExecution.getTableModel() );

      queryTime = logIfDurationAboveThreshold( beginTime, getId(), getQuery(), parameters );

      // Copy the tableModel and cache it
      tableModelCopy = TableModelUtils.copyTableModel( this, tableModel );
    } catch ( Exception e ) {

      try {
        CdaEvent.QueryInfo info = new CdaEvent.QueryInfo( getCdaSettings().getId(), getId(),
          getQuery(), parameterDataRow );

        if ( e instanceof QueryException && e.getCause() != null ) {
          eventPublisher.publish( new QueryErrorEvent( info, e.getCause() ) );
        } else {
          eventPublisher.publish( new QueryErrorEvent( info, e ) );
        }
      } catch ( Exception inner ) {
        logger.error( "Error pushing event", inner );
      }
      if ( e instanceof QueryException ) {
        throw (QueryException) e;
      }
      throw new QueryException( "Found an unhandled exception:", e );
    } finally {
      if ( rawQueryExecution != null ) {
        rawQueryExecution.closeDataSource();
      }
    }

    // put the copy into the cache ...
    if ( isCacheEnabled() ) {
      ExtraCacheInfo cInfo =
        new ExtraCacheInfo( this.getCdaSettings().getId(), getId(), queryTime, tableModelCopy );
      IQueryCache cache = getCdaCache();
      if ( cache != null ) {
        cache.putTableModel( key, tableModelCopy, getCacheDuration(), cInfo );
      } else {
        logger.error( "Cache enabled but no cache available." );
      }
    }

    // and finally return the copy.
    return tableModelCopy;
  }

  public List<Parameter> getFilledParameters( final QueryOptions queryOptions ) throws QueryException {

    // Get parameters from definition and apply their values
    //TODO: use queryOptions' parameters instead of copying?
    final List<Parameter> parameters = new ArrayList<Parameter>( getParameters().size() );
    for ( Parameter param : getParameters() ) {
      parameters.add( new Parameter( param ) );
    }


    for ( final Parameter parameter : parameters ) {
      final Parameter parameterPassed = queryOptions.getParameter( parameter.getName() );
      try {
        if ( parameter.getAccess().equals( Parameter.Access.PUBLIC ) && parameterPassed != null ) {

          //complete passed parameter and get its value
          parameterPassed.inheritDefaults( parameter );
          parameter.setValue( parameterPassed.getValue() );

        } else {
          //just force evaluation of default value
          parameter.setValue( parameter.getValue() );
        }
      } catch ( InvalidParameterException e ) {
        throw new QueryException( "Error parsing parameters ", e );
      }
    }
    return parameters;
  }


  private TableCacheKey createCacheKey( final List<Parameter> parameters ) throws QueryException {
    try {
      final Connection connection;
      if ( getConnectionType() == ConnectionCatalog.ConnectionType.NONE ) {
        connection = new DummyConnection();
      } else {
        connection = getCdaSettings().getConnection( getConnectionId() );
      }
      return new TableCacheKey( connection, getQuery(), getQueryType(), parameters, getExtraCacheKey() );
    } catch ( UnknownConnectionException e ) {
      // I'm sure I'll never be here
      throw new QueryException( "Unable to get a Connection for this dataAccess ", e );
    }
  }


  /**
   * @param beginTime When query execution began.
   * @return duration (in seconds)
   */
  private long logIfDurationAboveThreshold( final long beginTime, final String queryId, final String query,
                                            final List<Parameter> parameters ) {
    long endTime = System.currentTimeMillis();
    long duration = ( endTime - beginTime ) / 1000; //precision not an issue: integer op is ok
    if ( duration > queryTimeThreshold ) {
      //publish
      try {
        eventPublisher.publish( new QueryTooLongEvent(
          new QueryTooLongEvent.QueryInfo( this.getCdaSettings().getId(), queryId, query,
            Parameter.createParameterDataRowFromParameters( parameters ) ), duration ) );
      } catch ( Exception e ) {
        //TODO
        logger.error( "Error pushing event", e );
      }

      //log query and duration
      String logMsg = "Query " + queryId + " took " + duration + "s.\n";
      logMsg += "\t Query contents: << " + query.trim() + " >>\n";
      if ( parameters.size() > 0 ) {
        logMsg += "\t Parameters: \n";
        for ( Parameter parameter : parameters ) {
          logMsg += "\t\t" + parameter.toString() + "\n";
        }
      }
      logger.warn( logMsg );
    }
    return duration;
  }


  protected TableModel postProcessTableModel( TableModel tm ) {
    // we can use this method to override the general behavior. By default, no post processing is done
    return tm;
  }

  //TODO:

  /**
   * Query state.
   */
  public static interface IDataSourceQuery {

    public TableModel getTableModel();


    public void closeDataSource() throws QueryException;
  }


  protected abstract IDataSourceQuery performRawQuery( ParameterDataRow parameterDataRow ) throws QueryException;


  //  public abstract void closeDataSource() throws QueryException;
  public String getQuery() {
    return query;
  }


  public String getConnectionId() {
    return connectionId;
  }

  public String getQueryType() {
    return this.queryType;
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    List<PropertyDescriptor> properties = super.getInterface();
    properties.add( new PropertyDescriptor( "query", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "connection", PropertyDescriptor.Type.STRING,
      PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "cache", PropertyDescriptor.Type.BOOLEAN,
      PropertyDescriptor.Placement.CHILD ) );
    properties.add( new PropertyDescriptor( "cacheDuration", PropertyDescriptor.Type.NUMERIC,
      PropertyDescriptor.Placement.ATTRIB ) );
    properties.add( new PropertyDescriptor( "cacheKeys", PropertyDescriptor.Type.ARRAY,
      PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }


  private static int getQueryTimeThresholdFromConfig( int defaultValue ) {
    String strVal = CdaEngine.getInstance().getConfigProperty( QUERY_TIME_THRESHOLD_PROPERTY );
    if ( !StringUtils.isEmpty( strVal ) ) {
      try {
        return Integer.parseInt( strVal );
      } catch ( NumberFormatException nfe ) {
        logger.warn( MessageFormat.format( "Could not parse {0} in property {1}, using default {2}.", strVal,
          QUERY_TIME_THRESHOLD_PROPERTY, defaultValue ) );
      }
    }
    return defaultValue;
  }


  public void accept( DomVisitor xmlVisitor, Element root ) {
    xmlVisitor.visit( (SimpleDataAccess) this, root );
  }


  public void setQuery( String query ) {
    this.query = query;
  }

  public void setQueryType( String queryType ) {
    this.queryType = queryType;
  }
}
