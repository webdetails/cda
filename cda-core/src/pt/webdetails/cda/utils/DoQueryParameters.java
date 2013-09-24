/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 *
 * @author joao
 */
public class DoQueryParameters {

    private String path;
    private String solution;
    private String file;
    private String outputType;
    private int outputIndexId;
    private String DataAccessId;
    private boolean bypassCache;
    private boolean paginateQuery;
    private int pageSize;
    private int pageStart;
    private boolean wrapItUp;
    private String jsonCallback;
    private List<String> sortBy;
    private Map<String, Object> extraParams;
    private Map<String, Object> extraSettings;
    
    
    public DoQueryParameters(String path, String solution, String file) {
        this.path = path;
        this.solution = solution;
        this.file = file;
        this.outputType = "json";
        this.outputIndexId = 1;
        this.DataAccessId = "<blank>";
        this.bypassCache = false;
        this.paginateQuery = false;
        this.pageSize = 0;
        this.pageStart = 0;
        this.wrapItUp = false;
        this.jsonCallback="<blank>";
        this.sortBy = new ArrayList<String>();
        extraParams = new HashMap<String, Object>();
        extraSettings = new HashMap<String, Object>();
        
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
    public void setOutputIndexId(int outputIndexId) {
        this.outputIndexId = outputIndexId;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * @param solution the solution to set
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the DataAccessId
     */
    public String getDataAccessId() {
        return DataAccessId;
    }

    /**
     * @param DataAccessId the DataAccessId to set
     */
    public void setDataAccessId(String DataAccessId) {
        this.DataAccessId = DataAccessId;
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
    public void setBypassCache(boolean bypassCache) {
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
    public void setPaginateQuery(boolean paginateQuery) {
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
    public void setPageSize(int pageSize) {
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
    public void setPageStart(int pageStart) {
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
    public void setWrapItUp(boolean wrapItUp) {
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
    public void setSortBy(List<String> sortBy) {
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
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * @return the extraParams
     */
    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    /**
     * @param extraParams the extraParams to set
     */
    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    public boolean hasParameter(String JSONP_CALLBACK) {
        return (getExtraParams().containsKey(JSONP_CALLBACK));
    }
    public Object getAnExtraParameter(String paramName){
        return getExtraParams().get(paramName);
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
    public void setExtraSettings(Map<String, Object> extraSettings) {
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
    public void setJsonCallback(String jsonCallback) {
        this.jsonCallback = jsonCallback;
    }
}

