/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.utils;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.InvalidParameterException;
import pt.webdetails.cda.formula.ICdaCoreSessionFormulaContext;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;


public class FormulaEvaluator {
  
  private final static String FORMULA_BEGIN = "${";
  private final static String FORMULA_END = "}";
  
 
  public static String replaceFormula(String text) {
    
    if(!StringUtils.contains(text, FORMULA_BEGIN)) return text;
    try{
      IUserSession session = (CdaEngine.getEnvironment().getSessionUtils()).getCurrentSession();
      ICdaCoreSessionFormulaContext formulaContext = CdaEngine.getEnvironment().getFormulaContext();
      formulaContext.setSession(session);
      
      return replaceFormula(text, formulaContext);//XXX CdaCoreSessionFormulaContext got deleted


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
      if (formulaContext != null) {
    	  formula.initialize(formulaContext);
      } else {
          IUserSession session = (CdaEngine.getEnvironment().getSessionUtils()).getCurrentSession();
          ICdaCoreSessionFormulaContext formulaContext1 = CdaEngine.getEnvironment().getFormulaContext();
          if (formulaContext1 != null) {
        	  formulaContext1.setSession(session);
        	  formula.initialize(formulaContext1);
          }
      }

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
