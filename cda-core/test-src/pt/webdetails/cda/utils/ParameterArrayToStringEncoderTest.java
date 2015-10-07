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

import junit.framework.Assert;
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

    String result = encoder.encodeParameterArray( new String[] { "" + d1.getTime(), "" + d2.getTime() },
      Parameter.Type.DATE_ARRAY );
    Assert.assertEquals( "" + d1.getTime() + ";" + d2.getTime(), result );

  }


}
