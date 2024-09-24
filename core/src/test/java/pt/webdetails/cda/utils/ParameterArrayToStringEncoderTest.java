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

import org.junit.Assert;
import org.junit.Test;
import pt.webdetails.cda.dataaccess.Parameter;

import java.util.Calendar;
import java.util.Date;

public class ParameterArrayToStringEncoderTest {

  @Test
  public void testStringParameterArrayEncoder() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ",", "'" );
    String result = encoder.encodeParameterArray( new String[] { "aa", "bb", "cc" }, Parameter.Type.STRING_ARRAY );

    Assert.assertEquals( "'aa','bb','cc'", result );

  }

  @Test
  public void testStringParameterArrayEncoderNullQuote() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ",", null );
    String result = encoder.encodeParameterArray( new String[] { "aa", "bb", "cc" }, Parameter.Type.STRING_ARRAY );

    Assert.assertEquals( "aa,bb,cc", result );

  }


  @Test
  public void testIntegerParameterArrayEncoder() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    String result = encoder.encodeParameterArray( new String[] { "4", "7", "8" }, Parameter.Type.INTEGER_ARRAY );

    Assert.assertEquals( "4;7;8", result );

  }

  @Test
  public void testIntegerParameterArrayEncoderLongArray() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    String result = encoder.encodeParameterArray( new Long[] { 4L, 7L, 8L }, Parameter.Type.INTEGER_ARRAY );

    Assert.assertEquals( "4;7;8", result );

  }


  @Test
  public void testNumericParameterArrayEncoder() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    String result = encoder.encodeParameterArray( new Double[] { 23.4, 75.8, 89.8 }, Parameter.Type.NUMERIC_ARRAY );

    Assert.assertEquals( "23.4;75.8;89.8", result );

  }

  @Test
  public void testNumericParameterArrayEncoderStringArray() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    String result =
      encoder.encodeParameterArray( new String[] { "23.4", "75.8", "89.8" }, Parameter.Type.NUMERIC_ARRAY );

    Assert.assertEquals( "23.4;75.8;89.8", result );

  }


  @Test
  public void testDateParameterArrayEncoder() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    Calendar cld = Calendar.getInstance();
    cld.set( 2014, Calendar.OCTOBER, 1 );
    Date d1 = cld.getTime();
    cld.set( 2014, Calendar.OCTOBER, 31 );
    Date d2 = cld.getTime();

    String result = encoder.encodeParameterArray( new Date[] { d1, d2 }, Parameter.Type.DATE_ARRAY );
    Assert.assertEquals( "" + d1.getTime() + ";" + d2.getTime(), result );

  }


  @Test
  public void testDateParameterArrayEncoderStringArray() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ";", "'" );
    Calendar cld = Calendar.getInstance();
    cld.set( 2014, Calendar.OCTOBER, 1 );
    Date d1 = cld.getTime();
    cld.set( 2014, Calendar.OCTOBER, 31 );
    Date d2 = cld.getTime();

    String result =
      encoder.encodeParameterArray( new String[] { "" + d1.getTime(), "" + d2.getTime() }, Parameter.Type.DATE_ARRAY );
    Assert.assertEquals( "" + d1.getTime() + ";" + d2.getTime(), result );

  }


}
