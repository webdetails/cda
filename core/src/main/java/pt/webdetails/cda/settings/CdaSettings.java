/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.EvaluableConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.UnsupportedConnectionException;
import pt.webdetails.cda.connections.kettle.TransFromFileConnection;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.connections.scripting.ScriptingConnection;
import pt.webdetails.cda.connections.xpath.XPathConnection;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.DataAccessEnums.ConnectionInstanceType;
import pt.webdetails.cda.dataaccess.DataAccessEnums.DataAccessInstanceType;
import pt.webdetails.cda.dataaccess.DenormalizedMdxDataAccess;
import pt.webdetails.cda.dataaccess.DenormalizedOlap4JDataAccess;
import pt.webdetails.cda.dataaccess.JoinCompoundDataAccess;
import pt.webdetails.cda.dataaccess.JsonScriptableDataAccess;
import pt.webdetails.cda.dataaccess.KettleDataAccess;
import pt.webdetails.cda.dataaccess.MdxDataAccess;
import pt.webdetails.cda.dataaccess.MqlDataAccess;
import pt.webdetails.cda.dataaccess.Olap4JDataAccess;
import pt.webdetails.cda.dataaccess.ReflectionDataAccess;
import pt.webdetails.cda.dataaccess.ScriptableDataAccess;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.dataaccess.UnionCompoundDataAccess;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.dataaccess.XPathDataAccess;
import pt.webdetails.cda.utils.TableModelUtils;
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cda.xml.DomTraversalHelper;
import pt.webdetails.cda.xml.XmlUtils;

import javax.naming.OperationNotSupportedException;
import javax.swing.table.TableModel;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CdaSettings class
 * <p/>
 * Created by IntelliJ IDEA. User: pedro Date: Feb 2, 2010 Time: 2:41:44 PM
 */
public class CdaSettings {

  private static final Log logger = LogFactory.getLog( CdaSettings.class );
  private String id;
  private ResourceKey contextKey;
  private Element root;
  private LinkedHashMap<String, Connection> connectionsMap;
  private LinkedHashMap<String, DataAccess> dataAccessMap;
  private Document genDoc;


  /**
   * Creates a representation of an existing CDA file.
   *
   * @param doc - XML document
   * @param id  - File id
   * @param key - Context key - representation of the id in the solution rep.
   * @throws UnsupportedConnectionException
   * @throws UnsupportedDataAccessException
   */
  public CdaSettings( final Document doc,
                      final String id,
                      final ResourceKey key ) throws UnsupportedConnectionException, UnsupportedDataAccessException {

    this.contextKey = key;
    this.id = id;
    this.root = doc.getRootElement();

    connectionsMap = new LinkedHashMap<String, Connection>();
    dataAccessMap = new LinkedHashMap<String, DataAccess>();

    parseDocument();

  }

  /**
   * Creates a representation of a CDA via API
   *
   * @param id
   * @param key
   * @throws UnsupportedConnectionException
   * @throws UnsupportedDataAccessException
   */
  public CdaSettings( final String id,
                      final ResourceKey key ) {

    this.contextKey = key;
    this.id = id;

    genDoc = DocumentFactory.getInstance().createDocument( "UTF-8" );

    genDoc.addElement( "CDADescriptor" );
    this.root = genDoc.getRootElement();

    connectionsMap = new LinkedHashMap<String, Connection>();
    dataAccessMap = new LinkedHashMap<String, DataAccess>();

  }

  public CdaSettings( final String id, final ResourceKey key, boolean garbage ) {
    this.contextKey = key;
    this.id = id;

    connectionsMap = new LinkedHashMap<String, Connection>();
    dataAccessMap = new LinkedHashMap<String, DataAccess>();
  }

  public TableModel listQueries() {
    return TableModelUtils.dataAccessMapToTableModel( dataAccessMap );
  }

  private void parseDocument() throws UnsupportedConnectionException, UnsupportedDataAccessException {

    // 1 - Parse Connections
    // 2 - Parse DataAccesses

    logger.debug( "Creating CdaSettings - parsing document" );

    parseConnections();

    parseDataAccesses();


    logger.debug( "CdaSettings created successfully" );


  }


  private DataAccess parseDataAccess( Element element ) {
    final String typeName = element.attributeValue( "type" );
    DataAccessInstanceType type = DataAccessInstanceType.parseType( typeName );
    if ( type != null ) {
      switch( type ) {
        case DENORMALIZED_MDX:
          return new DenormalizedMdxDataAccess( element );
        case JOIN:
          return new JoinCompoundDataAccess( element );
        case DENORMALIZED_OLAP4J:
          return new DenormalizedOlap4JDataAccess( element );
        case KETTLE:
          return new KettleDataAccess( element );
        case MDX:
          return new MdxDataAccess( element );
        case MQL:
          return new MqlDataAccess( element );
        case OLAP4J:
          return new Olap4JDataAccess( element );
        case REFLECTION:
          return new ReflectionDataAccess( element );
        case SCRIPTABLE:
          return new ScriptableDataAccess( element );
        case JSON_SCRIPTABLE:
          return new JsonScriptableDataAccess( element );
        case SQL:
          return new SqlDataAccess( element );
        case UNION:
          return new UnionCompoundDataAccess( element );
        case XPATH:
          return new XPathDataAccess( element );
      }
    }
    return null;
  }

  private void parseDataAccesses() throws UnsupportedDataAccessException {

    // Parsing DataAccess.

    // 1 - Parse data access, and then parse the CompoundDataAccess

    @SuppressWarnings( "unchecked" )
    final List<Element> dataAccessesList =
      root.selectNodes( "/CDADescriptor/DataAccess | /CDADescriptor/CompoundDataAccess" );

    for ( final Element element : dataAccessesList ) {

      final String type = element.attributeValue( "type" );

      // Initialize this ConnectionType

      DataAccess dataAccess = null;
      try {
        dataAccess = parseDataAccess( element );
        if ( dataAccess != null ) {
          dataAccess.setCdaSettings( this );
          addInternalDataAccess( dataAccess );
        }
      } catch ( Exception e ) {
        throw new UnsupportedDataAccessException( "Error parsing DataAccess: " + Util.getExceptionDescription( e ), e );
      }
      if ( dataAccess == null ) {
        throw new UnsupportedDataAccessException( MessageFormat.format( "Unknown DataAccess type {0}.", type ) );
      }
    }

  }

  private Connection parseConnection( String type, Element element ) throws InvalidConnectionException {

    ConnectionInstanceType connType = ConnectionInstanceType.parseType( type );

    if ( connType != null ) {
      switch( connType ) {
        case KETTLE_TRANS_FROM_FILE:
          return new TransFromFileConnection( element );
        case Metadata:
          return new MetadataConnection( element );
        case MONDRIAN_JDBC:
          return new pt.webdetails.cda.connections.mondrian.JdbcConnection( element );
        case MONDRIAN_JNDI:
          return new pt.webdetails.cda.connections.mondrian.JndiConnection( element );
        case OLAP4J:
          return new pt.webdetails.cda.connections.olap4j.DefaultOlap4jConnection( element );
        case SCRIPTING:
          return new ScriptingConnection( element );
        case SQL_JDBC:
          return new pt.webdetails.cda.connections.sql.JdbcConnection( element );
        case SQL_JNDI:
          return new pt.webdetails.cda.connections.sql.JndiConnection( element );
        case XPATH:
          return new XPathConnection( element );
      }
    }
    return null;
  }

  private void parseConnections() throws UnsupportedConnectionException {

    @SuppressWarnings( "unchecked" )
    final List<Element> connectionList = root.selectNodes( "/CDADescriptor/DataSources/Connection" );

    for ( final Element element : connectionList ) {

      final String type = element.attributeValue( "type" );

      Connection connection = null;
      try {
        connection = parseConnection( type, element );
        if ( connection != null ) {
          connection.setCdaSettings( this );
          addInternalConnection( connection );
        }
      } catch ( InvalidConnectionException e ) {
        throw new UnsupportedConnectionException(
          MessageFormat.format( "Error initializing connection: {0}", Util.getExceptionDescription( e ) ), e );
      }
      if ( connection == null ) {
        throw new UnsupportedConnectionException( MessageFormat.format( "Unrecognized connection type {0}.", type ) );
      }
    }
  }

  public String asXML() throws OperationNotSupportedException,
    IOException, TransformerFactoryConfigurationError, TransformerException {

    //if genDoc does not exist we can be sure that the CdaSetting
    //was instantiatet from an existing file. we don't want to regenerate
    //the xml in that case

    if ( genDoc == null ) {
      return this.root.asXML();
    }

    DomTraversalHelper tHelper = new DomTraversalHelper();

    return XmlUtils.prettyPrint( tHelper.traverse( this ).asXML() );

  }

  /**
   * @param dataAccess
   */
  public void addDataAccess( final DataAccess dataAccess ) {
    addInternalDataAccess( dataAccess );
    dataAccess.setCdaSettings( this );
  }

  /**
   * @param connection
   */
  public void addConnection( final Connection connection ) {
    addInternalConnection( connection );
    connection.setCdaSettings( this );
  }

  private void addInternalConnection( final Connection connectionSettings ) {
    connectionsMap.put( connectionSettings.getId(), connectionSettings );
  }

  private void addInternalDataAccess( final DataAccess dataAccess ) {
    dataAccessMap.put( dataAccess.getId(), dataAccess );
  }

  public Connection getConnection( final String id ) throws UnknownConnectionException {
    if ( !connectionsMap.containsKey( id ) ) {
      throw new UnknownConnectionException( "Unknown connection with id " + id, null );
    }

    Connection connection = connectionsMap.get( id );

    if ( connection instanceof EvaluableConnection ) { //return evaluated copy, keep original
      return ( (EvaluableConnection) connection ).evaluate();
    } else {
      return connection;
    }
  }

  public DataAccess getDataAccess( final String id ) throws UnknownDataAccessException {
    if ( !dataAccessMap.containsKey( id ) ) {
      throw new UnknownDataAccessException( "Unknown dataAccess with id " + id, null );
    }
    return dataAccessMap.get( id );
  }

  public String getId() {
    return id;
  }

  public ResourceKey getContextKey() {
    return contextKey;
  }

  public Map<String, Connection> getConnectionsMap() {

    return this.connectionsMap;
  }

  public Map<String, DataAccess> getDataAccessMap() {

    return this.dataAccessMap;
  }
}
