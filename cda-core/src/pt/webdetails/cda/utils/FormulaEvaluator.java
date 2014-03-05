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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.InvalidParameterException;


public class FormulaEvaluator {
  
  private final static String FORMULA_BEGIN = "${";
  private final static String FORMULA_END = "}";
  
 
  public static String replaceFormula(String text) {
    
    if(!StringUtils.contains(text, FORMULA_BEGIN)) return text;
    try{
      FormulaContext formulaContext = CdaEngine.getEnvironment().getFormulaContext();
      
      return replaceFormula(text, formulaContext);


    }
    catch(Exception e){//TODO: change
      throw new RuntimeException(e);
    }
  }

  public static String replaceFormula(String text, FormulaContext context) throws InvalidParameterException
  { 
    int startIdx = StringUtils.indexOf(text, FORMULA_BEGIN);
    int contentStartIdx = startIdx + FORMULA_BEGIN.length();
    
    if(startIdx > -1)
    { 
      int contentEndIdx = StringUtils.lastIndexOf(text, FORMULA_END);
      int endIdx = contentEndIdx + FORMULA_END.length();
      if(contentEndIdx >= contentStartIdx)
      {  
        String contents = StringUtils.substring(text, contentStartIdx, contentEndIdx);
        
        StringBuilder result = new StringBuilder();
        result.append(StringUtils.substring(text, 0, startIdx) );
        result.append(processFormula(contents, context));
        result.append(StringUtils.substring(text, endIdx, text.length()));
        
        return result.toString(); 
      }
      //TODO: else throw something
    }
    return text;
  }
  
  public static Object processFormula(String localValue) throws InvalidParameterException 
  {
    return processFormula(localValue, null);
  }
  
  public static Object processFormula(String localValue, FormulaContext formulaContext) throws InvalidParameterException 
  {
    try {
      Formula formula = new Formula(localValue);

      // set context if available
      if (formulaContext == null) {
        formulaContext = CdaEngine.getEnvironment().getFormulaContext();
      }
      formula.initialize(formulaContext);

      // evaluate
      Object result = formula.evaluate();
      if(result instanceof ArrayList)
      {//TODO: this returns Object[] with no specific type
          result = ((ArrayList<?>) result).toArray();
      }
      return result;
    } catch (org.pentaho.reporting.libraries.formula.parser.ParseException e) {
      throw new InvalidParameterException("Unable to parse expression " + localValue, e);
    } catch (org.pentaho.reporting.libraries.formula.EvaluationException e) {
      throw new InvalidParameterException("Unable to evaluate expression " + localValue, e);
    }
  }
  
}
