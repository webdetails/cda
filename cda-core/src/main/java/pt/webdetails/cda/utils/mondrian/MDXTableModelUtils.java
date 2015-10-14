/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils.mondrian;


import mondrian.mdx.MemberExpr;
import mondrian.olap.Dimension;
import mondrian.olap.Exp;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Util;
import mondrian.rolap.RolapMeasure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MDXTableModelUtils {

  private static final Log logger = LogFactory.getLog( MDXTableModelUtils.class );
  private static final String FORMATTED_MEASURE = "*FORMATTED_MEASURE_";

  public static String getProperMemberName( Member member, String defaultName ) {
    if ( defaultName.startsWith( FORMATTED_MEASURE ) ) {
      Member expressionMember = extractExpressionMember( member );
      return expressionMember != null ? getCaptionOrName( expressionMember ) : defaultName;
    }
    return defaultName;
  }

  /*
   * Used in the BandedMDX case, where column names are typically unique names:
   * [Measures].[MeasureName]
   * We will return them localized, if a caption is available:
   * [Measures].[LocalizedMeasureName]
   * */
  public static String computeUniqueColumnName( Position position ) {
    final StringBuffer positionName = new StringBuffer( 100 );
    for ( int j = 0; j < position.size(); j++ ) {
      if ( j != 0 ) {
        positionName.append( '/' );
      }
      final Member member = position.get( j );
      positionName.append( getUniqueMemberName( member ) );
    }
    return positionName.toString();
  }

  public static String getUniqueMemberName( Member member ) {
    String memberValue = Util.quoteMdxIdentifier( getProperMemberName( member ) );
    while ( member.getParentMember() != null ) {
      memberValue = Util.quoteMdxIdentifier( getProperMemberName( member.getParentMember() ) ) + "." + memberValue;
      member = member.getParentMember();
    }
    final Hierarchy hierarchy = member.getHierarchy();
    final Dimension dimension = hierarchy.getDimension();
    if ( hierarchy.getName().equals( dimension.getName() ) ) {
      return Util.quoteMdxIdentifier( hierarchy.getName() ) + "." + memberValue;
    } else {
      return Util.quoteMdxIdentifier( dimension.getName() ) + "." + Util.quoteMdxIdentifier( hierarchy.getName() ) + "."
        + memberValue;
    }
  }

  /*
   * Extracts the member who holds the actual localized caption
   * */
  private static Member extractExpressionMember( Member member ) {
    try {
      if ( member instanceof RolapMeasure ) {
        Exp exp = member.getExpression();
        if ( exp instanceof MemberExpr ) {
          return ( (MemberExpr) exp ).getMember();
        }
      }
    } catch ( Exception e ) {
      logger.error( "Error retrieving expression member, returning null", e );
    }
    return null;
  }

  private static String getProperMemberName( Member member ) {
    String memberName = member.getName();
    if ( memberName.startsWith( FORMATTED_MEASURE ) ) {
      Member expressionMember = extractExpressionMember( member );
      // expressionMember.getName() for the actual name VS expressionMember.getCaption(), for the localized name
      // getCaption will fallback to getName if no caption was provided
      return expressionMember != null ? getCaptionOrName( expressionMember ) : memberName;
    }
    return memberName;
  }

  private static String getCaptionOrName( Member member ) {
    String caption = member.getCaption();
    // typical case where no caption was provided for the locale
    // we want to return the actual name in this case
    if ( caption.startsWith( "%{" ) && caption.endsWith( "}" ) ) {
      return member.getName();
    }
    return caption;
  }

}
