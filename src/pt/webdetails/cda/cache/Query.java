/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.output.NullOutputStream;
import org.hibernate.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;

/**
 *
 * @author pdpi
 */
public class Query implements Serializable
{

  private long id;
  private String cdaFile;
  private String dataAccessId;
  private List parameters;
  private Double timeElapsed;
  private int hitCount, missCount;


  public Query()
  {
  }


  public Query(JSONObject json) throws JSONException
  {
    this.cdaFile = getJsonString(json, "cdaFile");
    this.dataAccessId = getJsonString(json, "dataAccessId");

    this.hitCount = getJsonInt(json, "hitCount");
    this.missCount = getJsonInt(json, "missCount");

    if (json.has("parameters"))
    {
      JSONArray params = json.getJSONArray("parameters");
      this.parameters = new ArrayList();
      for (int i = 0; i < params.length(); i++)
      {
        this.parameters.add(new CachedParam((JSONObject) params.get(i)));
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


  public List getParameters()
  {
    return parameters;
  }


  public void setParameters(List parameters)
  {
    this.parameters = parameters;
  }


  public void addParameter(CachedParam p)
  {
    if (parameters == null)
    {
      this.parameters = new ArrayList();
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
      for (Object o : getParameters())
      {
        CachedParam param = (CachedParam) o;
        params.put(param.getName(), param.getValue());
      }
    }
    catch (JSONException jse)
    {
      CacheManager.logger.error("Failed to build parameters for query: " + getCdaFile() + "/" + getDataAccessId());
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
      CacheManager.logger.error("Failed to build JSON for query: " + getCdaFile() + "/" + getDataAccessId());
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
      CacheManager.logger.error("Failed to execute query " + toString());
    }
  }


  protected void executeQuery() throws Exception
  {
    HibernateUtil.getSession().merge(this);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(getCdaFile());

    final QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId(getDataAccessId());
    // force query to be refreshed
    queryOptions.setCacheBypass(true);

    for (Object o : getParameters())
    {
      CachedParam param = (CachedParam) o;
      queryOptions.addParameter(param.getName(), param.getValue());
    }

    OutputStream nullOut = new NullOutputStream();
    Date d = new Date();
    CdaEngine.getInstance().doQuery(nullOut, cdaSettings, queryOptions);
    setTimeElapsed(new Double((new Date().getTime() - d.getTime()) / 1000));
    CacheManager.logger.debug("Time elapsed: " + new Double(timeElapsed).toString() + "s");
  }


  public void save()
  {
    if (getId() == 0)
    {
      Session s = HibernateUtil.getSession();
      s.save(this);
    }
  }


  public Double getTimeElapsed()
  {
    return timeElapsed;
  }


  public void setTimeElapsed(Double timeElapsed)
  {
    this.timeElapsed = timeElapsed;
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
