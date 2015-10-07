package pt.webdetails.cda.tests.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.table.TableModel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CdaTestHelper {

  public static <T> boolean columnContains( TableModel table, int colIdx, T... values ) {
    HashSet<T> set = new HashSet<T>();
    set.addAll( Arrays.asList( values ) );
    for ( int i = 0; i < table.getRowCount(); i++ ) {
      set.remove( table.getValueAt( i, colIdx ) );
      if ( set.isEmpty() ) {
        return true;
      }
    }
    return false;
  }

  public static <T> boolean columnContains( TableModel table, int colIdx, T value ) {
    for ( int i = 0; i < table.getRowCount(); i++ ) {
      if ( value.equals( table.getValueAt( i, colIdx ) ) ) {
        return true;
      }
    }
    return false;
  }

  public static boolean numericEquals( double actual, double expected, double delta ) {
    return Math.abs( actual - expected ) < delta;
  }

  public static boolean numericEquals( String actual, String expected, double delta ) {
    return numericEquals( Double.parseDouble( actual ), Double.parseDouble( expected ), delta );
  }

  public static class SimpleTableFromXml implements SimpleTable {
    Document doc;
    Element root;
    List<Element> rowSet;
    List<Element> columnsMetadata;

    @SuppressWarnings( "unchecked" )
    public SimpleTableFromXml( InputStream input ) throws DocumentException {
      SAXReader reader = new SAXReader( false );
      doc = reader.read( input );
      root = doc.getRootElement();
      rowSet = doc.selectNodes( "/CdaExport/ResultSet/Row" );
      columnsMetadata = doc.selectNodes( "/CdaExport/MetaData/ColumnMetaData" );
    }

    public String getValueAt( int rowIndex, int columnIndex ) {
      return ( (Element) rowSet.get( rowIndex ).selectNodes( "Col" ).get( columnIndex ) ).getText();
    }

    public int getRowCount() {
      return rowSet.size();
    }

    public int getColumnCount() {
      return columnsMetadata.size();
    }

    public String getColumnName( int columnIndex ) {
      return columnsMetadata.get( columnIndex ).attributeValue( "name" );
    }

    public String getColumnType( int columnIndex ) {
      return columnsMetadata.get( columnIndex ).attributeValue( "type" );
    }
  }

}
