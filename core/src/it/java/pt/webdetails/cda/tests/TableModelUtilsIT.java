/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cda.tests;

import junit.framework.Assert;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.MdxDataAccess;
import pt.webdetails.cda.tests.utils.CdaTestCase;
import pt.webdetails.cda.utils.TableModelUtils;

import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TableModelUtilsIT extends CdaTestCase {

  private class TableModelUtilsForTest extends TableModelUtils {

    private TypedTableModel typedTableModel;

    public TableModelUtilsForTest( int mapSize ) {
      int rowCount = mapSize;

      // Define names and types
      final String[] colNames = {
        "id", "name", "type"
      };

      final Class<?>[] colTypes = {
        String.class, String.class, String.class
      };

      typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );
    }

    protected TypedTableModel getTypedTableModel() {
      return typedTableModel;
    }

    protected TableModel createSorted( HashMap<String, DataAccess> dam ) {

      TypedTableModel model = this.getTypedTableModel();

      for ( DataAccess dataAccess : dam.values() ) {
        model.addRow( new Object[] {
          dataAccess.getId(), dataAccess.getName(), dataAccess.getType() } );
      }

      return model;
    }

  }

  @Test
  public void testDataAccessMapToTableModel() {

    boolean testFlag = true;

    HashMap<String, DataAccess> dams = new LinkedHashMap<String, DataAccess>( 4 );
    dams.put( "air", new MdxDataAccess( "air", "", "", "" ) );
    dams.put( "earth", new MdxDataAccess( "earth", "", "", "" ) );
    dams.put( "fire", new MdxDataAccess( "fire", "", "", "" ) );
    dams.put( "water", new MdxDataAccess( "water", "", "", "" ) );

    HashMap<String, DataAccess> damns = new LinkedHashMap<String, DataAccess>( 4 );
    damns.put( "water", new MdxDataAccess( "water", "", "", "" ) );
    damns.put( "air", new MdxDataAccess( "air", "", "", "" ) );
    damns.put( "fire", new MdxDataAccess( "fire", "", "", "" ) );
    damns.put( "earth", new MdxDataAccess( "earth", "", "", "" ) );

    TableModelUtilsForTest tmuForTest = new TableModelUtilsForTest( 4 );
    TableModel testControl = tmuForTest.createSorted( dams );
    TableModel result = tmuForTest.dataAccessMapToTableModel( damns );

    for ( int i = 0; i < 2; i++ ) {
      if ( !result.getValueAt( i, 0 ).equals( testControl.getValueAt( i, 0 ) ) ) {
        testFlag = false;
      }
    }

    Assert.assertTrue( testFlag );
  }

}
