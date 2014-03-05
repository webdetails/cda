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

package pt.webdetails.cda.events;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryErrorEvent extends CdaEvent {
  
  private Throwable e;

  public QueryErrorEvent(QueryInfo queryInfo, Throwable e) throws JSONException {
    super(CdaEventType.QueryError, queryInfo);
    this.e = e;
  }
  
  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject obj = super.toJSON();
    obj.put("exceptionType", e.getClass().getName());
    obj.put("message", e.getMessage());
    obj.put("stackTrace", toStringArray(e.getStackTrace()));
    return obj;
  }
  
  private static String[] toStringArray(final StackTraceElement[] stackTrace){
    if(stackTrace == null) return null;
    String[] result = new String[stackTrace.length];
    for(int i = 0; i< stackTrace.length; i++ ){
      result[i] = stackTrace[i].toString();
    }
    return result;
  }

}
