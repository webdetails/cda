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

package pt.webdetails.cda.utils.framework;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.session.IUserSession;
/**
 * Boilerplate for a JSON content generator
 */
public abstract class JsonCallHandler {

  private static Log logger = LogFactory.getLog(JsonCallHandler.class);
  
  public static final String ENCODING = "UTF-8";
  public static final int INDENT_FACTOR = 2;
  
  private HashMap<String, Method> methods = new HashMap<String, Method>();
  private String methodParameter = "method";
  private String defaultMethod = null;
  
  public static class JsonResultFields 
  {
    public static final String STATUS = "status";
    public static final String ERROR_MSG = "errorMsg";
    public static final String RESULT = "result";
  }
  
  public enum ResponseStatus{
    
    OK,
    ERROR;
    
    @Override public String toString(){
      return super.toString().toLowerCase();
    }
  }
  
  public static abstract class Method {
    
    private String name;
    
    public abstract JSONObject execute(ICommonParameterProvider requParam) throws Exception;
    
    public void setName(String name){
      this.name = name;
    }
    public String getName(){
      return name;
    }
  }

  /**
   * 
   * @param name
   * @param method
   */
  protected void registerMethod(String name, Method method){
    method.setName(name);
    methods.put(name, method);
  }
  
  /**
   * Simple overridable method to limit access. Allows everything by default.
   * @param session Caller session
   * @param method Method being executed
   * @return
   */
  protected boolean hasPermission(IUserSession session,  Method method){
    return true;
  }
  
  public void handleCall(String methodName, ICommonParameterProvider requParam, OutputStream out)
  {
  //TODO: messages
    //String methodName = requestParams.getStringParameter(methodParameter, defaultMethod);
       
    JSONObject result = null;
    Method method = methods.get(methodName);
    IUserSession session = (CdaEngine.getEnvironment().getSessionUtils()).getCurrentSession();

    try 
    {
      if(methodName == null){
        result = getErrorJson("No method received.");
      }
      else if(method == null)
      {
        result = getErrorJson(MessageFormat.format("Method {0} not found.", methodName));
      }
      else if(!hasPermission(session, method)){
        result = getErrorJson(MessageFormat.format("Permission denied to call method {0}:{1}.", this.getClass().getName(), methodName));
      }
      else 
      {
        try
        {
          result = method.execute(requParam);
        }
        catch(JSONException e){
          logger.error( MessageFormat.format("Error building JSON response in method {0}.", methodName) , e);
        }
        catch(Exception e){
          logger.error( MessageFormat.format("Error executing method {0}.", methodName), e);
          result = createJsonResultFromException(e);
        }
      }
      out.write(result.toString(INDENT_FACTOR).getBytes(ENCODING));
      
    } catch (JSONException e) {
      logger.error("Error building JSON response", e);
    } catch (UnsupportedEncodingException e) {
      logger.error("Error attempting to use UTF-8 encoding", e);
    } catch (IOException e) {
      logger.error("Error writing to output stream", e);
    }
  }
  
  public static JSONObject createJsonResultFromException(Exception exc) throws JSONException
  {
    JSONObject result = new JSONObject();
    
    result.put(JsonResultFields.STATUS, ResponseStatus.ERROR);
    result.put(JsonResultFields.ERROR_MSG, exc.getMessage());
    
    return result;
  }
  
  public static JSONObject getErrorJson(String msg) throws JSONException {
    JSONObject result = new JSONObject();
    result.put(JsonResultFields.STATUS, ResponseStatus.ERROR);
    result.put(JsonResultFields.ERROR_MSG, msg);
    return result;
  }

  public static JSONObject getOKJson(Object obj) throws JSONException {
    JSONObject result = new JSONObject();
    result.put(JsonResultFields.STATUS, ResponseStatus.OK);
    result.put(JsonResultFields.RESULT, obj);
    return result;
  }
  
  
}
