/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronExpression;

/**
 *
 * @author pdpi
 */
public class CachedQuery extends Query
{

  private Date lastExecuted, nextExecution;
  private boolean executeAtStart;
  private String cronString;


  CachedQuery(JSONObject json) throws JSONException
  {
    super(json);

    this.cronString = getJsonString(json,"cronString");
    this.executeAtStart = getJsonBoolean(json,"executeAtStart");
    this.lastExecuted = getJsonDate(json,"lastExecuted");
    updateNext();
  }


  CachedQuery(Query query)
  {
    this.executeAtStart = true;
  }


  CachedQuery()
  {
    super();
    this.executeAtStart = true;
    this.lastExecuted = new Date(0);
  }


  public boolean isExecuteAtStart()
  {
    return executeAtStart;
  }


  public void setExecuteAtStart(boolean executeAtStart)
  {
    this.executeAtStart = executeAtStart;
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


  @Override
  public JSONObject toJSON()
  {
    JSONObject json = super.toJSON();
    try
    {
      json.put("lastExecuted", getLastExecuted().getTime());
      json.put("nextExecution", getNextExecution().getTime());
      json.put("cronString", getCronString());
      json.put("executeAtStart", isExecuteAtStart());
    }
    catch (JSONException ex)
    {
      CacheManager.logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
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
    }
    catch (Exception e)
    {
      CacheManager.logger.error("Failed to execute query " + toString());
    }
    finally
    {
      setLastExecuted(new Date());
      //save();
    }
  }
}
