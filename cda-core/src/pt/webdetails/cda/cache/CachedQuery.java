/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.quartz.CronExpression;
import pt.webdetails.cda.cache.CacheScheduleManager;
import pt.webdetails.cda.utils.Util;

/**
 *
 * @author pdpi
 */
public class CachedQuery extends Query
{

  private static final long serialVersionUID = 1L;
  
  private Date lastExecuted, nextExecution;
  private boolean success = true;
  private long timeElapsed;
  private String cronString, userName;


  CachedQuery(JSONObject json) throws JSONException
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
      CacheScheduleManager.logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
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
      CacheScheduleManager.logger.error("Failed to execute query " + toString() + " " + Util.getExceptionDescription(e));
      Logger.getLogger(CachedQuery.class.getName()).log(Level.SEVERE, null, e);
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
