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
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pdpi
 */
public class CachedParam implements Serializable
{

  private static final long serialVersionUID = 1L;

  private long id;
  private String name, value;


  public CachedParam()
  {
  }


  public CachedParam(String name, String value)
  {
    this.name = name;
    this.value = value;
  }


  CachedParam(JSONObject json) throws JSONException
  {
    this.name = json.getString("name");
    this.value = json.getString("value");
  }


  public long getId()
  {
    return id;
  }


  public void setId(long id)
  {
    this.id = id;
  }


  public String getName()
  {
    return name;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getValue()
  {
    return value;
  }


  public void setValue(String value)
  {
    this.value = value;
  }
}
