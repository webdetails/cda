/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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
import pt.webdetails.cda.utils.ParameterArrayToStringEncoder;
import pt.webdetails.cda.utils.Util;

import pt.webdetails.cda.xml.DomVisitor;


public class Parameter implements java.io.Serializable {


  private static final String PARAMETER_ARRAY_SEPARATOR =
    "pt.webdetails.cda.dataaccess.parameterarray.Separator";
  private static final String PARAMETER_ARRAY_QUOTE = "pt.webdetails.cda.dataaccess.parameterarray.Quote";

  private String separator;
  private String quoteCharacter;
  private static final long serialVersionUID = 3L;
  private static final String FORMULA_BEGIN = "${";
  private static final String FORMULA_END = "}";
  static Log logger = LogFactory.getLog( Parameter.class );
  private String name;
  private Type type;
  private Object defaultValue;
  private String pattern = StringUtils.EMPTY;
  private Object value;
  private Access access = Access.PUBLIC;

  public Parameter() {
  }

  public Parameter( final String name, final String type, final String defaultValue, final String pattern,
                    final String access ) {
    this.name = name;
    this.type = Type.parse( type ); //defaults to null
    this.defaultValue = defaultValue;
    this.pattern = pattern;
    this.access = Access.parse( access ); //defaults to public
  }


  /* *****
   * CTORS
   ********/

  /**
   * Defensive copy constructor
   *
   * @param param Parameter to clone
   */
  public Parameter( Parameter param ) {
    this( param.getName(), param.getTypeAsString(), param.getStringValue(), param.getPattern(),
      param.getAccess().toString() );
    this.setSeparator( param.getSeparator() );
    this.setQuoteCharacter( param.getQuoteCharacter() );
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
    this.setQuoteCharacter( p.attributeValue( "quoteCharacter" ) );
  }

  public Parameter( final String name, final Object value ) {
    this.name = name;
    this.value = value;
  }

  public static ParameterDataRow createParameterDataRowFromParameters( final List<Parameter> parameters )
    throws InvalidParameterException {
    return createParameterDataRowFromParameters( parameters.toArray( new Parameter[ parameters.size() ] ) );
  }

  public static ParameterDataRow createParameterDataRowFromParameters( final Parameter[] parameters )
    throws InvalidParameterException {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    if ( parameters != null ) {
      for ( final Parameter parameter : parameters ) {
        names.add( parameter.getName() );
        values.add( parameter.getValue() );
      }
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow( names.toArray( new String[] { } ),
      values.toArray() );

    return parameterDataRow;
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


    //This is used to make sure that if we set an array as a string array, getValue returns
    // a properly typed array and not the original string array
    if ( objValue.getClass().isAssignableFrom( String[].class )
      && ( ( Type.INTEGER_ARRAY.equals( getType() ) )
      || Type.NUMERIC_ARRAY.equals( getType() )
      || Type.DATE_ARRAY.equals( getType() ) ) ) {

      ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( getSeparator(), getQuoteCharacter() );
      objValue = encoder.encodeParameterArray( objValue, getType() );

      /*
      ArrayList<String> parsed = new ArrayList<String>();
      for ( Object obj : (Object[]) objValue ) {
        parsed.add( obj.toString() );
      }
      objValue = stringArrayToString( parsed.toArray( new String[ parsed.size() ] ), getSeparator() );
      */
    }

    if ( objValue instanceof String ) { //may be a string or a parsed value
      final String strValue = (String) objValue;
      //check if it is a formula
      if ( strValue != null && strValue.trim().startsWith( FORMULA_BEGIN ) ) {
        String formula = Util.getContentsBetween( strValue, FORMULA_BEGIN, FORMULA_END );
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
        throw new InvalidParameterException( "Parameter type " + getType() + " unknown, can't continue", null );
      }
      value = getValueFromString( strValue, valueType );
      return value;
    } else {
      return objValue;
    }
  }

  public void setValue( final Object value ) {
    this.value = value;
  }

  /**
   * @param localValue
   * @param valueType
   * @return
   * @throws InvalidParameterException
   */
  private Object getValueFromString( final String localValue, Type valueType ) throws InvalidParameterException {

    switch( valueType ) {
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
            throw new InvalidParameterException(
              "Unable to parse " + Type.DATE.getName() + " '" + localValue + "' with pattern " + getPattern(), e );
          }
        } else {
          return new Date( Long.parseLong( localValue ) );
        }
      case STRING_ARRAY:
        return parseToArray( localValue, Type.STRING, new String[ 0 ] );
      case DATE_ARRAY:
        return parseToArray( localValue, Type.DATE, new Date[ 0 ] );
      case INTEGER_ARRAY:
        return parseToArray( localValue, Type.INTEGER, new Long[ 0 ] );
      case NUMERIC_ARRAY:
        return parseToArray( localValue, Type.NUMERIC, new Double[ 0 ] );
      default:
        return localValue;
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> T[] parseToArray( String arrayAsString, Type elementType, T[] array ) throws InvalidParameterException {
    CSVTokenizer tokenizer = new CSVTokenizer( arrayAsString, getSeparator() );

    ArrayList<T> result = new ArrayList<T>();
    while ( tokenizer.hasMoreTokens() ) {
      result.add( (T) getValueFromString( tokenizer.nextToken(), elementType ) );
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

  public void setType( final Type type ) {
    this.type = type;
  }

  public void setType( final String type ) {
    this.type = Type.parse( type );
  }

  public String getTypeAsString() {
    return ( type == null ) ? null : type.getName();
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

  private String getValueAsString( Object value ) {
    String separator = getSeparator();

    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( separator, getQuoteCharacter() );
    if ( value == null ) {
      if ( getDefaultValue() != null ) {
        return getDefaultValue().toString();
      } else {
        return null;
      }
    } else if ( value instanceof String ) {
      return (String) value;
    } else if ( type != null ) {
      switch( type ) {
        case STRING_ARRAY://csvTokenizer compatible
          return encoder.encodeParameterArray( value, type );
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
          return encoder.encodeParameterArray( value, type );
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

  public Access getAccess() {
    return this.access;
  }


  public String getSeparator() {
    if ( this.separator == null ) {
      this.separator = CdaEngine.getInstance().getConfigProperty( PARAMETER_ARRAY_SEPARATOR );
      if ( StringUtils.isEmpty( this.separator ) ) {
        this.separator = ";";
      }
    }
    return this.separator;
  }

  protected void setSeparator( String separator ) {
    this.separator = separator;
  }


  public String getQuoteCharacter() {
    if ( this.quoteCharacter == null ) {
      this.quoteCharacter = CdaEngine.getInstance().getConfigProperty( PARAMETER_ARRAY_QUOTE );
      if ( StringUtils.isEmpty( this.quoteCharacter ) ) {
        this.quoteCharacter = "\"";
      }
    }
    return this.quoteCharacter;
  }


  protected void setQuoteCharacter( String quoteCharacter ) {
    this.quoteCharacter = quoteCharacter;
  }


  /**
   * For debugging purposes
   */
  public String toString() {
    return getName() + "=" + getStringValue();
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
        && ( ( getType() == null && param.getType() == null )
        || getType().equals( param.getType() ) )
        && StringUtils.equals( getStringValue(), param.getStringValue() );
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
  }

  public void readObject( ObjectInputStream in ) throws IOException {
    try {
      this.setName( (String) in.readObject() );
      this.setType( (Type) in.readObject() );
      //if(isDateType()) this.setPattern((String) in.readObject());
      this.setStringValue( (String) in.readObject(), this.getType() );
      this.setSeparator( (String) in.readObject() );
      this.setQuoteCharacter( (String) in.readObject() );
    } catch ( ClassNotFoundException e ) {
      throw new IOException( "Error casting read object.", e );
    }
  }

  /**
   * Should only be called on evaluated parameters
   */
  public void writeObject( ObjectOutputStream out ) throws IOException {
    out.writeObject( this.getName() );
    out.writeObject( this.getType() );
    //if(isDateType()) out.writeObject(this.pattern);
    out.writeObject( this.getStringValue() );
    out.writeObject( this.getSeparator() );
    out.writeObject( this.getQuoteCharacter() );
  }


  public void accept( DomVisitor xmlVisitor, Element daEle ) {
    xmlVisitor.visit( this, daEle );
  }

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

    public static Type parse( String typeString ) {
      for ( Type type : Type.values() ) {
        if ( type.name.equals( typeString ) ) {
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

    public final String getName() {
      return name;
    }

    public String toString() {
      return name;
    }

    public boolean isArrayType() {
      switch( this ) {
        case STRING_ARRAY:
        case INTEGER_ARRAY:
        case NUMERIC_ARRAY:
        case DATE_ARRAY:
          return true;
        default:
          return false;
      }
    }

  }

}
