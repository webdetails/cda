/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.junit.Test;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.test.util.CdaTestHelper.SimpleTableModel;
import pt.webdetails.cpf.Util;

import javax.swing.table.TableModel;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

public class CsvExporterTest extends AbstractKettleExporterTestBase {

  @Test
  public void testCsvExport1() throws Exception {
    TableModel table = BasicExportExamples.getTestTable1();
    final String expected =
      "The Integer;\"The String\";The Numeric;The Date;The Calculation" + System.lineSeparator()
        + "\"1\";\"One\";\"1.05\";\"Sun Jan 01 00:01:01 GMT 2012\";\"-12.34567890123456789\"" + System.lineSeparator()
        + "\"-2\";\"Two > One\";\"-1.05\";;\"987654321.12345678900\"" + System.lineSeparator()
        + "\"9223372036854775807\";\"Many\";\"1.7976931348623157E308\";\"Thu Jan 01 00:00:00 GMT 1970\";\"4.9E-325\""
        + System.lineSeparator();
    final String result = getCsvResult( table );
    assertEquals( expected, result );
  }

  @Test
  public void testCsvCustomQuotesFromProperties() throws Exception {
    HashMap<String, String> properties = new HashMap<>();
    properties.put( "pt.webdetails.cda.exporter.csv.Separator", "|" );
    properties.put( "pt.webdetails.cda.exporter.csv.Enclosure", "'" );
    final ICdaEnvironment origEnv = CdaEngine.getEnvironment();
    try {
      initBareEngine( getMockEnvironment( properties ) );
      TableModel table = BasicExportExamples.getTestTable1();

      final String expected = getCustomExpect1();
      final String result = getCsvResult( table );
      assertEquals( expected, result );
    } finally {
      initBareEngine( origEnv );
    }
  }

  @Test
  public void testCsvCustomQuotes() throws Exception {
    HashMap<String, String> settings = new HashMap<>();
    settings.put( "csvSeparator", "|" );
    settings.put( "csvQuote", "'" );
    TableModel table = BasicExportExamples.getTestTable1();

    final String expected = getCustomExpect1();
    final String result = getCsvResult( table, settings );
    assertEquals( expected, result );
  }

  private static final String getCustomExpect1() {
    return "The Integer|'The String'|The Numeric|The Date|The Calculation" + System.lineSeparator()
      + "'1'|'One'|'1.05'|'Sun Jan 01 00:01:01 GMT 2012'|'-12.34567890123456789'" + System.lineSeparator()
      + "'-2'|'Two > One'|'-1.05'||'987654321.12345678900'" + System.lineSeparator()
      + "'9223372036854775807'|'Many'|'1.7976931348623157E308'|'Thu Jan 01 00:00:00 GMT 1970'|'4.9E-325'"
      + System.lineSeparator();
  }

  @Test
  public void testCsvNulls() throws Exception {
    TableModel table = BasicExportExamples.getNullOneLiner();
    final String result = getCsvResult( table ).split( System.lineSeparator() )[ 1 ];
    final String expected = ";;;;";
    assertEquals( expected, result );
  }

  @Test
  public void testEmpty() throws Exception {
    TableModel table = BasicExportExamples.getEmptyTable();
    final String result = getCsvResult( table );
    assertEquals( "", result );
  }

  @Test
  public void testQuoting() throws Exception {
    HashMap<String, String> settings = new HashMap<>();
    settings.put( "csvQuote", "'" );
    TableModel table = new SimpleTableModel(
      new Object[] { "don't miss this", "other" },
      new Object[] { "ok", "'';'" } );
    final String[] result = getCsvResult( table, settings ).split( System.lineSeparator() );
    assertEquals( "'don''t miss this';'other'", result[ 1 ] );
    assertEquals( "'ok';''''';'''", result[ 2 ] );
  }

  private String getCsvResult( TableModel table ) throws ExporterException {
    Map<String, String> settings = Collections.emptyMap();
    return getCsvResult( table, settings );
  }

  private String getCsvResult( TableModel table, Map<String, String> settings )
    throws ExporterException {
    final TimeZone tz = TimeZone.getDefault();
    try {
      TimeZone.setDefault( TimeZone.getTimeZone( "GMT" ) );
      TableExporter exporter = new CsvExporter( settings );
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      exporter.export( out, table );
      return Util.toString( out.toByteArray() );
    } finally {
      TimeZone.setDefault( tz );
    }
  }

}
