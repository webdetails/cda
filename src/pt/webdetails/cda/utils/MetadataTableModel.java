/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

/**
 *
 * @author pdpi
 */
public class MetadataTableModel extends TypedTableModel
{

  private Map<String, String> metadata;


  public MetadataTableModel(String[] colNames, Class[] colTypes, int rowCount)
  {
    super(colNames, colTypes, rowCount);
    metadata = new HashMap<String, String>();
  }


  public MetadataTableModel(String[] colNames, Class[] colTypes, int rowCount, Map<String, String> metadata)
  {
    this(colNames, colTypes, rowCount);
    this.metadata.putAll(metadata);
  }


  public void setMetadata(String key, String value)
  {
    metadata.put(key, value);
  }


  public void setMetadata(String key, Object value)
  {
    metadata.put(key, value.toString());
  }


  public String getMetadata(String key)
  {
    return metadata.get(key);
  }


  public Map<String, String> getAllMetadata()
  {
    return metadata;
  }
}
