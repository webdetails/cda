package pt.webdetails.cda.dataaccess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.pentaho.reporting.libraries.base.util.CSVTokenizer;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import pt.webdetails.cda.utils.FormulaEvaluator;
import pt.webdetails.cda.utils.Util;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 4:09:48 PM
 */
public class Parameter implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  
  final static String DEFAULT_ARRAY_SEPERATOR = ";";

  private String name;
  private Type type;
  private Object defaultValue;
  private String pattern = StringUtils.EMPTY;
  private Object value;
  private Access access = Access.PUBLIC;
  
  private String separator = DEFAULT_ARRAY_SEPERATOR;
  
  public enum Access {
  	PRIVATE("private"),
  	PUBLIC("public");
  	
  	private String name;
  	
  	Access(String name){ this.name = name; }
  	
  	public static Access parse(String text){//TODO: -> valueOf
  		for(Access type : Access.values()){
  			if(text != null && type.name.equals(text.trim().toLowerCase())){
  				return type;
  			}
  		}
  		return PUBLIC;//default
  	}
  	public String toString(){ return this.name;}
  }
  
  private final static String FORMULA_BEGIN = "${";
  private final static String FORMULA_END = "}";
  
  private FormulaContext formulaContext;
  public void setFormulaContext(FormulaContext context){
  	formulaContext = context;
  }
  
  enum Type{
  	
  	STRING("String"),
  	INTEGER("Integer"),
  	NUMERIC("Numeric"),
  	DATE("Date"),
    STRING_ARRAY("StringArray"),
    INTEGER_ARRAY("IntegerArray"),
    NUMERIC_ARRAY("NumericArray"),
    DATE_ARRAY("DateArray");
  	
  	private String name;
  	
  	Type(String name){
  		this.name = name;
  	}
  	
  	public final String getName(){
  		return name;
  	}
  	
  	public String toString(){
  		return name;
  	}
  	
  	public boolean isArrayType(){
  	  switch(this){
  	    case STRING_ARRAY:
  	    case INTEGER_ARRAY:
  	    case NUMERIC_ARRAY:
  	    case DATE_ARRAY:
  	      return true;
  	    default:
  	      return false;
  	  }
  	}
  	
    public static Type parse(String typeString) {// throws ParseException{ //TODO: -> valueOf
      for (Type type : Type.values()) {
        if (type.name.equals(typeString)) {
          return type;
        }
      }
      // throw new ParseException(typeString + " is not recognized by " + Type.class.getCanonicalName(),0);
      return null;
    }
  	
    public static Type inferTypeFromObject(Object obj) {
      if (obj != null) {
        if (Object[].class.isAssignableFrom(obj.getClass())) {
          if (Double[].class.isAssignableFrom(obj.getClass())) {
            return NUMERIC_ARRAY;
          } else if (Integer[].class.isAssignableFrom(obj.getClass())) {
            return INTEGER_ARRAY;
          } else if (Date[].class.isAssignableFrom(obj.getClass())) {
            return DATE_ARRAY;
          } else if (String[].class.isAssignableFrom(obj.getClass())) {
            return STRING_ARRAY;
          }
        } else if (Double.class.isAssignableFrom(obj.getClass())) {
          return NUMERIC;
        } else if (Integer.class.isAssignableFrom(obj.getClass())) {
          return INTEGER;
        } else if (Date.class.isAssignableFrom(obj.getClass())) {
          return DATE;
        } else if (String.class.isAssignableFrom(obj.getClass())) {
          return STRING;
        }
      }
      return null;// default
    }

  }

  public Parameter()
  {
  }

  public Parameter(final String name, final String type, final String defaultValue, final String pattern, final String access)
  {
    this.name = name;
    this.type = Type.parse(type);//defaults to null
    this.defaultValue = defaultValue;
    this.pattern = pattern;
    this.access = Access.parse(access);//defaults to public
  }

  public Parameter(final Element p)
  {
    this(
        p.attributeValue("name"),
        p.attributeValue("type"),
        p.attributeValue("default"),
        p.attributeValue("pattern"),
        p.attributeValue("access")
    );
  }

  public Parameter(final String name, final Object value)
  {
    this.name = name;
    this.value = value;
  }
  
  public void inheritDefaults(Parameter defaultParameter){
    if(this.type == null) this.setType(defaultParameter.getType());
    if(this.type == Type.DATE || this.type == Type.DATE_ARRAY) this.setPattern(defaultParameter.getPattern());
    this.setSeparator(defaultParameter.getSeparator());
  }


  public Object getValue() throws InvalidParameterException
  {
    final Object objValue = value == null ? getDefaultValue() : value;

    if(objValue instanceof String){//may be a string or a parsed value
      final String strValue = (String) objValue;
      //check if it is a formula
      if(strValue != null && strValue.trim().startsWith(FORMULA_BEGIN)){
      	return FormulaEvaluator.processFormula(Util.getContentsBetween(strValue, FORMULA_BEGIN, FORMULA_END), this.formulaContext);
      }
      
      Type valueType = getType();
      if(valueType == null){
        throw  new InvalidParameterException("Parameter type " + getType() + " unknown, can't continue",null);
      }
      value = getValueFromString(strValue, valueType);
      return value;
    }
    else return objValue;
  }

  /**
   * @param localValue
   * @param valueType
   * @return
   * @throws InvalidParameterException
   */
  private Object getValueFromString(final String localValue, Type valueType) throws InvalidParameterException {
    
    switch(valueType){
      case STRING:
        return localValue;
      case INTEGER:
        return Integer.parseInt(localValue);
      case NUMERIC:
        return Double.parseDouble(localValue);
      case DATE:
        SimpleDateFormat format = new SimpleDateFormat(getPattern());
        try
        {
          return format.parse(localValue);
        }
        catch (ParseException e)
        {
          throw new InvalidParameterException("Unable to parse " + Type.DATE.getName() + " '" + localValue + "' with pattern " + getPattern() , e);
        }
      case STRING_ARRAY:
        return parseToArray(localValue, Type.STRING);
      case DATE_ARRAY:
        return parseToArray(localValue, Type.DATE);
      case INTEGER_ARRAY:
        return parseToArray(localValue, Type.INTEGER);
      case NUMERIC_ARRAY:
        return parseToArray(localValue, Type.NUMERIC);
      default:
         return localValue;
    }
  }

  private Object[] parseToArray(String arrayAsString, Type elementType) throws InvalidParameterException
  {    
    CSVTokenizer tokenizer = new CSVTokenizer(arrayAsString, getSeparator());
    
    ArrayList<Object> result = new ArrayList<Object>();
    while( tokenizer.hasMoreTokens()){
      result.add(getValueFromString(tokenizer.nextToken(), elementType));
    }
    return result.toArray();
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public Type getType()
  {
    return type;
  }
  
  public String getTypeAsString(){//TODO:temp
    return (type == null) ? null : type.getName();
  }

  public void setType(final String type)
  {
    this.type = Type.parse(type);
  }
  
  public void setType(final Type type){
    this.type = type;
  }

  public Object getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(final Object defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getPattern()
  {
    return pattern;
  }

  public void setPattern(final String pattern)
  {
    this.pattern = pattern;
  }

  public String getStringValue() {
    String separator = getSeparator();
    if(separator == null) separator = DEFAULT_ARRAY_SEPERATOR;
      
    if (value == null) {
      if (getDefaultValue() != null) return getDefaultValue().toString();
      else return null;
    } else if (type != null) {
      switch (type) {
        case STRING_ARRAY:
        case DATE_ARRAY:
        case INTEGER_ARRAY:
        case NUMERIC_ARRAY:
          Object[] arr = (Object[]) value;
          int i = 0;
          StringBuilder strBuild = new StringBuilder();
          for (Object o : arr) {
            if (i++ > 0) strBuild.append(separator);
            strBuild.append(o);
          }
          return strBuild.toString();
      }
    }
    return value.toString();
  }

  public void setStringValue(final String stringValue)
  {
    if(this.type == null){
     //TODO: warn 
    }
    this.value = stringValue;
  }
  
  public void setStringValue(final String stringValue, Type type){
    this.value = stringValue;//TODO: parse now
    this.type = type;
  }
  
  public void setValue(final Object value){
    this.value = value;
  }
  
  public Access getAccess(){
  	return this.access;
  }
  
  public void setSeparator(String separator){
    this.separator = separator;
  }
  public String getSeparator(){
    if(this.separator == null) return DEFAULT_ARRAY_SEPERATOR;
    else return this.separator;
  }
  /**
   * For debugging purposes
   */
  public String toString(){
  	return getName() + "=" + getStringValue();
  }

}
