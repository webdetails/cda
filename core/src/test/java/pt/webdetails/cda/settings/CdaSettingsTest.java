/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.settings;

import static org.junit.Assert.*;

import javax.swing.table.TableModel;

import org.junit.Test;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;

import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.DataAccessEnums;

import static org.mockito.Mockito.*;

public class CdaSettingsTest {

  private static final ResourceKey RES_KEY = new ResourceKey( "schema", "rk", null );

  @Test
  public void testAddDataAccess() throws Exception {
    CdaSettings settings = new CdaSettings( "settings.cda", RES_KEY );
    assertTrue( settings.getDataAccessMap().isEmpty() );
    DataAccess dataAccess = mock( DataAccess.class );
    when( dataAccess.getId() ).thenReturn( "id" );
    settings.addDataAccess( dataAccess );
    assertEquals( dataAccess, settings.getDataAccess( "id" ) );
    verify( dataAccess ).setCdaSettings( settings );
    assertTrue( settings.getDataAccessMap().containsKey( "id" ) );
  }

  @Test
  public void testNoDataAccess() throws Exception {
    CdaSettings settings = new CdaSettings( "settings.cda", RES_KEY );
    try {
      settings.getDataAccess( "<no id>" );
      fail( "no exception" );
    } catch ( UnknownDataAccessException e ) {
      assertTrue( e.getMessage().contains( "<no id>" ) );
    }
  }

  @Test
  public void testListQueries() {
    CdaSettings settings = new CdaSettings( "test", RES_KEY );

    DataAccess incognito = mock( DataAccess.class );
    when( incognito.getId() ).thenReturn( "new id" );
    when( incognito.getName() ).thenReturn( "some name" );
    when( incognito.getType() ).thenReturn( "some type" );
    when( incognito.getAccess() ).thenReturn( DataAccessEnums.ACCESS_TYPE.PRIVATE );
    settings.addDataAccess( incognito );

    DataAccess dataAccess = mock( DataAccess.class );
    when( dataAccess.getId() ).thenReturn( "the id" );
    when( dataAccess.getName() ).thenReturn( "some name" );
    when( dataAccess.getType() ).thenReturn( "some type" );
    when( dataAccess.getAccess() ).thenReturn( DataAccessEnums.ACCESS_TYPE.PUBLIC );
    settings.addDataAccess( dataAccess );

    assertEquals( 2, settings.getDataAccessMap().size() );

    TableModel listQueries = settings.listQueries();
    assertEquals( 1, listQueries.getRowCount() );
    assertEquals( listQueries.getValueAt( 0, 0 ), "the id" );
  }


}
