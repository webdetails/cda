/*!
 * Copyright 2002 - 2016 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cda;

import org.junit.Test;
import pt.webdetails.cda.services.Previewer;
import pt.webdetails.cda.settings.CdaSettingsReadException;
import pt.webdetails.cpf.messaging.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class CdaUtilsTest {

  @Test
  public void previewQuery_ValidPath() throws Exception {
    CdaUtils utils = spyUtilsWithFakePreviewer( "file.cda", "qwerty" );
    doNothing().when( utils ).checkFileExists( "file.cda" );

    assertEquals( "qwerty", utils.previewQuery( mockRequest( "file.cda" ) ) );
  }

  @Test( expected = CdaSettingsReadException.class )
  public void previewQuery_InvalidPath() throws Exception {
    CdaUtils utils = spyUtilsWithFakePreviewer( "file.cda", "qwerty" );
    doThrow( new CdaSettingsReadException( "", null ) ).when( utils ).checkFileExists( "file.cda" );

    assertEquals( "qwerty", utils.previewQuery( mockRequest( "file.cda" ) ) );
  }

  private CdaUtils spyUtilsWithFakePreviewer( String file, String content ) throws Exception {
    Previewer previewer = mock( Previewer.class );
    when( previewer.previewQuery( file ) ).thenReturn( content );

    CdaUtils utils = spy( new CdaUtils() );
    doReturn( previewer ).when( utils ).getPreviewer();
    return utils;
  }

  private HttpServletRequest mockRequest( String file ) {
    return new MockHttpServletRequest( "/previewQuery", Collections.singletonMap( "path", new String[] { file } ) );
  }
}
