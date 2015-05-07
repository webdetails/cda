/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.exporter.ExporterEngine;

public class QueryOptions implements ExportOptions, IQueryArguments, Cloneable {

  private String dataAccessId;
  private int outputIndexId;
  private boolean paginate;
  private int pageSize;
  private int pageStart;
  private List<String> sortBy;
  private List<Parameter> parameters;
  private Map<String, String> extraSettings;
  private String outputType;
  private boolean cacheBypass;
  private List<String> outputColumnName;


  public QueryOptions() {
    outputIndexId = 1;
    paginate = false;
    pageSize = 20;
    pageStart = 0;
    sortBy = new ArrayList<String>();
    parameters = new ArrayList<Parameter>();
    outputType = "json";
    extraSettings = new HashMap<String, String>();
    cacheBypass = false;
    outputColumnName = new ArrayList<String>();
  }


  public boolean isPaginate() {
    return paginate;
  }


  public void setPaginate( final boolean paginate ) {
    this.paginate = paginate;
  }

  public int getPageSize() {
    return pageSize;
  }


  public void setPageSize( final int pageSize ) {
    this.pageSize = pageSize;
  }

  public int getPageStart() {
    return pageStart;
  }


  public void setPageStart( final int pageStart ) {
    this.pageStart = pageStart;
  }


  public List<String> getSortBy() {
    return sortBy;
  }


  public void setSortBy( final List<String> sortBy ) {
    this.sortBy = sortBy;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }


  public String getDataAccessId() {
    return dataAccessId;
  }


  public void setDataAccessId( final String dataAccessId ) {
    this.dataAccessId = dataAccessId;
  }

  public int getOutputIndexId() {
    return outputIndexId;
  }


  public void setOutputIndexId( final int outputIndexId ) {
    this.outputIndexId = outputIndexId;
  }

  public void addParameter( final String name, final Object value ) {

    final Parameter p = new Parameter( name, value );
    p.setDefaultValue( value );
    parameters.add( p );

  }

  public Parameter getParameter( final String name ) {

    for ( final Parameter parameter : parameters ) {
      if ( parameter.getName().equals( name ) ) {
        return parameter;
      }
    }

    return null;

  }


  /**
   * Substitute existing parameter's value, or add if not there.
   */
  public void setParameter( final String name, final String value ) {
    for ( Parameter param : parameters ) {
      if ( param.getName().equals( name ) ) {
        param.setStringValue( value );
        return;
      }
    }
    //not found
    addParameter( name, value );
  }


  public String getOutputType() {
    return outputType;
  }


  public void setOutputType( final String outputType ) {
    this.outputType = outputType;
  }


  public void setOutputType( final ExporterEngine.OutputType outputType ) {
    this.outputType = outputType.toString();
  }


  public void addSetting( String setting, String value ) {
    extraSettings.put( setting, value );
  }


  public String getSetting( String setting ) {
    return extraSettings.get( setting );
  }


  public Map<String, String> getExtraSettings() {
    return extraSettings;
  }


  public boolean isCacheBypass() {
    return cacheBypass;
  }


  public void setCacheBypass( boolean cacheBypass ) {
    this.cacheBypass = cacheBypass;
  }

  public List<String> getOutputColumnName() {
    return this.outputColumnName;
  }

  public void setOutputColumnName( List<String> outputColumnName ) {
    this.outputColumnName = outputColumnName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
