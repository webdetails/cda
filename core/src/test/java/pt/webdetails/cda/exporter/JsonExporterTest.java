package pt.webdetails.cda.exporter;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import org.junit.Test;

import pt.webdetails.cda.tests.utils.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cda.utils.MetadataTableModel;
import pt.webdetails.cpf.Util;
import static pt.webdetails.cda.tests.utils.CdaTestHelper.assertJsonEquals;

public class JsonExporterTest {

  @Test
  public void testExportEmptyTable() throws Exception {
    TableModel table = BasicExportExamples.getEmptyTable();
    String json = exportToJsonString( table, Collections.<String,String>emptyMap() );
    String expected ="{ \"queryInfo\" : {}, \"metadata\" : [ ], \"resultset\" : [ ] }";
    assertJsonEquals( "", expected, json );
  }

  @Test
  public void testExportNulls() throws Exception {
    TableModel table = BasicExportExamples.getNullOneLiner();
    String json = exportToJsonString( table, Collections.<String,String>emptyMap() );
    String expected ="{ \"queryInfo\" : {},"
        + " \"metadata\" : ["
        + " { \"colIndex\" : 0, \"colType\" : \"Numeric\", \"colName\" : \"long null\" },"
        + " { \"colIndex\" : 1, \"colType\" : \"String\", \"colName\" : \"string null\" },"
        + " { \"colIndex\" : 2, \"colType\" : \"Numeric\", \"colName\" : \"double null\" },"
        + " { \"colIndex\" : 3, \"colType\" : \"Date\", \"colName\" : \"date null\" },"
        + " { \"colIndex\" : 4, \"colType\" : \"Numeric\", \"colName\" : \"big decimal null\" } ],"
    + " \"resultset\" : [ [ null, null, null, null, null ] ] }";
    assertJsonEquals( "", expected, json );
  }

  @Test
  public void testExportFunkyStrings() throws Exception {
    MetadataTableModel table = new MetadataTableModel(
        new String[] { "string A", "string B" },
        new Class<?>[] { String.class, String.class },
        3);
    table.addRow( "{ \"json\" : \"hi\"}", "},,}" );
    table.addRow( "null", "new Date()" );
    table.addRow( "\nwow\twoooow\n\n\t...", "\\o/" );
    String jsonExport = exportToJsonString( table );
    String expected = "{"
        + "  \"queryInfo\" : {"
        + "  },"
        + "  \"resultset\" : ["
        + " [ \"{ \\\"json\\\" : \\\"hi\\\"}\", \"},,}\" ],"
        + " [ \"null\", \"new Date()\" ],"
        + " [ \"\\nwow\\twoooow\\n\\n\\t...\", \"\\\\o/\" ] ],"
        + "  \"metadata\" : [ {"
        + "    \"colIndex\" : 0,"
        + "    \"colType\" : \"String\","
        + "    \"colName\" : \"string A\""
        + "  }, {\n"
        + "    \"colIndex\" : 1,"
        + "    \"colType\" : \"String\","
        + "    \"colName\" : \"string B\""
        + "  } ] }";
    assertJsonEquals( "", expected, jsonExport );
  }

  @Test
  public void testJsonExport1() throws Exception {
    TableModel table = BasicExportExamples.getTestTable1();

    final TimeZone tz = TimeZone.getDefault() ;
    try {
      TimeZone.setDefault( TimeZone.getTimeZone( "GMT" ) );
      String exported = exportToJsonString( table );
      final String expected = "{\"metadata\":[\n" + 
          "  {\"colName\":\"The Integer\",\"colType\":\"Numeric\",\"colIndex\":0}," + 
          "  {\"colName\":\"The String\",\"colType\":\"String\",\"colIndex\":1}," + 
          "  {\"colName\":\"The Numeric\",\"colType\":\"Numeric\",\"colIndex\":2}," + 
          "  {\"colName\":\"The Date\",\"colType\":\"Date\",\"colIndex\":3}," + 
          "  {\"colName\":\"The Calculation\",\"colType\":\"Numeric\",\"colIndex\":4}]," + 
          "\"resultset\":[\n" + 
          "  [1,\"One\",1.05, \"Sun Jan 01 00:01:01 GMT 2012\", -12.34567890123456789]," + 
          "  [-2,\"Two > One\", -1.05, null, 987654321.123456789]," + 
          "  [9223372036854775807, \"Many\", 1.7976931348623157E308, \"Thu Jan 01 00:00:00 GMT 1970\",4.9E-325]]," + 
          "\"queryInfo\":{}}";
      assertJsonEquals( "json export", expected, exported );
    } finally {
      TimeZone.setDefault( tz );
    }
  }

  @Test
  public void testJsonP() throws Exception {
    TableModel table = new SimpleTableModel(
        new Object[] { "hello" } );
    String exported = exportToJsonString( table, Collections.singletonMap( "callback", "callMe" ) );
    Matcher callRegex =  Pattern.compile( "\\s*callMe[(]([^)]*)[)]\\s*;\\s*" ).matcher( exported );
    assertTrue( "makes callback", callRegex.matches() );
    String obj = callRegex.group( 1 );
    String expectArg = "{ metadata: [ { colIndex : 0, colType: 'String', colName: 'A' } ], "
        + "resultset: [[ 'hello' ]] }";
    assertJsonEquals( "call arg value", expectArg, obj );
  }

  private String exportToJsonString( TableModel table ) throws ExporterException {
    return exportToJsonString( table, Collections.<String,String>emptyMap() );
  }

  private String exportToJsonString( TableModel table, Map<String, String> settings ) throws ExporterException {
    TableExporter exporter = new JsonExporter( settings );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    exporter.export( out, table );
    return Util.toString( out.toByteArray() );
  }

}
