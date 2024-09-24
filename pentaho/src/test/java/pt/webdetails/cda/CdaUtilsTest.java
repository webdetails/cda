/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda;

import com.google.common.net.MediaType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import pt.webdetails.cda.exporter.ExportedQueryResult;
import pt.webdetails.cda.services.Previewer;
import pt.webdetails.cda.settings.CdaSettingsReadException;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cda.utils.HttpServletResponseForTests;
import pt.webdetails.cda.utils.QueryParameters;
import pt.webdetails.cpf.messaging.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Test
  public void testDoQueryInterPlugin() throws Exception {
    QueryParameters queryParametersUtil = spy( new QueryParameters() );
    ExportedQueryResult exportedQueryResult = mock( ExportedQueryResult.class );
    doReturn( "exported query result" ).when( exportedQueryResult ).asString();
    CdaUtils utils = spyUtilsWithFakePreviewer( "file.cda", "qwerty" );
    utils.setQueryParametersUtil( queryParametersUtil );
    doReturn( exportedQueryResult ).when( utils ).doQueryInternal( any( DoQueryParameters.class ) );

    HttpServletRequest servletRequest = mock( HttpServletRequest.class );
    Enumeration enumerationParameterNames = Collections.enumeration( Arrays.asList( "param1", "param2", "param3" ) );
    doReturn( enumerationParameterNames ).when( servletRequest ).getParameterNames();
    String[] parameterValueFirst = new String[] { "val11", "val12" };
    doReturn( parameterValueFirst ).when( servletRequest ).getParameterValues( "param1" );
    String[] parameterValueSecond = new String[] { "val21", "val22" };
    doReturn( parameterValueSecond ).when( servletRequest ).getParameterValues( "param2" );
    String[] parameterValueThird = new String[] { "val31" };
    doReturn( parameterValueThird ).when( servletRequest ).getParameterValues( "param3" );

    //the actual testing call
    String result = utils.doQueryInterPlugin( servletRequest );
    assertEquals( "exported query result", result);

    ArgumentCaptor<Map> argumentCaptorParams = ArgumentCaptor.forClass( Map.class );
    verify( queryParametersUtil ).getDoQueryParameters( argumentCaptorParams.capture() );

    ArgumentCaptor<DoQueryParameters> argumentCaptorDoQueryParams = ArgumentCaptor.forClass( DoQueryParameters.class );
    verify( utils ).doQueryInternal( argumentCaptorDoQueryParams.capture() );

    assertNotNull( argumentCaptorParams.getValue() );
    assertEquals( 3, argumentCaptorParams.getValue().size() );
    assertNotNull( argumentCaptorParams.getValue().get( "param1" ) );
    assertTrue( argumentCaptorParams.getValue().get( "param1" ) instanceof List );
    assertEquals( 2, ( ( List<String> ) argumentCaptorParams.getValue().get( "param1" ) ).size() );
    assertTrue( ( ( List<String> ) argumentCaptorParams.getValue().get( "param1" ) ).contains( "val11" ) );
    assertTrue( ( ( List<String> ) argumentCaptorParams.getValue().get( "param1" ) ).contains( "val12" ) );
    assertNotNull( argumentCaptorParams.getValue().get( "param2" ) );
    assertTrue( argumentCaptorParams.getValue().get( "param2" ) instanceof List );
    assertEquals( 2, ( ( List<String> ) argumentCaptorParams.getValue().get( "param2" ) ).size() );
    assertTrue( ( ( List<String> ) argumentCaptorParams.getValue().get( "param2" ) ).contains( "val21" ) );
    assertTrue( ( ( List<String> ) argumentCaptorParams.getValue().get( "param2" ) ).contains( "val22" ) );
    assertNotNull( argumentCaptorParams.getValue().get( "param3" ) );
    assertTrue( argumentCaptorParams.getValue().get( "param3" ) instanceof List );
    assertEquals( 1, ( ( List<String> ) argumentCaptorParams.getValue().get( "param3" ) ).size() );
    assertTrue( ( ( List<String> ) argumentCaptorParams.getValue().get( "param3" ) ).contains( "val31" ) );

    verify( utils, times( 1 ) ).doQueryInternal( argumentCaptorDoQueryParams.getValue() );
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

  @Test
  public void testUnwrapQueryContentType() throws Exception {
    String path = "dummy/path";
    String uuid = "1234-56789-101112";
    String expectedContentType = MediaType.MICROSOFT_EXCEL.toString();
    HttpServletResponse response = new HttpServletResponseForTests();
    HttpServletRequest request = mock( HttpServletRequest.class );
    ExportedQueryResult result = mockExporterThatSetsContentType( expectedContentType );

    CdaUtils utilsSpy = spyUtilsWithFakeCdaCoreServiceAndCorsUtils( result, path, uuid );

    Response queryResult = utilsSpy.unwrapQuery( path, uuid, response, request );
    String observedContentType = queryResult.getMetadata().getFirst( "Content-Type" ).toString();

    assertEquals( expectedContentType, observedContentType );
  }

  private CdaUtils spyUtilsWithFakeCdaCoreServiceAndCorsUtils( ExportedQueryResult result, String path, String uuid ) throws Exception {
    CdaCoreService cdaCoreService = mock( CdaCoreService.class );
    when( cdaCoreService.unwrapQuery( path, uuid ) ).thenReturn( result );

    CdaUtils utils = spy( new CdaUtils() );
    doReturn( cdaCoreService ).when( utils ).getCdaCoreService();
    doNothing().when( utils ).setCorsHeaders( any( HttpServletRequest.class ), any( HttpServletResponse.class ) );

    return utils;
  }

  private ExportedQueryResult mockExporterThatSetsContentType( String contentType ) throws IOException {
    ExportedQueryResult result = mock( ExportedQueryResult.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();

      HttpServletResponse response = (HttpServletResponse) args[0];
      response.setContentType( contentType );
      return null;
    } ).when( result ).writeHeaders( any( HttpServletResponse.class ) );
    return result;
  }
}
