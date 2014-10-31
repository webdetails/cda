package pt.webdetails.cda.tests.unit;

import org.junit.Ignore;
import org.junit.Test;
import junit.framework.Assert;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;
import pt.webdetails.cda.dataaccess.Parameter;

import java.util.Calendar;
import java.util.Date;


public class ParameterTest {

  @Test
  public void testStringArrayParameterCreation() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( String[].class ) );
    Assert.assertEquals( "12",  ( (String[]) value )[0] );
  }

  @Test
  public void testStringArrayParameterAssignment() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[]{"a", "b", "c"} );
    Object value = p.getValue();
    String[] valueAsArray = (String[]) value;
    Assert.assertEquals( 3, valueAsArray.length );
    Assert.assertEquals( "b", valueAsArray[1] );
  }


  @Test
  public void testStringArrayParameterGetValueAsString() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[]{"a", "b", "c"} );
    String value = p.getStringValue();
    Assert.assertEquals( "\"a\";\"b\";\"c\"", value );

  }

  @Test
  public void testStringArrayParameterSetStringValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue( "\"a\";\"b\";\"c\"" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( String[].class ) );
    String[] valueAsArray = (String[]) value;
    Assert.assertEquals( 3 , valueAsArray.length );
    Assert.assertEquals( "b", valueAsArray[ 1 ] );
  }


  //TO DO: Understand this behavior. this is not intuitive. Is this a bug ?
  @Test
  public void testStringArrayParameterGetValueAsStringWithSeparatorChars() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setValue( new String[]{"a'", "b", "c"} );
    String value = p.getStringValue();
    Assert.assertEquals( "\"a\";\"\";\"b\";\"c\"", value );

  }


  @Test
  @Ignore
  public void tempTestForCsvTokenizer() {
    CSVTokenizer tokenizer = new CSVTokenizer( "\"a\"\"b\";\"b\";\"b\";\"c\"", ";" );
    Assert.assertEquals( 4, tokenizer.countTokens() );
    Assert.assertEquals( "a\"b", tokenizer.nextToken() );
  }

  //What ?
  @Test
  public void testStringArrayParameterGetValueAsStringII() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue( "\"a\";\"\";\"b\";\"c\"" );
    String[] value = (String[]) p.getValue();
    Assert.assertEquals( 4 , value.length );
    Assert.assertEquals( "a", value[0] );
    Assert.assertEquals( "", value[1] );
  }




  //TO DO: Understand this behavior. this is not coherent with the test above
  @Test
  public void testStringArrayParameterSetStringValueWithSeparatorChars() throws Exception {
    Parameter p = new Parameter( "TestParam", "StringArray", "12;35", null, null );
    p.setStringValue(  "\"a'\";\"b\";\"c\"" );
    String stringValue = p.getStringValue();
    Assert.assertEquals( "\"a'\";\"b\";\"c\"", stringValue );
    String[] valueAsArray = (String[]) p.getValue();
    Assert.assertEquals( 3 , valueAsArray.length );
    Assert.assertEquals( "a'", valueAsArray[0] );
  }


  //IntegerArray
  @Test
  public void testIntegerArrayParameterCreation() throws Exception {
    Parameter p = new Parameter( "TestParam", "IntegerArray", "12;35", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
    Assert.assertEquals( 12,  ( (Long[]) value )[0].longValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new Long[]{(long) 45, (long) 89 } );
    Assert.assertEquals( "45;89", p.getStringValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueAsStringArrayGetStringValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new String[]{"45", "89" } );
    Assert.assertEquals( "45;89", p.getStringValue() );
  }

  @Test
  public void testIntegerArrayParameterSetValueAsStringArrayGetValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "IntegerArray", "12;35", null, null );
    p.setValue( new String[]{"45", "89" } );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
  }




  @Test
  public void testIntegerArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "IntegerArray", "12;35", null, null );
    p.setStringValue( "45;89" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Long[].class ) );
    Assert.assertEquals( 45,  ( (Long[]) value )[0].longValue() );
  }


  //this test doesn't work, test at Parameter.java:141 seems to be wrong
  @Test
  public void testNumericArrayParameterCreation() throws Exception {
    Parameter p = new Parameter( "TestParam", "NumericArray", "12.5;35.1", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Double[].class ) );
    Assert.assertEquals( 12.5,  ( (Double[]) value )[0].doubleValue() );
  }

  @Test
  public void testNumericArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "NumericArray", "12;35", null, null );
    p.setValue( new Double[]{45.5, 89.3 } );
    Assert.assertEquals( "45.5;89.3", p.getStringValue() );
  }


  @Test
  public void testNumericArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "NumericArray", "12;35", null, null );
    p.setStringValue( "45.5;89.2" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Double[].class ) );
    Assert.assertEquals( 45.5,  ( (Double[]) value )[0].doubleValue() );
  }


  @Test
  public void testDateArrayParameterCreation() throws Exception {
    //Dates are 2014-10-01 and 2014-10-31
    Parameter p = new Parameter( "TestParam", "DateArray", "1412202935142;1414798535142", null, null );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Date[].class ) );
    Calendar cld = Calendar.getInstance();
    cld.setTime( ( (Date[]) value )[0] );
    Assert.assertEquals( 2014,  cld.get( Calendar.YEAR ) );
    Assert.assertEquals( 9,  cld.get( Calendar.MONTH ) );
    Assert.assertEquals( 1,  cld.get( Calendar.DAY_OF_MONTH ) );
  }

  @Test
  public void testDateArrayParameterSetValueGetStringValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "DateArray", null, null, null );
    Calendar cld = Calendar.getInstance();
    cld.set( 2014, 9, 1 );
    Date d1 = cld.getTime();
    cld.set( 2014, 9, 31 );
    Date d2 = cld.getTime();
    p.setValue( new Date[]{d1, d2 } );
    Assert.assertEquals( "" + d1.getTime() + ";" + d2.getTime(), p.getStringValue() );
  }


  @Test
  public void testDateArrayParameterSetStringValueGetValue() throws Exception {
    Parameter p = new Parameter( "TestParam", "DateArray", null, null, null );
    p.setStringValue( "1412202935142;1414798535142" );
    Object value = p.getValue();
    Assert.assertTrue( value.getClass().isAssignableFrom( Date[].class ) );
    Calendar cld = Calendar.getInstance();
    cld.setTime( ( (Date[]) value )[1] );
    Assert.assertEquals( 2014,  cld.get( Calendar.YEAR ) );
    Assert.assertEquals( 9,  cld.get( Calendar.MONTH ) );
    Assert.assertEquals( 31,  cld.get( Calendar.DAY_OF_MONTH ) );

  }


}
