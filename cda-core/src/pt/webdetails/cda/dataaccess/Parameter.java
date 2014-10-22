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

package pt.webdetails.cda.dataaccess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.utils.FormulaEvaluator;
import pt.webdetails.cda.utils.Util;

import pt.webdetails.cda.xml.DomVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 4:09:48 PM
 */
public class Parameter implements java.io.Serializable {

  static Log logger = LogFactory.getLog( Parameter.class );

  private static final long serialVersionUID = 3L;

  static final String DEFAULT_ARRAY_SEPERATOR = ";";

  private String name;
  private Type type;
  private Object defaultValue;
  private String pattern = StringUtils.EMPTY;
  private Object value;
  private Access access = Access.PUBLIC;
  private String separator = DEFAULT_ARRAY_SEPERATOR;
  private static final String PARAMETER_KETTLE_SEPARATOR = "pt.webdetails.cda.dataaccess.parameterarray.kettle.Separator";
  private static final String PARAMETER_KETTLE_QUOTE = "pt.webdetails.cda.dataaccess.parameterarray.kettle.Quote";

  public enum Access {
    PRIVATE( "private" ),
    PUBLIC( "public" );
    private String name;
    Access( String name ) {
      this.name = name;
    }
    public static Access parse( String text ) {
      for ( Access type : Access.values() ) {
        if ( text != null && type.name.equals( text.trim().toLowerCase() ) ) {
          return type;
        }
      }
      return PUBLIC; //default
    }
    public String toString() {
      return this.name;
    }
  }
  private static final  String FORMULA_BEGIN = "${";
  private static final  String FORMULA_END = "}";
  public enum Type {
    STRING( "String" ),
    INTEGER( "Integer" ),
    NUMERIC( "Numeric" ),
    DATE( "Date" ),
    STRING_ARRAY( "StringArray" ),
    INTEGER_ARRAY( "IntegerArray" ),
    NUMERIC_ARRAY( "NumericArray" ),
    DATE_ARRAY( "DateArray" );
    private String name;
    Type( String name ) {
      this.name = name;
    }
    public final String getName() {
      return name;
    }
    public String toString() {
      return name;
    }
    public boolean isArrayType() {
      switch ( this ) {
        case STRING_ARRAY:
        case INTEGER_ARRAY:
        case NUMERIC_ARRAY:
        case DATE_ARRAY:
          return true;
        default:
          return false;
      }
    }
    public static Type parse( String typeString ) {
      for ( Type type : Type.values() ) {
        if ( type.name.equals(typeString ) ) {
          return type;
        }
      }
      return null;
    }
    public static Type inferTypeFromObject( Object obj ) {
      if ( obj != null ) {
        if ( Object[].class.isAssignableFrom( obj.getClass() ) ) {
          if ( Double[].class.isAssignableFrom( obj.getClass() ) ) {
            return NUMERIC_ARRAY;
          } else if ( Long[].class.isAssignableFrom( obj.getClass() ) ) {
            return INTEGER_ARRAY;
          } else if ( Date[].class.isAssignableFrom( obj.getClass() ) ) {
            return DATE_ARRAY;
          } else if ( String[].class.isAssignableFrom( obj.getClass() ) ) {
            return STRING_ARRAY;
          }
        } else if ( Double.class.isAssignableFrom( obj.getClass() ) ) {
          return NUMERIC;
        } else if ( Long.class.isAssignableFrom( obj.getClass() ) ) {
          return INTEGER;
        } else if ( Date.class.isAssignableFrom( obj.getClass() ) ) {
          return DATE;
        } else if ( String.class.isAssignableFrom( obj.getClass() ) ) {
          return STRING;
        }
      }
      return null; // default
    }

  }
  /* *****
   * CTORS
   ********/

  public Parameter() {
  }

  public Parameter( final String name, final String type, final String defaultValue, final String pattern, final String access ) {
    this.name = name;
    this.type = Type.parse( type ); //defaults to null
    this.defaultValue = defaultValue;
    this.pattern = pattern;
    this.access = Access.parse( access ); //defaults to public
  }
  /**
   * Defensive copy constructor
   * @param param Parameter to clone
   */
  public Parameter( Parameter param ) {
    this( param.getName(), param.getTypeAsString(), param.getStringValue(), param.getPattern(), param.getAccess().toString() );
    this.setSeparator(param.getSeparator() );
  }

  public Parameter( final Element p ) {
    this(
        p.attributeValue( "name" ),
        p.attributeValue( "type" ),
        p.attributeValue( "default" ),
        p.attributeValue( "pattern" ),
        p.attributeValue( "access" )
    );
    this.setSeparator( p.attributeValue( "separator" ) );
  }

  public Parameter( final String name, final Object value ) {
    this.name = name;
    this.value = value;
  }
  public void inheritDefaults( Parameter defaultParameter ) {
    if ( this.type == null ) {
      this.setType( defaultParameter.getType() );
    }
    if ( this.type == Type.DATE || this.type == Type.DATE_ARRAY ) {
      this.setPattern( defaultParameter.getPattern() );
    }
    this.setSeparator( defaultParameter.getSeparator() );
  }



  public Object getValue() throws InvalidParameterException {
    Object objValue = value == null ? getDefaultValue() : value;

    if ( objValue instanceof Object[] && ( Type.INTEGER_ARRAY.equals( getType() ) )
        || Type.NUMERIC_ARRAY.equals( getType() ) ) {
      ArrayList<String> parsed = new ArrayList<String>();
      for ( Object obj : (Object[]) objValue ) {
        parsed.add( obj.toString() );
      }
      objValue = stringArrayToString( parsed.toArray( new String[parsed.size()] ), getSeparator() );
    }

    if ( objValue instanceof String ) { //may be a string or a parsed value
      final String strValue = (String) objValue;
      //check if it is a formula
      if ( strValue != null && strValue.trim().startsWith( FORMULA_BEGIN ) ) {
        String formula = Util.getContentsBetween(strValue, FORMULA_BEGIN, FORMULA_END );
        if ( formula == null ) {
          throw new InvalidParameterException( "Malformed formula expression", null );
        }
        Object value = FormulaEvaluator.processFormula( formula );
        if ( getType() == Type.STRING && !( value instanceof String ) ) {
          return getValueAsString( value );
        } else {
          return value;
        }
      }
      Type valueType = getType();
      if ( valueType == null ) {
        throw  new InvalidParameterException( "Parameter type " + getType() + " unknown, can't continue", null );
      }
      value = getValueFromString( strValue, valueType );
      return value;
    } else {
      return objValue;
    }
  }

  /**
   * @param localValue
   * @param valueType
   * @return
   * @throws InvalidParameterException
   */
  private Object getValueFromString( final String localValue, Type valueType ) throws InvalidParameterException {
    switch ( valueType ) {
      case STRING:
        return localValue;
      case INTEGER:
        return Long.parseLong( localValue );
      case NUMERIC:
        return Double.parseDouble( localValue );
      case DATE:
        if ( !StringUtils.isEmpty( getPattern() ) ) {
          SimpleDateFormat format = new SimpleDateFormat( getPattern() );
          try {
            return format.parse( localValue );
          } catch ( ParseException e ) {
            throw new InvalidParameterException( "Unable to parse " + Type.DATE.getName() + " '" + localValue + "' with pattern " + getPattern(), e );
          }
        } else {
          return new Date( Long.parseLong( localValue ) );
        }
      case STRING_ARRAY:
        return parseToArray( localValue, Type.STRING, new String[0] );
      case DATE_ARRAY:
        return parseToArray( localValue, Type.DATE, new Date[0] );
      case INTEGER_ARRAY:
        return parseToArray( localValue, Type.INTEGER, new Long[0] );
      case NUMERIC_ARRAY:
        return parseToArray( localValue, Type.NUMERIC, new Double[0] );
      default:
        return localValue;
    }
  }


  @SuppressWarnings( "unchecked" )
  private <T> T[] parseToArray( String arrayAsString, Type elementType, T[] array ) throws InvalidParameterException {
    CSVTokenizer tokenizer = new CSVTokenizer( arrayAsString, getSeparator() );
    ArrayList<T> result = new ArrayList<T>();
    while ( tokenizer.hasMoreTokens() ) {
      result.add((T) getValueFromString( tokenizer.nextToken(), elementType ) );
    }
    return result.toArray( array );
  }

  public String getName() {
    return name;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }
  public String getTypeAsString() {
    return ( type == null ) ? null : type.getName();
  }

  public void setType( final String type ) {
    this.type = Type.parse( type );
  }
  public void setType( final Type type ) {
    this.type = type;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue( final Object defaultValue ) {
    this.defaultValue = defaultValue;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern( final String pattern ) {
    this.pattern = pattern;
  }


  private String getKettleTokens( String property, String defaultValue ) {
    String strVal = CdaEngine.getInstance().getConfigProperty( property );
    if ( !StringUtils.isEmpty( strVal ) ) {
      return strVal;
    }
    return defaultValue;
  }

  private String getValueAsString( Object value ) {
    String separator = getSeparator();

    if ( value == null ) {
      if ( getDefaultValue() != null ) {
        return getDefaultValue().toString();
      } else {
        return null;
      }
    } else {
      if ( value instanceof String ) {
        return (String) value;
      } else {
        if ( type != null ) {

          String stringSeparator = CdaEngine.getInstance().getConfigProperty( PARAMETER_KETTLE_SEPARATOR );
          if ( StringUtils.isEmpty( stringSeparator ) ) {
            stringSeparator = separator;
          }


          switch ( type ) {
            case STRING_ARRAY://csvTokenizer compatible
              Boolean isKettleQuote = true;
              String stringQuote = CdaEngine.getInstance().getConfigProperty( PARAMETER_KETTLE_QUOTE );
              if ( StringUtils.isEmpty( stringQuote ) ) {
                stringQuote = "\"";
                isKettleQuote = false;
              }


              if ( value instanceof List ) {
                value = ( (List) value ).toArray();
              }
              if ( !( value instanceof String[] ) && ( value instanceof Object[] ) ) {
                Object[] oldVal = (Object[]) value;
                String[] newVal = new String[oldVal.length];
                for ( int i = 0; i < oldVal.length; i++ ) {
                  //force toString()
                  newVal[i] = "" + oldVal[i];
                }
                value = newVal;
              }
              String[] strArr = (String[]) value;
              int i = 0;
              StringBuilder strBuild = new StringBuilder();
              for ( String s : strArr ) {
                if ( i++ > 0 ) {
                  strBuild.append( stringSeparator );
                }
                strBuild.append( stringQuote );
                int lastWritten = 0;

                for ( int sepIdx = StringUtils.indexOf( s, stringQuote ); sepIdx >= 0; sepIdx = StringUtils.indexOf( s, stringQuote, sepIdx ) )
                { //quote separator
                  strBuild.append( s.substring(lastWritten, sepIdx ) );
                  strBuild.append( stringQuote );
                  if ( isKettleQuote == false ) {
                    strBuild.append( stringSeparator );
                    strBuild.append( stringQuote );
                  } else {
                    strBuild.append( stringQuote );
                  }
                  lastWritten = ++sepIdx;
                }

                strBuild.append( StringUtils.substring( s, lastWritten, s.length() ) );
                strBuild.append( stringQuote );
              }
              return strBuild.toString();
            case DATE:
              try {
                Date dt = (Date) getValue();
                return ( dt == null ) ? null : "" + dt.getTime();
              } catch ( InvalidParameterException e ) {
                logger.error( "Parameter of date type " + getName() + " does not yield date.", e );
              }
              break;
            case DATE_ARRAY:
            case INTEGER_ARRAY:
            case NUMERIC_ARRAY:
            default://also handle whan we want a string and have an array
              if ( value instanceof Object[] ) {
                Object[] arr = (Object[]) value;
                i = 0;
                strBuild = new StringBuilder();
                for ( Object o : arr ) {
                  if ( i++ > 0 ) {
                    strBuild.append( stringSeparator );
                  }
                  if ( o instanceof Date ) {
                    strBuild.append( ( (Date) o ).getTime() );
                  } else {
                    strBuild.append( o );
                  }
                }
                return strBuild.toString();
              } //else toString
          }
        }
      }
    }
    return value.toString();
  }

  public String getStringValue() {
    return getValueAsString( this.value );
  }

  public void setStringValue( final String stringValue ) {
    this.value = stringValue;
  }
  public void setStringValue( final String stringValue, Type type ) {
    this.value = stringValue; //TODO: parse now?
    this.type = type;
  }

  public void setValue( final Object value ) {
    this.value = value;
  }
  public Access getAccess() {
    return this.access;
  }
  protected void setSeparator( String separator ) {
    this.separator = separator;
  }

  public String getSeparator() {
    if ( this.separator == null ) {
      return DEFAULT_ARRAY_SEPERATOR;
    } else {
      return this.separator;
    }
  }

  private String getQueryArrayTokens( String property, String defaultValue ) {
    String strVal = CdaEngine.getInstance().getConfigProperty( property );
    if ( !StringUtils.isEmpty( strVal ) ) {
      return strVal;
    }
    return defaultValue;
  }
  /**
   * For debugging purposes
   */
  public String toString() {
    return getName() + "=" + getStringValue();
  }
  public static ParameterDataRow createParameterDataRowFromParameters( final List<Parameter> parameters ) throws InvalidParameterException
  {
    return createParameterDataRowFromParameters(parameters.toArray( new Parameter[parameters.size()] ) );
  }
  public static ParameterDataRow createParameterDataRowFromParameters( final Parameter[] parameters ) throws InvalidParameterException
  {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    if ( parameters != null ) {
      for ( final Parameter parameter : parameters ) {
        names.add(parameter.getName() );
        values.add(parameter.getValue() );
      }
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow( names.toArray(new String[] {
    } ), values.toArray() );

    return parameterDataRow;
  }
  @Override
  public boolean equals( Object other ) {
    if ( other == null ) {
      return false;
    }
    if ( this == other ) {
      return true;
    }
    if ( other instanceof Parameter ) {
      Parameter param = (Parameter) other;
      return StringUtils.equals( getName(), param.getName() )
              &&
             ( ( getType() == null && param.getType() == null )
               || getType().equals( param.getType() ) )
              &&
             StringUtils.equals( getStringValue(), param.getStringValue() );
    } else {
      return false;
    }
  }
  @Override
  public int hashCode() {
    int hashCode = getName() == null ? 0 : getName().hashCode();
    hashCode = 31 * hashCode + ( getType() == null ? 0 : getType().hashCode() );
    hashCode = 31 * hashCode + ( getStringValue() == null ? 0 : getStringValue().hashCode() );
    return hashCode;
  };

  public void readObject( ObjectInputStream in ) throws IOException {
    try {
      this.setName((String) in.readObject() );
      this.setType((Type) in.readObject() );
      //if (isDateType() ) this.setPattern((String) in.readObject() );
      this.setStringValue((String) in.readObject(), this.getType() );
      this.setSeparator((String) in.readObject() );
    } catch ( ClassNotFoundException e ) {
      throw new IOException( "Error casting read object.", e );
    }
  }

  /**
   * Should only be called on evaluated parameters
   **/
  public void writeObject( ObjectOutputStream out ) throws IOException {
    out.writeObject(this.getName() );
    out.writeObject(this.getType() );
   //if (isDateType() ) out.writeObject(this.pattern);
    out.writeObject(this.getStringValue() );
    out.writeObject(this.getSeparator() );
  }

  public void accept( DomVisitor xmlVisitor, Element daEle ) {
    xmlVisitor.visit( this, daEle );
  }
  /**
   * Helper method to convert a String[] into a string, with a user defined separator char
   * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Arrays.html#toString(java.lang.Object[])
   * @param stringArray
   * @param separator - a user defined separator char
   * @return string
   */
  private String stringArrayToString( String[] stringArray, String separator ) {
    final String START_CHAR = "[";
    final String END_CHAR = "]";
    final String SEPARATOR = ", ";
    if ( stringArray != null ) {
      String s = java.util.Arrays.toString( stringArray );
      if ( s != null && s.trim().length() > 0 ) {
      //remove java.util.Arrays.toString() START_CHAR and END_CHAR
        if ( s.startsWith( START_CHAR ) && s.endsWith( END_CHAR ) ) {
          s = s.substring( 1, s.length() - 1 );
        }
        if ( separator != null ) {
          //replace java.util.Arrays.toString() SEPARATOR with user-defined separator
          s = s.replaceAll( SEPARATOR, separator );
        }
        return s;
      }
    }
    return null;
  }

}
