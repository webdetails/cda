/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import junit.framework.TestCase;
import pt.webdetails.cda.utils.NaturalOrderComparator;
import junit.framework.Assert;

public class NaturalOrderTest extends TestCase {


  public NaturalOrderTest() {
    super();
  }


  public void testCompareString() {

    NaturalOrderComparator noc = new NaturalOrderComparator( null, null );

    String a = "Product 01,000.12baah";
    String b = "Product 900baah";
    int result = noc.compareStrings( a, b );
    Assert.assertTrue( "test 1", result > 0 );
    int reverseResult = noc.compareStrings( b, a );
    Assert.assertTrue( "test 1 rev", Integer.signum( result ) + Integer.signum( reverseResult ) == 0 );

    a = " 0123,000.120zap";
    b = " 0123,000.1200baah";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "test 2", result > 0 );

    a = " 0123,000.120baah";
    b = " 0123,000.1200zap";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "test 3", result < 0 );

    a = "wabuum 0123,000.120zap";
    b = "wabuum123,000.1200baah";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "test 4", result > 0 );
    reverseResult = noc.compareStrings( b, a );
    Assert.assertTrue( "test 4 rev", Integer.signum( result ) + Integer.signum( reverseResult ) == 0 );

    a = "wabuum -0123,000.120zap";
    b = "wabuum123,000.1200baah";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "test 5", result < 0 );

    a = "-.12";
    b = "-.13";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "float negatives", result > 0 );

    a = "9";
    b = "13";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "Simple integer comparison, different lengths", result < 0 );

    a = "23 apples";
    b = "4 apples";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "Simple integer comparison, different lengths, text after", result > 0 );

    a = "092.0";
    b = "13.2";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "Float compare, leading 0.", result > 0 );

    a = "item9";
    b = "item10";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "Single int after text.", result < 0 );

    a = "baah12,000bah";
    b = "baah 12,000bah";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "equals nbr and text.", result == 0 );

    a = "";
    b = "01 not empty";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "empty is smaller.", result < 0 );
    reverseResult = noc.compareStrings( b, a );
    Assert.assertTrue( "empty reverse", Integer.signum( result ) + Integer.signum( reverseResult ) == 0 );

    a = "";
    b = "";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "empty equals.", result == 0 );

    try {
      a = null;
      b = null;
      result = noc.compareStrings( a, b );
      Assert.assertTrue( "Equal nulls.", result == 0 );
    } catch ( Exception e ) {
      Assert.fail( "Null sturdiness (both)" );
    }

    try {
      a = null;
      b = "baah";
      result = noc.compareStrings( a, b );
      reverseResult = noc.compareStrings( b, a );
      Assert.assertTrue( "Null comparison coherence", Integer.signum( result ) + Integer.signum( reverseResult ) == 0 );
    } catch ( Exception e ) {
      Assert.fail( "Null sturdiness (one side)" );
    }

    a = "c001";
    b = "b002";
    result = noc.compareStrings( a, b );
    Assert.assertTrue( "Regular string comparison, ignore nbr.", result > 0 );

  }

}
