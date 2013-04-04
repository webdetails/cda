/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
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
