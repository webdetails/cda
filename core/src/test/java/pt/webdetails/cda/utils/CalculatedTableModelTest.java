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


package pt.webdetails.cda.utils;


import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.ColumnDefinition;

import java.math.BigDecimal;

import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CalculatedTableModelTest {

  @BeforeClass
  public static void init() {
    initBareEngine( getMockEnvironment() );
  }

  @Test
  public void testGetValueAt() {
    BigDecimal validValue = new BigDecimal( 7 );

    TypedTableModel typedTableModel = new TypedTableModel( new String[] { "Q1", "Q2" },
      new Class<?>[] { Double.class, Double.class } );
    typedTableModel.addRow( 14.0d, 2.0d );
    typedTableModel.addRow( 14.0d, 0.0d );
    typedTableModel.addRow( null, 15.0d );
    typedTableModel.addRow( 14.0d, null );

    ColumnDefinition columnDefinition = new ColumnDefinition();
    columnDefinition.setFormula( "=[Q1]/[Q2]" );
    columnDefinition.setType( ColumnDefinition.TYPE.CALCULATED_COLUMN );

    ColumnDefinition[] calculatedColumns = new ColumnDefinition[ 1 ];
    calculatedColumns[ 0 ] = columnDefinition;

    CalculatedTableModel tableModel = new CalculatedTableModel( typedTableModel, calculatedColumns, true );

    System.out.println( tableModel.getColumnName( 2 ) );
    assertEquals( tableModel.getValueAt( 0, 2 ), validValue );
    assertNull( tableModel.getValueAt( 1, 2 ) );
    assertNull( tableModel.getValueAt( 2, 2 ) );
    assertNull( tableModel.getValueAt( 3, 2 ) );
  }
}
