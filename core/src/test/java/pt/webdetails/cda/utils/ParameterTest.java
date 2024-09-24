/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.utils;

import org.junit.Test;
import org.junit.Assert;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;

import pt.webdetails.cda.dataaccess.Parameter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class ParameterTest {

  public class ParameterForTest extends Parameter {

    private static final long serialVersionUID = 1L;

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
    Double[] value = (Double[]) p.getValue();
    Assert.assertEquals( 12.5d, value[ 0 ].doubleValue(), 1e-5 );
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
    Double[] value = (Double[]) p.getValue();
    Assert.assertEquals( 45.5, value[ 0 ].doubleValue(), 1e-5 );
  }

  @Test
  public void testNumericArrayParameterSetValueAsStringArrayGetValue() throws Exception {
    Parameter p = new ParameterForTest( "TestParam", "NumericArray", "12;35", null, null );
    p.setValue( new String[] { "45.5", "89.2" } );
    Double[] value = (Double[]) p.getValue();
    Assert.assertEquals( 89.2, value[ 1 ].doubleValue(), 1e-5 );
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

  @Test
  public void testParamNumericArraySerialization() throws Exception {
    Parameter param = new ParameterForTest( "p1", null, null, null, null );
    param.setValue( new Double[] { 1d, 2d, 3d } );
    param.setType( Parameter.Type.inferTypeFromObject( param.getValue() ) );
    Assert.assertEquals( Parameter.Type.NUMERIC_ARRAY, param.getType() );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream( out );
    param.writeObject( objOut );
    objOut.flush();

    Parameter paramBack = new ParameterForTest( null, null, null, null, null );
    paramBack.readObject( new ObjectInputStream( new ByteArrayInputStream( out.toByteArray() ) ) );
    Assert.assertEquals( param.getName(), paramBack.getName() );
    Assert.assertTrue( Arrays.equals( (Double[]) param.getValue(), (Double[]) paramBack.getValue() ) );
    Assert.assertEquals( param.getType(), paramBack.getType() );
  }


}
