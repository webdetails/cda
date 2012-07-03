/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import pt.webdetails.cpf.InterPluginCall;

/**
 *
 * @author pedro
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


    InterPluginCall pluginCall = new InterPluginCall(new InterPluginCall.Plugin(plugin), method, params);

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


  public static final void main(String[] args)
  {


    System.out.println("TEST");


    TableModel tm1 = InterPluginUtils.getInstance().getTableModelFromJSONArray("[{\"id\":1,\"type\":\"query\",\"name\":\"Test 1\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/sql-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/sql-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"},{\"id\":2,\"type\":\"query\",\"name\":\"Test 2\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"}]", null);

    HashMap params = new HashMap<String, Object>();
    params.put("columns", new String[]
            {
//              "timestamp", "file", "parameters", "message"
              "id", "XXX","group"
            });

    TableModel tm2 = InterPluginUtils.getInstance().getTableModelFromJSONArray("[{\"id\":1,\"type\":\"query\",\"name\":\"Test 1\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/sql-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/sql-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"},{\"id\":2,\"type\":\"query\",\"name\":\"Test 2\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"}]", params);



  }
}
