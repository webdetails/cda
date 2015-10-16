package pt.webdetails.cda.exporter;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import pt.webdetails.cda.tests.utils.TableModelChecker;
import pt.webdetails.cda.tests.utils.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cda.utils.MetadataTableModel;

public class XmlExporterTest {

  @Test
  public void testXmlExport1() throws Exception {
    TableModel table = BasicExportExamples.getTestTable1();

    Map<String, String> settings = new HashMap<>(0);
    final XmlStringTable exported = exportToXmlStringTable( table, settings );
    assertEquals( "Numeric", exported.getColumnType(0) );
    assertEquals( "String", exported.getColumnType(1) );
    assertEquals( "Numeric", exported.getColumnType(2) );
    assertEquals( "Date", exported.getColumnType(3) );
    assertEquals( "Numeric", exported.getColumnType(4) );
    TableModelChecker checker = new TableModelChecker();
    checker.assertColumnNames( exported, "The Integer", "The String", "The Numeric", "The Date", "The Calculation" );
    final TableModel expected = new SimpleTableModel(
        new Object[] { "1", "One", "1.05", "2012-01-01T00:01:01.000+0000", "-12.34567890123456789" },
        new Object[] { "-2", "Two > One", "-1.05", "", "987654321.12345678900" },
        new Object[] { "9223372036854775807", "Many", "1.7976931348623157E308", 
          "1970-01-01T01:00:00.000+0100", "4.9E-325" } );
    checker.assertEquals( expected, exported );
  }

  @Test
  public void testXmlExportFunkyStrings() throws Exception {
      MetadataTableModel table = new MetadataTableModel(
          new String[] { "string A", "string B" },
          new Class<?>[] { String.class, String.class },
          3);
      table.addRow( "<hi xml/>  </bye>", ">>>>>>>>" );
      table.addRow( "& = &amp;", "<!CDATA[ muahaha! ]>" );
      table.addRow( "wow\n\twoooow\n\n\t...", "\\o/" );

      XmlStringTable result = exportToXmlStringTable( table, Collections.<String,String>emptyMap() );
      TableModelChecker checker = new TableModelChecker();
      checker.assertEquals( table, result );
  }

  @Test
  public void testNulls() throws Exception {
    TableModel table = BasicExportExamples.getNullOneLiner();
    XmlStringTable result = exportToXmlStringTable( table, Collections.<String,String>emptyMap() );
    TableModelChecker checker = new TableModelChecker();
    assertEquals( "Numeric", result.getColumnType(0) );
    assertEquals( "String", result.getColumnType(1) );
    assertEquals( "Numeric", result.getColumnType(2) );
    assertEquals( "Date", result.getColumnType(3) );
    assertEquals( "Numeric", result.getColumnType(4) );
    checker.assertEquals( new SimpleTableModel( new Object[] {"", "", "", "", ""} ), result );
  }

  private XmlStringTable exportToXmlStringTable( TableModel table, Map<String, String> settings )
    throws ExporterException, DocumentException {
    TableExporter exporter = new XmlExporter( settings );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    exporter.export( out, table );

    InputStream in = new ByteArrayInputStream( out.toByteArray() );
    final XmlStringTable exported = new XmlStringTable( in );
    return exported;
  }

  protected static class XmlStringTable extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    Document doc;
    Element root;
    List<Element> rowSet;
    List<Element> columnsMetadata;

    @SuppressWarnings( "unchecked" )
    public XmlStringTable( InputStream input ) throws DocumentException {
      SAXReader reader = new SAXReader( false );
      doc = reader.read( input );
      root = doc.getRootElement();
      rowSet = doc.selectNodes( "/CdaExport/ResultSet/Row" );
      columnsMetadata = doc.selectNodes( "/CdaExport/MetaData/ColumnMetaData" );
    }

    @Override
    public String getValueAt( int rowIndex, int columnIndex ) {
      return ( (Element) rowSet.get( rowIndex ).selectNodes( "Col" ).get( columnIndex ) ).getText();
    }

    @Override
    public int getRowCount() {
      return rowSet.size();
    }

    @Override
    public int getColumnCount() {
      return columnsMetadata.size();
    }

    @Override
    public String getColumnName( int columnIndex ) {
      return columnsMetadata.get( columnIndex ).attributeValue( "name" );
    }

    public String getColumnType( int columnIndex ) {
      return columnsMetadata.get( columnIndex ).attributeValue( "type" );
    }
  }
}
