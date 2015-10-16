package pt.webdetails.cda.exporter;

import java.math.BigDecimal;
import java.util.Date;

import javax.swing.table.TableModel;

import pt.webdetails.cda.utils.MetadataTableModel;

/**
 * to add tables every exporter should try
 */
public class BasicExportExamples {

  static TableModel getTestTable1() {
    MetadataTableModel table = new MetadataTableModel(
        new String[] { "The Integer", "The String", "The Numeric", "The Date", "The Calculation" },
        new Class<?>[] { Long.class, String.class, Double.class, Date.class, BigDecimal.class },
        3);
    table.addRow( 1L, "One", 1.05, new Date( 1325376061000L ), new BigDecimal( "-12.34567890123456789" ) );
    table.addRow( -2L, "Two > One", -1.05, null, new BigDecimal( "000987654321.12345678900" ) );
    table.addRow( Long.MAX_VALUE, "Many", Double.MAX_VALUE, new Date( 0 ), new BigDecimal( "4.9E-325") );
    return table;
  }

  final static TableModel getEmptyTable() {
    return new MetadataTableModel( new String[0], new Class<?>[0], 0 );
  }

  final static TableModel getNullOneLiner() {
    MetadataTableModel table = new MetadataTableModel(
        new String[] { "long null", "string null", "double null", "date null", "big decimal null" },
        new Class<?>[] { Long.class, String.class, Double.class, Date.class, BigDecimal.class },
        1);
    table.addRow( null, null, null, null, null );
    return table;
  }


}
