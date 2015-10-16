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

package pt.webdetails.cda.connections;

import junit.framework.TestCase;

import org.dom4j.Element;

import pt.webdetails.cda.tests.utils.CdaTestHelper;

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
    Element element = CdaTestHelper.getElementFromSnippet(
        "<Connection id=\"id\" type=\"some.jndi\">" + 
        "  <Jndi>" + JNDI + "</Jndi>" + 
        "  <User>" + USER + "</User>" + 
        "  <Pass>" + PASS + "</Pass>" +
        "  <UserField>" + USER_FIELD + "</UserField>" +
        "  <PassField>" + PASSWORD_FIELD + "</PassField>" +
        "</Connection>"
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
}
