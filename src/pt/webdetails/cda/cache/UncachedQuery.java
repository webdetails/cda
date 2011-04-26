/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pdpi
 */
public class UncachedQuery extends Query
{

  public UncachedQuery(JSONObject json) throws JSONException
  {
    super(json);
  }


  public CachedQuery cacheMe()
  {
    return new CachedQuery(this);
  }


  @Override
  public void setTimeElapsed(long timeElapsed)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }


  @Override
  public long getTimeElapsed()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
