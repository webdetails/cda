/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.xml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.dataservices.DataservicesConnection;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;
import pt.webdetails.cda.dataaccess.DataAccessEnums;
import pt.webdetails.cda.dataaccess.StreamingDataservicesDataAccess;
import pt.webdetails.cda.filetests.CdaTestEnvironment;
import pt.webdetails.cda.filetests.CdaTestingContentAccessFactory;
import pt.webdetails.cpf.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DomVisitorTest {

  private DomVisitor domVisitor;

  @Before
  public void setUp() throws Exception {
    this.domVisitor = new DomVisitor();

    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    Connection connection = mock( Connection.class );
    Statement statement = mock( Statement.class );
    ResultSet resultSet = mock( ResultSet.class );
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );
    when( resultSet.next() ).thenReturn( true ).thenReturn( false );
    when( resultSet.getMetaData() ).thenReturn( resultSetMetaData );
    when( statement.executeQuery( Mockito.anyString() ) ).thenReturn( resultSet );
    when( connection.createStatement( Mockito.anyInt(), Mockito.anyInt() ) ).thenReturn( statement );
    DriverConnectionProvider dataserviceLocalConnectionProvider = mock( DriverConnectionProvider.class );
    when( dataserviceLocalConnectionProvider.createConnection( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( connection );
    IDataservicesLocalConnection dataserviceLocalConnection = mock( IDataservicesLocalConnection.class );
    when( dataserviceLocalConnection.getDriverConnectionProvider() ).thenReturn( dataserviceLocalConnectionProvider );
    CdaTestEnvironment testEnvironment = spy( new CdaTestEnvironment( factory ) );
    when( testEnvironment.getDataServicesLocalConnection() ).thenReturn( dataserviceLocalConnection );
    CdaEngine.init( testEnvironment );
  }

  @Test
  public void testVisitStreamingDataServices() throws Exception {
    StreamingDataservicesDataAccess streamingDataservicesDataAccess = mock( StreamingDataservicesDataAccess.class );
    Element element = getElementFromSnippet( "<emptyElement/>" );
    when( streamingDataservicesDataAccess.getAccess() ).thenReturn( DataAccessEnums.ACCESS_TYPE.PUBLIC );
    when( streamingDataservicesDataAccess.isCacheEnabled() ).thenReturn( true );
    when( streamingDataservicesDataAccess.getName() ).thenReturn( "name" );
    when( streamingDataservicesDataAccess.getQuery() ).thenReturn( "select * from dummy" );
    when( streamingDataservicesDataAccess.getDataServiceName() ).thenReturn( "dummy" );
    when( streamingDataservicesDataAccess.getWindowMillisSize() ).thenReturn( 0l );
    when( streamingDataservicesDataAccess.getWindowRate() ).thenReturn( 0l );
    when( streamingDataservicesDataAccess.getWindowRowSize() ).thenReturn( 0 );
    domVisitor.visit( streamingDataservicesDataAccess, element );

    assertEquals( "dummy", element.element( "StreamingDataServiceName" ).getText() );
    assertEquals( "0", element.element( "WindowMillisSize" ).getText() );
    assertEquals( "0", element.element( "WindowRate" ).getText() );
    assertEquals( "0", element.element( "WindowRowSize" ).getText() );
  }

  @Test
  public void testVisit() throws Exception {
    Element element = getElementFromSnippet( "<emptyElement/>" );
    DataservicesConnection connection = mock( DataservicesConnection.class );
    when( connection.getId() ).thenReturn( "id" );
    when( connection.getTypeForFile() ).thenReturn( "type" );

    domVisitor.visit( connection, element );

    assertEquals( "id", element.element( "Connection" ).attribute( "id" ).getText() );
    assertEquals( "type", element.element( "Connection" ).attribute( "type" ).getText() );
  }

  @Test
  public void testNvl() {
    assertNotNull( domVisitor.nvl( null ) );
    assertEquals( "", domVisitor.nvl( null ) );
    assertEquals( "", domVisitor.nvl( "" ) );
    assertEquals( "not empty", domVisitor.nvl( "not empty" ) );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
