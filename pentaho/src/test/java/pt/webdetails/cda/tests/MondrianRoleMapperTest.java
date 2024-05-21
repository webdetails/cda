/*!
 * Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.tests;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import pt.webdetails.cda.connections.mondrian.MondrianRoleMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MondrianRoleMapperTest {

  private class MondrianRoleMapperForTest extends MondrianRoleMapper {
    private IConnectionUserRoleMapper userRoleMapper;
    private boolean objectDefined;


    public MondrianRoleMapperForTest( IConnectionUserRoleMapper curm ) {
      userRoleMapper = curm;
      objectDefined = true;
    }

    public MondrianRoleMapperForTest( IConnectionUserRoleMapper curm, boolean iod ) {
      userRoleMapper = curm;
      objectDefined = iod;
    }

    @Override
    protected boolean isObjectDefined() {
      return objectDefined;
    }

    @Override
    protected IConnectionUserRoleMapper getConnectionUserRoleMapper() {
      return userRoleMapper;
    }


    @Override
    protected IPentahoSession getSession() {
      return null;
    }
  }

  @Test
  public void testRoleMapperBasic() throws PentahoAccessControlException {
    IConnectionUserRoleMapper mapper = mock( IConnectionUserRoleMapper.class );


    when( mapper.mapConnectionRoles( Mockito.<IPentahoSession>any(),
      eq( "mondrian:/SteelWheels" ) ) ).thenReturn( new String[] { "role" } );

    MondrianRoleMapperForTest mrmpt = new MondrianRoleMapperForTest( mapper );

    String result = mrmpt.getRoles( "mondrian:/SteelWheels" );

    Assert.assertEquals( "role", result );

  }

  @Test
  public void testRoleMapperNoMatch() throws PentahoAccessControlException {
    IConnectionUserRoleMapper mapper = mock( IConnectionUserRoleMapper.class );


    when( mapper.mapConnectionRoles( Mockito.<IPentahoSession>any(),
      eq( "mondrian:/NotSteelWheels" ) ) ).thenReturn( new String[] { "role" } );

    MondrianRoleMapperForTest mrmpt = new MondrianRoleMapperForTest( mapper );

    String result = mrmpt.getRoles( "mondrian:/SteelWheels" );

    Assert.assertEquals( "", result );

  }

  @Test
  public void testRoleMapperMultipleRoles() throws PentahoAccessControlException {
    IConnectionUserRoleMapper mapper = mock( IConnectionUserRoleMapper.class );

    when( mapper.mapConnectionRoles( Mockito.<IPentahoSession>any(),
      eq( "mondrian:/SteelWheels" ) ) ).thenReturn( new String[] { "role", "role1" } );

    MondrianRoleMapperForTest mrmpt = new MondrianRoleMapperForTest( mapper );

    String result = mrmpt.getRoles( "mondrian:/SteelWheels" );

    Assert.assertEquals( "role,role1", result );

  }

  @Test
  public void testRoleMapperMultipleRolesWithCommas() throws PentahoAccessControlException {
    IConnectionUserRoleMapper mapper = mock( IConnectionUserRoleMapper.class );

    when( mapper.mapConnectionRoles( Mockito.<IPentahoSession>any(),
      eq( "mondrian:/SteelWheels" ) ) ).thenReturn( new String[] { "ro,le", "role1" } );

    MondrianRoleMapperForTest mrmpt = new MondrianRoleMapperForTest( mapper );

    String result = mrmpt.getRoles( "mondrian:/SteelWheels" );

    Assert.assertEquals( "ro,,le,role1", result );
  }

  @Test
  public void testRoleMapperMultipleRolesObjectNotDefined() throws PentahoAccessControlException {
    IConnectionUserRoleMapper mapper = mock( IConnectionUserRoleMapper.class );

    when( mapper.mapConnectionRoles( Mockito.<IPentahoSession>any(),
      eq( "mondrian:/SteelWheels" ) ) ).thenReturn( new String[] { "ro,le", "role1" } );

    MondrianRoleMapperForTest mrmpt = new MondrianRoleMapperForTest( mapper, false );

    String result = mrmpt.getRoles( "mondrian:/SteelWheels" );

    Assert.assertEquals( "", result );
  }

}
