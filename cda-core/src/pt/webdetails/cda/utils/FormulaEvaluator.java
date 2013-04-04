package pt.webdetails.cda.utils;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
//XXX remove
//import org.pentaho.platform.api.engine.IPentahoSession;
//import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.ICdaCoreSessionFormulaContext;//XXX should I maintain this class and interface?
import pt.webdetails.cda.CdaCoreSessionFormulaContext;
import pt.webdetails.cda.dataaccess.InvalidParameterException;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.session.UserSession;

public class FormulaEvaluator {
  
  private final static String FORMULA_BEGIN = "${";
  private final static String FORMULA_END = "}";
  
 
  public static String replaceFormula(String text) {
    
    if(!StringUtils.contains(text, FORMULA_BEGIN)) return text;
    try{
      IUserSession session = UserSession.session;//XXX implementation of IUserSession
      return replaceFormula(text, new CdaSessionFormulaContext(session));
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
      if (formulaContext != null) formula.initialize(formulaContext);
      else formula.initialize(new CdaSessionFormulaContext(UserSession.session));
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
