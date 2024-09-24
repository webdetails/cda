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

package pt.webdetails.cda.exporter;

import pt.webdetails.cda.utils.MetadataTableModel;

import javax.swing.table.TableModel;
import java.math.BigDecimal;
import java.util.Date;

/**
 * to add tables every exporter should try
 */
public class BasicExportExamples {

  static TableModel getTestTable1() {
    MetadataTableModel table = new MetadataTableModel(
      new String[] { "The Integer", "The String", "The Numeric", "The Date", "The Calculation" },
      new Class<?>[] { Long.class, String.class, Double.class, Date.class, BigDecimal.class },
      3 );
    table.addRow( 1L, "One", 1.05, new Date( 1325376061000L ), new BigDecimal( "-12.34567890123456789" ) );
    table.addRow( -2L, "Two > One", -1.05, null, new BigDecimal( "000987654321.12345678900" ) );
    table.addRow( Long.MAX_VALUE, "Many", Double.MAX_VALUE, new Date( 0 ), new BigDecimal( "4.9E-325" ) );
    return table;
  }

  static final TableModel getEmptyTable() {
    return new MetadataTableModel( new String[ 0 ], new Class<?>[ 0 ], 0 );
  }

  static final TableModel getNullOneLiner() {
    MetadataTableModel table = new MetadataTableModel(
      new String[] { "long null", "string null", "double null", "date null", "big decimal null" },
      new Class<?>[] { Long.class, String.class, Double.class, Date.class, BigDecimal.class },
      1 );
    table.addRow( null, null, null, null, null );
    return table;
  }


}
