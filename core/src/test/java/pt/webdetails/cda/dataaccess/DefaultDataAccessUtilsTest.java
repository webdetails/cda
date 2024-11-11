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
package pt.webdetails.cda.dataaccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.settings.CdaSettings;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultDataAccessUtilsTest {

  private DefaultDataAccessUtils defaultDataAccessUtils;
  private TransFromFileConnectionInfo mockTransFromFileConnectionInfo;
  private CdaSettings mockCdaSettings;
  private String query = "some_query";

  @Before
  public void setUp() {
    defaultDataAccessUtils = new DefaultDataAccessUtils( "user", "repo", "pass" );

    mockTransFromFileConnectionInfo = mock( TransFromFileConnectionInfo.class );
    when( mockTransFromFileConnectionInfo.getDefinedArgumentNames() ).thenReturn( new String[]{ } );
    when( mockTransFromFileConnectionInfo.getDefinedVariableNames() ).thenReturn( new ParameterMapping[]{ } );

    mockCdaSettings = mock( CdaSettings.class );
  }

  @Test
  public void testCreateKettleTransformationProducerWithLeadingFrontSlash() {
    when( mockTransFromFileConnectionInfo.getTransformationFile() ).thenReturn( "/full/path/to/file.ktr" );
    when( mockCdaSettings.getId() ).thenReturn( "/full/path/to/file.cda" );
    assertNotNull( defaultDataAccessUtils.createKettleTransformationProducer( mockTransFromFileConnectionInfo, query,
        mockCdaSettings ) );
  }

  @Test
  public void testCreateKettleTransformationProducerWithoutLeadingFrontSlash() {
    when( mockTransFromFileConnectionInfo.getTransformationFile() ).thenReturn( "full/path/to/file.ktr" );
    when( mockCdaSettings.getId() ).thenReturn( "full/path/to/file.cda" );
    assertNotNull( defaultDataAccessUtils.createKettleTransformationProducer( mockTransFromFileConnectionInfo, query,
        mockCdaSettings ) );
  }

  @Test
  public void testCreateKettleTransformationProducerWithDifferentPaths() {
    when( mockTransFromFileConnectionInfo.getTransformationFile() ).thenReturn( "full/path/to/file.ktr" );
    when( mockCdaSettings.getId() ).thenReturn( "full/path/to/other/file.cda" );
    assertNotNull( defaultDataAccessUtils.createKettleTransformationProducer( mockTransFromFileConnectionInfo, query,
        mockCdaSettings ) );
  }

  @Test
  public void testCreateKettleTransformationProducerWithNoPaths() {
    when( mockTransFromFileConnectionInfo.getTransformationFile() ).thenReturn( "file.ktr" );
    when( mockCdaSettings.getId() ).thenReturn( "file.cda" );
    assertNotNull( defaultDataAccessUtils.createKettleTransformationProducer( mockTransFromFileConnectionInfo, query,
        mockCdaSettings ) );
  }
}
