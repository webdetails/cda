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

package pt.webdetails.cda.formula;

import junit.framework.TestCase;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

import static org.mockito.Mockito.*;

public class CdaSessionParameterProviderTest extends TestCase {
  CdaSessionParameterProvider cdaSessionParameterProvider;
  ISessionUtils sessionUtilsMock;


  public void setUp() throws Exception {
    super.setUp();

    sessionUtilsMock = mock( ISessionUtils.class );
    cdaSessionParameterProvider = new CdaSessionParameterProvider( sessionUtilsMock );
  }

  public void testGetParameter() throws Exception {
    doReturn( null ).when( sessionUtilsMock ).getCurrentSession();
    assertNull( cdaSessionParameterProvider.getParameter( "param" ) );

    IUserSession userSessionMock = mock( IUserSession.class );
    doReturn( "param" ).when( userSessionMock ).getParameter( "param" );
    doReturn( userSessionMock ).when( sessionUtilsMock ).getCurrentSession();
    assertEquals( cdaSessionParameterProvider.getParameter( "param" ), "param" );
    verify( userSessionMock, times( 1 ) ).getParameter( "param" );
  }
}
