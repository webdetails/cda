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

package pt.webdetails.cda.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.IPluginCall;

/**
 *
 * @author pedro
 * WARN: This is used from BeanShell CDA datasources. 
 * There are no references to this class from within CDA and that is expected!
 */
public class InterPluginUtils
{

  private static final Log logger = LogFactory.getLog(TableModelUtils.class);
  private static InterPluginUtils _instance;


  public static synchronized InterPluginUtils getInstance()
  {

    if (_instance == null)
    {
      _instance = new InterPluginUtils();
    }

    return _instance;
  }


  public static TableModel getTableModelFromJsonPluginCall(String plugin, String method)
  {
    return getTableModelFromJsonPluginCall(plugin, method, new HashMap<String, Object>());
  }


  public static TableModel getTableModelFromJsonPluginCall(String plugin, String method, Map<String, Object> params)
  {
	  IPluginCall pluginCall = CdaEngine.getEnvironment().createPluginCall(plugin, method, params);
	  return InterPluginUtils.getInstance().getTableModelFromJSONArray(pluginCall.call(), params);
  }


  public TableModel getTableModelFromJSONArray(String json, Map<String, Object> params)
  {


    ObjectMapper mapper = new ObjectMapper();
    try
    {
      List<Map> list = mapper.readValue(json, List.class);

      // Get columnNames and columnClasses
      Map<Object, Class> cols = new LinkedHashMap<Object, Class>();

      for (Map map : list)
      {
        for (Object key : map.keySet())
        {
          if (!cols.containsKey(key))
          {
            cols.put(key, key.getClass());
          }
        }
      }

      // Do we have columns parameter? If so, add that
      Map<Object, Class> columnOutputs;
      if (params != null && params.containsKey("columns"))
      {
        String[] columns = (String[]) params.get("columns");
        columnOutputs = new LinkedHashMap<Object, Class>();
        for (int i = 0; i < columns.length; i++)
        {
          String column = columns[i];
          // Do we have this in our mapping?
          if (cols.containsKey(column))
          {
            columnOutputs.put(column, cols.get(column));
          }
          else
          {
            columnOutputs.put(column, String.class);
          }
        }


      }
      else
      {
        columnOutputs = cols;
      }



      TypedTableModel tm = new TypedTableModel(columnOutputs.keySet().toArray(new String[0]), columnOutputs.values().toArray(new Class[0]), list.size());
      logger.debug("Done. columnOutputs has " + columnOutputs.size() + " entries");

      // Fill the table model
      for (int i = 0; i < list.size(); i++)
      {
        Map map = list.get(i);

        // Iterate through the columnOutputs
        Object[] keyset = columnOutputs.keySet().toArray();
        for (int j = 0; j < keyset.length; j++)
        {
          Object key = keyset[j];
          if (map.containsKey(key))
          {
            tm.setValueAt(map.get(key), i, j);
          }

        }

      }


      return tm;
    }
    catch (IOException ex)
    {
      logger.error("Error parsing json: " + json, ex);
    }


    return null;


  }


}

