/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * TODO: merge with queryOptions?
 */
public class DoQueryParameters {

  private String path;
  private String solution;
  private String file;

  private String outputType;
  private String dataAccessId;
  private String jsonCallback;

  private int outputIndexId;
  private int pageSize;
  private int pageStart;

  private boolean bypassCache;
  private boolean paginateQuery;
  private boolean wrapItUp;

  private List<String> sortBy;
  private List<String> outputColumnName;

  private Map<String, Object> extraParameters;
  private Map<String, Object> extraSettings;

  public DoQueryParameters( String path, String solution, String file ) {
    this.path = path;
    this.solution = solution;
    this.file = file;

    this.outputType = "json";
    this.outputIndexId = 1;
    this.dataAccessId = "<blank>";
    this.jsonCallback = "<blank>";

    this.sortBy = new ArrayList<>();
    this.outputColumnName = new ArrayList<>();

    this.extraParameters = new HashMap<>();
    this.extraSettings = new HashMap<>();
  }

  public DoQueryParameters( String cdaSettingsPath ) {
    this( cdaSettingsPath, null, null );
  }

  public DoQueryParameters() {
    this( null, null, null );
  }

  /**
   * @return the outputIndexId
   */
  public int getOutputIndexId() {

    return outputIndexId;
  }

  /**
   * @param outputIndexId the outputIndexId to set
   */
  public void setOutputIndexId( int outputIndexId ) {
    this.outputIndexId = outputIndexId;
  }

  /**
   * @return the path
   */
  public String getPath() {
    final boolean isLegacyPath = !StringUtils.isEmpty( solution );
    if ( isLegacyPath ) {
      return Util.joinPath( solution, path, file );
    }

    return path;
  }

  /**
   * @param path the path to set
   * @deprecated
   */
  public void setPath( String path ) {
    this.path = path;
  }

  /**
   * @param solution the solution to set
   * @deprecated
   */
  public void setSolution( String solution ) {
    this.solution = solution;
  }

  /**
   * @param file the file to set
   * @deprecated
   */
  public void setFile( String file ) {
    this.file = file;
  }

  /**
   * @return the DataAccessId
   */
  public String getDataAccessId() {
    return dataAccessId;
  }

  /**
   * @param DataAccessId the DataAccessId to set
   */
  public void setDataAccessId( String DataAccessId ) {
    this.dataAccessId = DataAccessId;
  }

  /**
   * @return the bypassCache
   */
  public boolean isBypassCache() {
    return bypassCache;
  }

  /**
   * @param bypassCache the bypassCache to set
   */
  public void setBypassCache( boolean bypassCache ) {
    this.bypassCache = bypassCache;
  }

  /**
   * @return the paginateQuery
   */
  public boolean isPaginateQuery() {
    return paginateQuery;
  }

  /**
   * @param paginateQuery the paginateQuery to set
   */
  public void setPaginateQuery( boolean paginateQuery ) {
    this.paginateQuery = paginateQuery;
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @param pageSize the pageSize to set
   */
  public void setPageSize( int pageSize ) {
    this.pageSize = pageSize;
  }

  /**
   * @return the pageStart
   */
  public int getPageStart() {
    return pageStart;
  }

  /**
   * @param pageStart the pageStart to set
   */
  public void setPageStart( int pageStart ) {
    this.pageStart = pageStart;
  }

  /**
   * @return the wrapItUp
   */
  public boolean isWrapItUp() {
    return wrapItUp;
  }

  /**
   * @param wrapItUp the wrapItUp to set
   */
  public void setWrapItUp( boolean wrapItUp ) {
    this.wrapItUp = wrapItUp;
  }

  /**
   * @return the sortBy
   */
  public List<String> getSortBy() {
    return sortBy;
  }

  /**
   * @param sortBy the sortBy to set
   */
  public void setSortBy( List<String> sortBy ) {
    this.sortBy = sortBy;
  }

  /**
   * @return the outputType
   */
  public String getOutputType() {
    return outputType;
  }

  /**
   * @param outputType the outputType to set
   */
  public void setOutputType( String outputType ) {
    this.outputType = outputType;
  }

  /**
   * @return the extraParams
   */
  public Map<String, Object> getParameters() {
    return extraParameters;
  }

  public void setParameters( Map<String, Object> parameters ) {
    this.extraParameters = parameters;
  }

  public Object getParameter( String paramName ) {
    return getParameters().get( paramName );
  }

  /**
   * @return the extraSettings
   */
  public Map<String, Object> getExtraSettings() {
    return extraSettings;
  }

  /**
   * @param extraSettings the extraSettings to set
   */
  public void setExtraSettings( Map<String, Object> extraSettings ) {
    this.extraSettings = extraSettings;
  }

  /**
   * @return the jsonCallback
   */
  public String getJsonCallback() {
    return jsonCallback;
  }

  /**
   * @param jsonCallback the jsonCallback to set
   */
  public void setJsonCallback( String jsonCallback ) {
    this.jsonCallback = jsonCallback;
  }

  /**
   * @return the outputColumnName
   */
  public List<String> getOutputColumnName() {
    return this.outputColumnName;
  }

  /**
   * @param outputColumnName the outputColumnName to set
   */
  public void setOutputColumnName( List<String> outputColumnName ) {
    this.outputColumnName = outputColumnName;
  }
}
