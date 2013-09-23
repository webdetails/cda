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

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;


/**
 * Provides server-side filtering akin to <a href="http://datatables.net/ref">DataTables</a>' bFilter option.<br>
 * All search terms must have a hit in one of the columns for a match  
 */
public class DataTableFilter {

  private static final String TERM_SEPARATOR = " ";
  
  private String[] searchTerms;
  private int[] searchableColumns;
  
  /**
   * 
   * @param searchText space-separated list of search terms
   * @param searchableColumns columns indexes to search on
   */
  public DataTableFilter(String searchText, int[] searchableColumns){
    
    if(searchableColumns == null || searchText == null){
      throw new IllegalArgumentException(DataTableFilter.class.getName() + " cannot have null arguments.");
    }
    
    this.searchableColumns = searchableColumns;
    this.searchTerms = StringUtils.split(searchText, TERM_SEPARATOR);
  }
  
  public boolean rowContainsSearchTerms(TableModel table, int rowIndex){
    String[] columnValues = getRelevantColumns(table, rowIndex);
    
    for(String searchTerm : searchTerms) {
      boolean containsTerm = false;
      for(String value: columnValues){
        if(StringUtils.containsIgnoreCase(value, searchTerm)){
          containsTerm = true;
          continue;
        }
      }
      if(!containsTerm) return false;
    }
    
    return true;
    
  }
  
  private String[] getRelevantColumns(TableModel table, int rowIndex){
    String[] row = new String[searchableColumns.length];
    for(int i=0; i< row.length; i++){
      Object rawValue = table.getValueAt(rowIndex, searchableColumns[i]);
      row[i] = (rawValue == null) ? null : rawValue.toString();
    }
    return row;
  }
  
}
