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
package pt.webdetails.cda.utils;

import org.junit.Test;
import junit.framework.Assert;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;
import pt.webdetails.cda.dataaccess.Parameter;

import java.util.Calendar;
import java.util.Date;


public class ParameterTest {

  public class ParameterForTest extends Parameter {

    public ParameterForTest( final String name, final String type, final String defaultValue, final String pattern,
                             final String access ) {
      super( name, type, defaultValue, pattern, access );
    }


    @Override
    public String getSeparator() {
      return ";";
    }

    @Override
    public String getQuoteCharacter() {
      return "\"";
    }

  }


  @Test
  public void testStringArrayParameterCreation() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( String[].class ) );
    Assert.assertEquals( "12", ( (String[]) value )[ 0 ] );
  }

  @Test
  public void testStringArrayParameterAssignment() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[] { "a", "b", "c" } );
    Object value = p.getValue();
    String[] valueAsArray = (String[]) value;
    Assert.assertEquals( 3, valueAsArray.length );
    Assert.assertEquals( "b", valueAsArray[ 1 ] );
  }


  @Test
  public void testStringArrayParameterGetValueAsString() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[] { "a", "b", "c" } );
    String value = p.getStringValue();
    Assert.assertEquals( "\"a\";\"b\";\"c\"", value );

  }

  @Test
  public void testStringArrayParameterSetStringValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue( "\"a\";\"b\";\"c\"" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( String[].class ) );
    String[] valueAsArray = (String[]) value;
    Assert.assertEquals( 3, valueAsArray.length );
    Assert.assertEquals( "b", valueAsArray[ 1 ] );
  }


  @Test
  public void tempCsvTokenizerWithQuotedQuotes() {
    CSVTokenizer tokenizer = new CSVTokenizer( "\"a\"\"b\";\"b\";\"b\";\"c\"", ";" );
    Assert.assertEquals( 4, tokenizer.countTokens() );
    Assert.assertEquals( "a\"b", tokenizer.nextToken() );
  }


  @Test
  public void testStringArrayParameterGetValueAsStringWithQuoteChar() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[] { "a'", "b", "c" } );
    String value = p.getStringValue();
    Assert.assertEquals( "\"a'\";\"b\";\"c\"", value );

  }

  @Test
  public void testStringArrayParameterGetValueAsStringWithSeparatorChars() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[] { "a\"", "b", "c" } );
    String value = p.getStringValue();
    Assert.assertEquals( "\"a\"\"\";\"b\";\"c\"", value );

  }


  @Test
  public void testStringArrayParameterGetValueAsStringII() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue( "\"a\"\"\";\"b\";\"c\"" );
    String[] value = (String[]) p.getValue();
    Assert.assertEquals( 3, value.length );
    Assert.assertEquals( "a\"", value[ 0 ] );
    Assert.assertEquals( "b", value[ 1 ] );
  }

  @Test
  public void testStringArrayParameterSetStringValueWithSeparatorChars() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue( "\"a\"\"\";\"b\";\"c\"" );
    String stringValue = p.getStringValue();
    Assert.assertEquals( "\"a\"\"\";\"b\";\"c\"", stringValue );
    String[] valueAsArray = (String[]) p.getValue();
    Assert.assertEquals( 3, valueAsArray.length );
    Assert.assertEquals( "a\"", valueAsArray[ 0 ] );
  }


  //IntegerArray
  @Test
  public void testIntegerArrayParameterCreation() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "IntegerArray", "12;35", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
    Assert.assertEquals( 12, ( (Long[]) value )[ 0 ].longValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new Long[] { (long) 45, (long) 89 } );
    Assert.assertEquals( "45;89", p.getStringValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueAsStringArrayGetStringValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new String[] { "45", "89" } );
    Assert.assertEquals( "45;89", p.getStringValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueAsStringArrayGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new String[] { "45", "89" } );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
  }


  @Test
  public void testIntegerArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "IntegerArray", "12;35", null, null );
    p.setStringValue( "45;89" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
    Assert.assertEquals( 45, ( (Long[]) value )[ 0 ].longValue() );
  }


  @Test
  public void testNumericArrayParameterCreation() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "NumericArray", "12.5;35.1", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Double[].class ) );
    Assert.assertEquals( 12.5, ( (Double[]) value )[ 0 ] );
  }

  @Test
  public void testNumericArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "NumericArray", "12;35", null, null );
    p.setValue( new Double[] { 45.5, 89.3 } );
    Assert.assertEquals( "45.5;89.3", p.getStringValue() );
  }


  @Test
  public void testNumericArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "NumericArray", "12;35", null, null );
    p.setStringValue( "45.5;89.2" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Double[].class ) );
    Assert.assertEquals( 45.5, ( (Double[]) value )[ 0 ] );
  }

  @Test
  public void testNumericArrayParameterSetValueAsStringArrayGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "NumericArray", "12;35", null, null );
    p.setValue( new String[] { "45.5", "89.2" } );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Double[].class ) );
    Assert.assertEquals( 89.2, ( (Double[]) value )[ 1 ] );
  }


  @Test
  public void testDateArrayParameterCreation() throws Exception {
    //Dates are 2014-10-01 and 2014-10-31
    Parameter p = new ParameterForTest( "TestParam", "DateArray", "1412202935142;1414798535142", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Date[].class ) );
    Calendar cld = Calendar.getInstance();
    cld.setTime( ( (Date[]) value )[ 0 ] );
    Assert.assertEquals( 2014, cld.get( Calendar.YEAR ) );
    Assert.assertEquals( 9, cld.get( Calendar.MONTH ) );
    Assert.assertEquals( 1, cld.get( Calendar.DAY_OF_MONTH ) );
  }

  @Test
  public void testDateArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "DateArray", null, null, null );
    Calendar cld = Calendar.getInstance();
    cld.set( 2014, Calendar.OCTOBER, 1 );
    Date d1 = cld.getTime();
    cld.set( 2014, Calendar.OCTOBER, 31 );
    Date d2 = cld.getTime();
    p.setValue( new Date[] { d1, d2 } );
    Assert.assertEquals( "" + d1.getTime() + ";" + d2.getTime(), p.getStringValue() );
  }


  @Test
  public void testDateArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "DateArray", null, null, null );
    p.setStringValue( "1412202935142;1414798535142" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Date[].class ) );
    Calendar cld = Calendar.getInstance();
    cld.setTime( ( (Date[]) value )[ 1 ] );
    Assert.assertEquals( 2014, cld.get( Calendar.YEAR ) );
    Assert.assertEquals( Calendar.OCTOBER, cld.get( Calendar.MONTH ) );
    Assert.assertEquals( 31, cld.get( Calendar.DAY_OF_MONTH ) );

  }


  @Test
  public void testDateArrayParameterSetValueAsStringArrayGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "DateArray", "12;35", null, null );
    p.setValue( new String[] { "1412202935142", "1414798535142" } );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Date[].class ) );
    Calendar cld = Calendar.getInstance();
    cld.setTime( ( (Date[]) value )[ 1 ] );
    Assert.assertEquals( 2014, cld.get( Calendar.YEAR ) );
    Assert.assertEquals( 9, cld.get( Calendar.MONTH ) );
    Assert.assertEquals( 31, cld.get( Calendar.DAY_OF_MONTH ) );

  }


}
