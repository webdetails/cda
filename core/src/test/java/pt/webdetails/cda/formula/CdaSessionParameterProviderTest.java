/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
