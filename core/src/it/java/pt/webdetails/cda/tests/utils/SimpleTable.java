package pt.webdetails.cda.tests.utils;

/**
 * for re-parsing
 */
public interface SimpleTable {
  String getValueAt( int rowIndex, int columnIndex );

  int getRowCount();

  int getColumnCount();

  String getColumnName( int columnIndex );

  String getColumnType( int columnIndex );
}
