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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cpf.Util;
//import pt.webdetails.cda.PluginHibernateException;
//import pt.webdetails.cda.cache.CacheScheduleManager;

/**
 *
 * @author pdpi
 */
public abstract class Query implements Serializable
{

  private static final long serialVersionUID = 1L;

  private static Log logger = LogFactory.getLog(Query.class);
  private long id;
  private String cdaFile;
  private String dataAccessId;
  private List<CachedParam> parameters;
  private int hitCount, missCount;

  public Query()
  {
  }


  public Query(JSONObject json) throws JSONException
  {
    this();
    this.cdaFile = getJsonString(json, "cdaFile");
    this.dataAccessId = getJsonString(json, "dataAccessId");

    this.hitCount = getJsonInt(json, "hitCount");
    this.missCount = getJsonInt(json, "missCount");

    if (json.has("parameters"))
    {
      this.parameters = new ArrayList<CachedParam>();
      Object params = json.get("parameters");

      if (params instanceof JSONArray)
      {
        for (int i = 0; i < ((JSONArray) params).length(); i++)
        {
          this.parameters.add(new CachedParam(((JSONArray) params).getJSONObject(i)));
        }
      }
      else
      {
        String[] names = JSONObject.getNames((JSONObject) params);
        if (names != null)
        {
          for (String name : names)
          {
            this.parameters.add(new CachedParam(name, ((JSONObject) params).getString(name)));
          }
        }
      }

    }
  }


  public long getId()
  {
    return id;
  }


  public void setId(long id)
  {
    this.id = id;
  }


  public String getCdaFile()
  {
    return cdaFile;
  }


  public void setCdaFile(String cdaFile)
  {
    this.cdaFile = cdaFile;
  }


  public String getDataAccessId()
  {
    return dataAccessId;
  }


  public void setDataAccessId(String dataAccessId)
  {
    this.dataAccessId = dataAccessId;
  }


  public int getHitCount()
  {
    return hitCount;
  }


  public void setHitCount(int hitCount)
  {
    this.hitCount = hitCount;
  }


  public int getMissCount()
  {
    return missCount;
  }


  public void setMissCount(int missCount)
  {
    this.missCount = missCount;
  }


  public List<CachedParam> getParameters()
  {
    return parameters;
  }


  public void setParameters(List<CachedParam> parameters)
  {
    this.parameters = parameters;
  }


  public void addParameter(CachedParam p)
  {
    if (parameters == null)
    {
      this.parameters = new ArrayList<CachedParam>();
    }
    this.parameters.add(p);
  }


  public void registerRequest(boolean hit)
  {
    if (hit)
    {
      setHitCount(getHitCount() + 1);
    }
    else
    {
      setMissCount(getMissCount() + 1);
    }
  }


  public JSONObject toJSON()
  {
    JSONObject output = new JSONObject();
    JSONObject params = new JSONObject();
    try
    {
      for (CachedParam param : getParameters())
      {
        params.put(param.getName(), param.getValue());
      }
    }
    catch (JSONException jse)
    {
      //CacheScheduleManager.logger.error("Failed to build parameters for query: " + getCdaFile() + "/" + getDataAccessId());
        logger.error("Failed to build parameters for query: " + getCdaFile() + "/" + getDataAccessId());
      return null;
    }
    try
    {
      output.put("id", getId());

      output.put("cdaFile", getCdaFile());
      output.put("dataAccessId", getDataAccessId());
      output.put("parameters", params);

      output.put("hitCount", getHitCount());
      output.put("missCount", getMissCount());
    }
    catch (JSONException jse)
    {
      //CacheScheduleManager.logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
        logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
      return null;
    }
    return output;
  }


  public void execute() throws Exception
  {
    try
    {
      executeQuery();
    }
    catch (Exception e)
    {
      //CacheScheduleManager.logger.error("Failed to execute query " + toString());
      logger.error("Failed to execute query " + toString());
    }
  }


  protected void executeQuery() throws Exception
  {

    final CdaSettings cdaSettings = CdaEngine.getInstance().getSettingsManager().parseSettingsFile(getCdaFile());

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId(getDataAccessId());
    // force query to be refreshed
    queryOptions.setCacheBypass(true);

    for (Object o : getParameters())
    {
      CachedParam param = (CachedParam) o;
      queryOptions.addParameter(param.getName(), param.getValue());
    }

    long start = System.currentTimeMillis();
    CdaEngine.getInstance().doQuery( cdaSettings, queryOptions );

    long elapsed = System.currentTimeMillis() - start;
    setTimeElapsed( elapsed );
    logger.debug( "Time elapsed: " + Util.DEFAULT_DURATION_FORMAT_SEC.format( elapsed / 1000d ) );
  }


  public abstract void setTimeElapsed(long timeElapsed);

  public abstract long getTimeElapsed();

  //XXX
  public void save() throws Exception//PluginHibernateException 
  {
    if (getId() == 0)
    {
      
//      Session s = PluginHibernateUtil.getSession();
      
//      s.save(this);
    }
  }


  protected static String getJsonString(JSONObject json, String expr)
  {
    try
    {
      return json.getString(expr);
    }
    catch (Exception e)
    {
      return "";
    }
  }


  protected static int getJsonInt(JSONObject json, String expr)
  {
    try
    {
      return json.getInt(expr);
    }
    catch (Exception e)
    {
      return 0;
    }
  }


  protected static Date getJsonDate(JSONObject json, String expr)
  {
    try
    {
      return new Date(json.getLong(expr));
    }
    catch (Exception e)
    {
      return new Date(0);
    }
  }


  protected static boolean getJsonBoolean(JSONObject json, String expr)
  {
    try
    {
      return json.getBoolean(expr);
    }
    catch (Exception e)
    {
      return false;
    }
  }
}
