/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.cache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pdpi
 */
public class UncachedQuery extends Query
{

  private static final long serialVersionUID = 1L;


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
