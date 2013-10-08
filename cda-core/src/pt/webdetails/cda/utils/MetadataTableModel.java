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
