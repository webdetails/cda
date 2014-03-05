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

package pt.webdetails.cda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;


import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;


/**
 * This is a CDA Pojo Component that can be used in XActions or anywhere else.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class CdaQueryComponent {

  private static final Log log = LogFactory.getLog(CdaQueryComponent.class);
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  
  IPentahoResultSet resultSet = null;
  String file = null;
  Map<String, Object> inputs = new HashMap<String, Object>();
  
  public void setFile(String file) {
    this.file = file;
  }
  
  public void setInputs(Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  public boolean validate() throws Exception {
    if (file == null) {
      log.error("File not set"); //$NON-NLS-1$
      return false;
    }
    // verify file exists
    
    return true;
  }

  private int inputsGetInteger(String name, int defaultVal)
  {
    Object obj = inputs.get(name);
    
    // pojo component forces all strings to upper case :-(
    if (obj == null)
    {
      obj = inputs.get(name.toUpperCase());
    }
    
    if (obj == null)
    {
      return defaultVal;
    }
    
    return new Integer(obj.toString());
  }

  private long inputsGetLong(String name, long defaultVal) {
      Object obj = inputs.get(name);
      // pojo component forces all strings to upper case :-(
      if (obj == null) {
        obj = inputs.get(name.toUpperCase());
      }
      if (obj == null) {
        return defaultVal;
      }
      return new Long(obj.toString());
  }
  
  private String inputsGetString(String name, String defaultVal) {
    Object obj = inputs.get(name);
    // pojo component forces all strings to upper case :-(
    if (obj == null) {
      obj = inputs.get(name.toUpperCase());
    }
    if (obj == null) {
      return defaultVal;
    }
    return obj.toString();
  }
  
  public boolean execute() throws Exception {

    final QueryOptions queryOptions = new QueryOptions();

    final CdaSettings cdaSettings = CdaEngine.getInstance().getSettingsManager().parseSettingsFile(file);
    
    final String CDA_PARAMS = "cdaParameterString";
    final String CDA_PARAM_SEPARATOR = ";";

    // page info
    
    final long pageSize = inputsGetLong("pageSize", 0);
    final long pageStart = inputsGetLong("pageStart", 0);
    final boolean paginate = "true".equals(inputsGetString("paginateQuery", "false"));
    if (pageSize > 0 || pageStart > 0 || paginate) {
      if (pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE) {
        throw new ArithmeticException("Paging values too large");
      }
      queryOptions.setPaginate(true);
      queryOptions.setPageSize(pageSize > 0 ? (int) pageSize : paginate ? DEFAULT_PAGE_SIZE : 0);
      queryOptions.setPageStart(pageStart > 0 ? (int) pageStart : paginate ? DEFAULT_START_PAGE : 0);
    }

    // query info 
    
    queryOptions.setOutputType(inputsGetString("outputType", "resultset"));
    queryOptions.setDataAccessId(inputsGetString("dataAccessId", "<blank>"));
    queryOptions.setOutputIndexId(inputsGetInteger("outputIndexId", 1));
    
    // params and settings
    
    //process parameter string "name1=value1;name2=value2"
    String cdaParamString = inputsGetString(CDA_PARAMS, null);
    if (cdaParamString != null && cdaParamString.trim().length() > 0) {
      
      List<String> cdaParams = new ArrayList<String>();
      //split to 'name=val' tokens
      CSVTokenizer tokenizer = new CSVTokenizer(cdaParamString, CDA_PARAM_SEPARATOR);
      while(tokenizer.hasMoreTokens()){
        cdaParams.add(tokenizer.nextToken());
      }
      
      //split '='
      for(String nameValue : cdaParams){
        int i = 0;
        CSVTokenizer nameValSeparator = new CSVTokenizer(nameValue, "=");
        String name=null, value=null;
        while(nameValSeparator.hasMoreTokens()){
          if(i++ == 0){
            name = nameValSeparator.nextToken();
          }
          else {
            value = nameValSeparator.nextToken();
            break;
          }
        }
        if(name != null) queryOptions.addParameter(name, value);
      }
    }
    
	for (String param : inputs.keySet()) {
      if (param.startsWith("param")) {
        queryOptions.addParameter(param.substring(5), inputsGetString(param, ""));
      } else if (param.startsWith("setting")) {
        queryOptions.addSetting(param.substring(7), inputsGetString(param, ""));
      }
    }

      // TODO: Support binary outputs if outputtype not equals resultset
    if (queryOptions.getOutputType().equals("resultset")) {
      TableModel tableModel = cdaSettings.getDataAccess(queryOptions.getDataAccessId()).doQuery(queryOptions);
      resultSet = convertTableToResultSet(tableModel);
    } 
    
    return true;
  }
  
  private IPentahoResultSet convertTableToResultSet(TableModel tableModel) {
    List<String> columnNames = new ArrayList<String>();
    for (int i = 0; i < tableModel.getColumnCount(); i++) {
      columnNames.add(tableModel.getColumnName(i));
    }
    MemoryMetaData metadata = new MemoryMetaData(columnNames);
    
    MemoryResultSet resultSet = new MemoryResultSet();
    resultSet.setMetaData(metadata);
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      Object row[] = new Object[tableModel.getColumnCount()];
      for (int j = 0; j < tableModel.getColumnCount(); j++) {
        row[j] = tableModel.getValueAt(i, j);
      }
      resultSet.addRow(row);
    }
    return resultSet;
  }
  
  public IPentahoResultSet getResultSet() {
    return resultSet;
  }
}
