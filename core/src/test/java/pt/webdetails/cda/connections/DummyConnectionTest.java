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


package pt.webdetails.cda.connections;

import junit.framework.TestCase;
import org.dom4j.Element;
import org.junit.Before;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitor;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class DummyConnectionTest extends TestCase {
  DummyConnection dummyConnection;

  @Before
  public void setUp() {
    dummyConnection = new DummyConnection();
  }

  public void testGetGenericType() throws Exception {
    assertEquals( dummyConnection.getGenericType(), ConnectionCatalog.ConnectionType.NONE );
  }

  public void testInitializeConnection() throws InvalidConnectionException {
    Element connectionMock = mock( Element.class );
    doReturn( "dummy" ).when( connectionMock ).attributeValue( "id" );
    dummyConnection = new DummyConnection( connectionMock );
    assertEquals( dummyConnection.getId(), "dummy" );
  }

  public void testGetType() throws Exception {
    assertEquals( dummyConnection.getType(), "dummy" );
  }

  public void testHashCode() throws Exception {
    assertEquals( dummyConnection.hashCode(), -1 );
  }

  public void testEquals() throws Exception {
    assertFalse( dummyConnection.equals( dummyConnection ) );
  }

  public void testGetProperties() throws Exception {
    ArrayList<PropertyDescriptor> properties = dummyConnection.getProperties();
    assertNotNull( properties );
    assertEquals( properties.size(), 0 );
  }

  public void testGetId() throws Exception {
    assertEquals( dummyConnection.getId(), null );
    dummyConnection = new DummyConnection( "dummyConnectionID" );
    assertEquals( dummyConnection.getId(), "dummyConnectionID" );
  }

  public void testGetSetCdaSettings() throws Exception {
    assertNull( dummyConnection.getCdaSettings() );
    dummyConnection.setCdaSettings( mock( CdaSettings.class ) );
    assertNotNull( dummyConnection.getCdaSettings() );
  }

  public void testGetTypeForFile() throws Exception {
    assertEquals( dummyConnection.getTypeForFile(), "dummy" );
  }

  public void testAccept() throws Exception {
    DomVisitor domVisitorMock = mock( DomVisitor.class );
    Element elementMock = mock( Element.class );
    dummyConnection.accept( domVisitorMock, elementMock );
    verify( domVisitorMock, times( 1 ) ).visit( any( AbstractConnection.class ), eq( elementMock ) );
  }
}
