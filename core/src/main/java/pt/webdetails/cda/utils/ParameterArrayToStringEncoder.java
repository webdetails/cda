/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.dataaccess.Parameter;

import java.util.Date;
import java.util.List;

public class ParameterArrayToStringEncoder {

  private static final Log logger = LogFactory.getLog( ParameterArrayToStringEncoder.class );

  private String fieldSeparator;
  private String quoteCharacter;

  public ParameterArrayToStringEncoder( String fieldSeparator, String quoteCharacter ) {
    this.fieldSeparator = fieldSeparator;
    this.quoteCharacter = quoteCharacter == null ? "" : quoteCharacter;
  }


  public String encodeParameterArray( Object parameterArrayValue, Parameter.Type type ) {
    switch( type ) {
      case STRING_ARRAY:
        if ( parameterArrayValue instanceof List ) {
          parameterArrayValue = ( (List<?>) parameterArrayValue ).toArray();
        }

        if ( !( parameterArrayValue instanceof String[] ) && ( parameterArrayValue instanceof Object[] ) ) {
          Object[] oldVal = (Object[]) parameterArrayValue;
          String[] newVal = new String[ oldVal.length ];
          for ( int i = 0; i < oldVal.length; i++ ) {
            //force toString()
            newVal[ i ] = "" + oldVal[ i ];
          }
          parameterArrayValue = newVal;
        }

        String[] strArr = (String[]) parameterArrayValue;
        int i = 0;
        StringBuilder strBuild = new StringBuilder();
        for ( String s : strArr ) {
          if ( i++ > 0 ) {
            strBuild.append( fieldSeparator );
          }

          strBuild.append( quoteCharacter );
          //Encode occurrences of the quote character - CSVTokenizer compatible
          strBuild.append( s.replace( quoteCharacter, quoteCharacter + quoteCharacter ) );
          strBuild.append( quoteCharacter );
        }
        return strBuild.toString();
      case DATE_ARRAY:
      case INTEGER_ARRAY:
      case NUMERIC_ARRAY:
      default:
        //also handle whan we want a string and have an array
        if ( parameterArrayValue instanceof Object[] ) {
          Object[] arr = (Object[]) parameterArrayValue;
          i = 0;
          strBuild = new StringBuilder();
          for ( Object o : arr ) {
            if ( i++ > 0 ) {
              strBuild.append( fieldSeparator );
            }
            if ( o instanceof Date ) {
              strBuild.append( ( (Date) o ).getTime() );
            } else {
              strBuild.append( o );
            }
          }
          return strBuild.toString();
        } else {
          logger.warn( "Non array type passed to ArrayEncoder. Returning input value as String" );
          return parameterArrayValue.toString();
        }
    }
  }

}
