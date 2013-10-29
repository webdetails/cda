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

package pt.webdetails.cda.cache.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.quartz.CronExpression;

import pt.webdetails.cda.utils.Util;


/**
 *
 * @author pdpi
 */
public class CachedQuery extends Query
{

  
  private static final Log logger = LogFactory.getLog(CachedQuery.class);
  private static final long serialVersionUID = 1L;
  private Date lastExecuted, nextExecution;
  private boolean success = true;
  private long timeElapsed;
  private String cronString, userName;


  public CachedQuery(JSONObject json) throws JSONException
  {
    super(json);
    this.userName = PentahoSessionHolder.getSession().getName();
    this.cronString = getJsonString(json, "cronString");
    this.lastExecuted = getJsonDate(json, "lastExecuted");

    updateNext();
  }


  CachedQuery(Query query)
  {
  }


  CachedQuery()
  {
    super();
    this.lastExecuted = new Date(0);
  }


  public boolean isSuccess()
  {
    return success;
  }


  public void setSuccess(boolean success)
  {
    this.success = success;
  }


  public Date getLastExecuted()
  {
    return lastExecuted;
  }


  public Date getNextExecution()
  {
    return nextExecution;
  }


  public void setNextExecution(Date nextExecution)
  {
    this.nextExecution = nextExecution;
  }


  public void setLastExecuted(Date lastExecuted)
  {
    this.lastExecuted = lastExecuted;
  }


  public String getCronString()
  {
    return cronString;
  }


  public void setCronString(String chronString)
  {
    this.cronString = chronString;
  }


  public long getTimeElapsed()
  {
    return timeElapsed;
  }


  public void setTimeElapsed(long timeElapsed)
  {
    this.timeElapsed = timeElapsed;
  }


  @Override
  public JSONObject toJSON()
  {
    JSONObject json = super.toJSON();
    try
    {
      json.put("lastExecuted", getLastExecuted().getTime());
      json.put("nextExecution", getNextExecution().getTime());
      json.put("cronString", getCronString());
      json.put("success", isSuccess());
      json.put("timeElapsed", getTimeElapsed());


    }
    catch (JSONException ex)
    {
      logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
    }


    return json;
  }


  @Override
  public String toString()
  {
    return getCdaFile() + "/" + getDataAccessId();
  }


  public Date updateNext()
  {
    try
    {
      CronExpression ce = new CronExpression(getCronString());
      setNextExecution(ce.getNextValidTimeAfter(new Date()));
      return getNextExecution();
    }
    catch (ParseException ex)
    {
      Logger.getLogger(CachedQuery.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }

  }


  @Override
  public void execute() throws Exception
  {
    try
    {
      executeQuery();
      setSuccess(true);
    }
    catch (Exception e)
    {
      logger.error("Failed to execute query " + toString() + " " + Util.getExceptionDescription(e));
      setSuccess(false);
    }
    finally
    {
      setLastExecuted(new Date());
      updateNext();
    }
  }


  /**
   * @return the userName
   */
  public String getUserName()
  {
    return userName;
  }


  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName)
  {
    this.userName = userName;
  }
}
