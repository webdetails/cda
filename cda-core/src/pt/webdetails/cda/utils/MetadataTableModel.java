/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.utils;

import java.util.HashMap;
import java.util.Map;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

/**
 *
 * @author pdpi
 */
public class MetadataTableModel extends TypedTableModel
{

  private static final long serialVersionUID = 1L;

  private Map<String, String> metadata;


  public MetadataTableModel(String[] colNames, Class<?>[] colTypes, int rowCount)
  {
    super(colNames, colTypes, rowCount);
    metadata = new HashMap<String, String>();
  }


  public MetadataTableModel(String[] colNames, Class<?>[] colTypes, int rowCount, Map<String, String> metadata)
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
