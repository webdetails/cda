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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

public class NaturalOrderComparator implements Comparator<Integer> {

  private TableModel baseData;
  private List<String> sortBy;
  Comparator<String> stringComparator = String.CASE_INSENSITIVE_ORDER;
  Pattern recognizeNbr = Pattern.compile( "[\\s]*[-+]?(?:(?:\\d[\\d,]*)(?:[.][\\d]+)?|([.][\\d]+))" );

  public NaturalOrderComparator( TableModel baseData, List<String> sortBy ) {
    this.baseData = baseData;
    this.sortBy = sortBy;
  }


  public int compare( Integer i0, Integer i1 ) {

    for ( String entry : sortBy ) {

      char direction = entry.charAt( entry.length() - 1 );
      boolean ascending = direction == 'A' || direction == 'a';
      int i = Integer.parseInt( entry.substring( 0, entry.length() - 1 ), 10 );
      int bigger = 0;

      String v0 = baseData.getValueAt( i0, i ) != null ? baseData.getValueAt( i0, i ).toString() : "";
      String v1 = baseData.getValueAt( i1, i ) != null ? baseData.getValueAt( i1, i ).toString() : "";

      boolean v0Empty = false;
      boolean v1Empty = false;

      if ( v0.compareTo( "Infinity" ) == 0 || v0.compareTo( "" ) == 0 || v0.compareTo( "null" ) == 0 ) {
        v0Empty = true;
      }
      if ( v1.compareTo( "Infinity" ) == 0 || v1.compareTo( "" ) == 0 || v1.compareTo( "null" ) == 0 ) {
        v1Empty = true;
      }

      if ( v0Empty && v1Empty ) {
        return 0;
      } else if ( v0Empty ) {
        return 1;
      } else if ( v1Empty ) {
        return -1;
      }


      bigger = compareStrings( v0, v1 );
      if ( bigger != 0 ) {
        return ascending ? bigger : -bigger;
      }
    }
    return 0;
  }

  public int compareStrings( String a, String b ) {

    if ( a == null ) {
      if ( b == null ) {
        return 0;
      } else {
        return -1;
      }
    } else if ( b == null ) {
      return 1;
    }

    Matcher matcherA = recognizeNbr.matcher( a );
    Matcher matcherB = recognizeNbr.matcher( b );

    int idxA = 0, idxB = 0;
    while ( idxA < a.length() || idxB < b.length() ) {
      boolean foundInA = matcherA.find( idxA );
      boolean foundInB = matcherB.find( idxB );

      if ( !foundInA || !foundInB ) {
        // then our job is over, return a regular string comparison
        return stringComparator.compare( a.substring( idxA ), b.substring( idxB ) );
      }
      // else found in both

      //1) compare unmatched bit as String
      String preA = StringUtils.substring( a, idxA, matcherA.start() );
      String preB = StringUtils.substring( b, idxB, matcherB.start() );
      int comparison = stringComparator.compare( preA, preB );

      if ( comparison != 0 ) {
        return comparison; // done!
      }

      // 2) get the number and compare it
      String matchA = StringUtils.substring( a, matcherA.start(), matcherA.end() );
      String matchB = StringUtils.substring( b, matcherB.start(), matcherB.end() );

      BigDecimal numberA = new BigDecimal( StringUtils.remove( matchA, ',' ).trim() );
      BigDecimal numberB = new BigDecimal( StringUtils.remove( matchB, ',' ).trim() );

      comparison = numberA.compareTo( numberB );

      if ( comparison != 0 ) {
        return comparison;
      }

      // 3) if inconclusive process the rest
      idxA = matcherA.end();
      idxB = matcherB.end();
    }

    return 0;
  }

}
