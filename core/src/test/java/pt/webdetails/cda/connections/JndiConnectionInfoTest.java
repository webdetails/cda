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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import pt.webdetails.cpf.Util;
import junit.framework.TestCase;

public class JndiConnectionInfoTest extends TestCase {
  private static final String JNDI = "testJndi";
  private static final String USER = "testUser";
  private static final String PASS = "testPassword";
  private static final String USER_FIELD = "testUserFormula";
  private static final String PASSWORD_FIELD = "testPassFormula";

  private JndiConnectionInfo jndiConnectionInfo;

  public void setUp() throws Exception {
    super.setUp();
    jndiConnectionInfo = new JndiConnectionInfo( JNDI, USER, PASS, USER_FIELD, PASSWORD_FIELD );
  }

  public void testConstructor() throws Exception {
    JndiConnectionInfo jndiConnectionInfo = new JndiConnectionInfo( JNDI, USER, PASS, USER_FIELD, PASSWORD_FIELD );
    assertEquals( jndiConnectionInfo.getJndi(), JNDI );
    assertEquals( jndiConnectionInfo.getUser(), USER );
    assertEquals( jndiConnectionInfo.getPass(), PASS );
    assertEquals( jndiConnectionInfo.getUserField(), USER_FIELD );
    assertEquals( jndiConnectionInfo.getPasswordField(), PASSWORD_FIELD );
  }


  public void testElementConstructor() throws Exception {
    Element element = getElementFromSnippet(
      "<Connection id=\"id\" type=\"some.jndi\">"
        + "  <Jndi>" + JNDI + "</Jndi>"
        + "  <User>" + USER + "</User>"
        + "  <Pass>" + PASS + "</Pass>"
        + "  <UserField>" + USER_FIELD + "</UserField>"
        + "  <PassField>" + PASSWORD_FIELD + "</PassField>"
        + "</Connection>"
    );
    jndiConnectionInfo = new JndiConnectionInfo( element );
    assertEquals( jndiConnectionInfo.getJndi(), JNDI );
    assertEquals( jndiConnectionInfo.getUser(), USER );
    assertEquals( jndiConnectionInfo.getPass(), PASS );
    assertEquals( jndiConnectionInfo.getUserField(), USER_FIELD );
    assertEquals( jndiConnectionInfo.getPasswordField(), PASSWORD_FIELD );
  }

  public void testGetSetUser() throws Exception {
    assertEquals( jndiConnectionInfo.getUser(), USER );
    jndiConnectionInfo.setUser( USER + "_test" );
    assertEquals( jndiConnectionInfo.getUser(), USER + "_test" );
  }

  public void testGetSetPass() throws Exception {
    assertEquals( jndiConnectionInfo.getPass(), PASS );
    jndiConnectionInfo.setPass( PASS + "_test" );
    assertEquals( jndiConnectionInfo.getPass(), PASS + "_test" );
  }

  public void testGetSetUserField() throws Exception {
    assertEquals( jndiConnectionInfo.getUserField(), USER_FIELD );
    jndiConnectionInfo.setUserField( USER_FIELD + "_test" );
    assertEquals( jndiConnectionInfo.getUserField(), USER_FIELD + "_test" );
  }

  public void testGetSetPasswordField() throws Exception {
    assertEquals( jndiConnectionInfo.getPasswordField(), PASSWORD_FIELD );
    jndiConnectionInfo.setPasswordField( PASSWORD_FIELD + "_test" );
    assertEquals( jndiConnectionInfo.getPasswordField(), PASSWORD_FIELD + "_test" );
  }

  public void testGetSetJndi() throws Exception {
    jndiConnectionInfo = new JndiConnectionInfo( null, USER, PASS, USER_FIELD, PASSWORD_FIELD );
    assertEquals( jndiConnectionInfo.getJndi(), "" );
    jndiConnectionInfo.setJndi( null );
    assertEquals( jndiConnectionInfo.getJndi(), "" );
    jndiConnectionInfo.setJndi( JNDI + "_test" );
    assertEquals( jndiConnectionInfo.getJndi(), JNDI + "_test" );
  }

  public void testEquals() throws Exception {
    assertTrue( jndiConnectionInfo.equals( jndiConnectionInfo ) );
    assertFalse( jndiConnectionInfo.equals( null ) );
    assertFalse( jndiConnectionInfo.equals( new String( "test" ) ) );

    JndiConnectionInfo jndiConnectionInfoTest = new JndiConnectionInfo( JNDI, USER, PASS, USER_FIELD, PASSWORD_FIELD );
    assertTrue( jndiConnectionInfo.equals( jndiConnectionInfoTest ) );
    jndiConnectionInfoTest.setJndi( JNDI + "_test" );
    assertFalse( jndiConnectionInfo.equals( jndiConnectionInfoTest ) );
  }

  public void testHashCode() throws Exception {
    assertEquals( jndiConnectionInfo.hashCode(), JNDI.hashCode() );
  }

  private Element getElementFromSnippet( String xml ) throws Exception {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }
}
