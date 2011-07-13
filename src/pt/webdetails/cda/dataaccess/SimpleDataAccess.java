package pt.webdetails.cda.dataaccess;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.ObjectOutputStream;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;

import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.DummyConnection;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.JsonExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.TableModelUtils;
import pt.webdetails.cda.utils.Util;

/**
 * Implementation of the SimpleDataAccess
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:04:10 AM
 */
public abstract class SimpleDataAccess extends AbstractDataAccess
{

  protected static class TableCacheKey implements Serializable
  {

    private static final long serialVersionUID = 3L; //1->2 only hash of connection kept; 2->3 file/dataAccessId
    
    private int connectionHash;
    private String query;
    private ParameterDataRow parameterDataRow;
    private Object extraCacheKey;
    
    private String cdaSettingsId;
    private String dataAccessId;

    /**
     * For serialization
     */
    protected TableCacheKey()
    {
    }


    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
      out.writeInt(connectionHash);
      out.writeObject(query);
      out.writeObject(createParametersFromParameterDataRow(parameterDataRow));
      out.writeObject(extraCacheKey);
      out.writeObject(cdaSettingsId);//information only, not used in hash/equals
      out.writeObject(dataAccessId);//information only, not used in hash/equals
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
      //connection
      connectionHash = in.readInt();
      //query
      query = (String) in.readObject();
      //parameterDataRow
      try
      {
        Object holder = in.readObject();
        if (holder != null)
        {
          parameterDataRow = createParameterDataRowFromParameters((ArrayList<Parameter>) (ArrayList) holder);
        }
        else
        {
          parameterDataRow = null;
        }
      }
      catch (InvalidParameterException e)
      {
        parameterDataRow = null;
      }
      //extraCacheKey
      extraCacheKey = in.readObject();
      cdaSettingsId = (String) in.readObject();
      dataAccessId = (String) in.readObject();
    }


    private TableCacheKey(final Connection connection, final String query,
            final ParameterDataRow parameterDataRow, final Object extraCacheKey, 
            String cdaSettingsId, String dataAccessId)
    {
      if (connection == null)
      {
        throw new NullPointerException();
      }
      if (query == null)
      {
        throw new NullPointerException();
      }
      if (parameterDataRow == null)
      {
        throw new NullPointerException();
      }

      this.connectionHash = connection.hashCode();
      this.query = query;
      this.parameterDataRow = parameterDataRow;
      this.extraCacheKey = extraCacheKey;
      this.cdaSettingsId = cdaSettingsId;
      this.dataAccessId = dataAccessId;
    }


    public boolean equals(final Object o)
    {
      if (this == o)
      {
        return true;
      }
      if (o == null || getClass() != o.getClass())
      {
        return false;
      }

      final TableCacheKey that = (TableCacheKey) o;

      if(connectionHash != that.connectionHash){
        return false;
      }
      if (parameterDataRow != null ? !parameterDataRow.equals(that.parameterDataRow) : that.parameterDataRow != null)
      {
        return false;
      }
      if (query != null ? !query.equals(that.query) : that.query != null)
      {
        return false;
      }
      if (extraCacheKey != null ? !extraCacheKey.equals(that.extraCacheKey) : that.extraCacheKey != null)
      {
        return false;
      }

      return true;
    }


    @Override
    public int hashCode()
    {
      int result = connectionHash;
      result = 31 * result + (query != null ? query.hashCode() : 0);
      result = 31 * result + (parameterDataRow != null ? parameterDataRow.hashCode() : 0);
      result = 31 * result + (extraCacheKey != null ? extraCacheKey.hashCode() : 0);
      return result;
    }
    
    public String getDataAccessId(){
      return this.dataAccessId;
    }
    
    public String getCdaSettingsId(){
      return this.cdaSettingsId;
    }
    
  }
  private static final Log logger = LogFactory.getLog(SimpleDataAccess.class);
  protected String connectionId;
  protected String query;
  private static final String QUERY_TIME_THRESHOLD_PROPERTY = "pt.webdetails.cda.QueryTimeThreshold";
  private static int queryTimeThreshold = getQueryTimeThresholdFromConfig(3600);//seconds


  public SimpleDataAccess()
  {
  }


  public SimpleDataAccess(final Element element)
  {

    super(element);
    connectionId = element.attributeValue("connection");
    query = element.selectSingleNode("./Query").getText();

  }


  /**
   * 
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public SimpleDataAccess(String id, String name, String connectionId, String query)
  {
    super(id, name);
    this.query = query;
    this.connectionId = connectionId;
  }


  protected synchronized TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException
  {

    final Cache cache = getCache();

    // Get parameters from definition and apply it's values
    final ArrayList<Parameter> parameters = (ArrayList<Parameter>) getParameters().clone();

    for (final Parameter parameter : parameters)
    {
      final Parameter parameterPassed = queryOptions.getParameter(parameter.getName());
      if (parameter.getAccess().equals(Parameter.Access.PUBLIC) && parameterPassed != null)
      {
        //parameter.setStringValue(parameterPassed.getStringValue());
        try
        {
          parameterPassed.inheritDefaults(parameter);
          parameter.setValue(parameterPassed.getValue());
        }
        catch (InvalidParameterException e){
          throw new QueryException("Error parsing parameters ", e);
        }
      }
      else
      {
        parameter.setValue(parameter.getDefaultValue());
      }
    }


    final ParameterDataRow parameterDataRow;
    try
    {
      parameterDataRow = createParameterDataRowFromParameters(parameters);
    }
    catch (InvalidParameterException e)
    {
      throw new QueryException("Error parsing parameters ", e);
    }

    // create the cache-key which is both query and parameter values
    TableCacheKey key;
    TableModel tableModelCopy;
    try
    {
      try
      {
        final Connection connection;
        if (getConnectionType() == ConnectionCatalog.ConnectionType.NONE)
        {
          connection = new DummyConnection();
        }
        else
        {
          connection = getCdaSettings().getConnection(getConnectionId());
        }
        key = new TableCacheKey(connection, getQuery(), parameterDataRow, getExtraCacheKey(), 
            this.getCdaSettings().getId(), queryOptions.getDataAccessId());//
      }
      catch (UnknownConnectionException e)
      {
        // I'm sure I'll never be here
        throw new QueryException("Unable to get a Connection for this dataAccess ", e);
      }

      if (isCache())
      {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        try{
          //make sure we have the right class loader in thread to instantiate cda classes in case DiskStore is used
          Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
          final net.sf.ehcache.Element element = cache.get(key);
          if (element != null && !queryOptions.isCacheBypass()) // Are we explicitly saying to bypass the cache?
          {
            final TableModel cachedTableModel = (TableModel) element.getObjectValue();
            if (cachedTableModel != null)
            {
              // we have a entry in the cache ... great!
              logger.debug("Found tableModel in cache. Returning");
              return cachedTableModel;
            }
          }
        }
        catch(Exception e){
          logger.error("Error while attempting to load from cache, bypassing cache (cause: " + e.getClass() + ")", e);
        }
        finally{
          Thread.currentThread().setContextClassLoader(contextCL);
        }
      }

      //start timing query
      long beginTime = System.currentTimeMillis();

      final TableModel tableModel = postProcessTableModel(performRawQuery(parameterDataRow));

      logIfDurationAboveThreshold(beginTime, getId(), getQuery(), parameters);

      // Copy the tableModel and cache it
      // Handle the TableModel

      tableModelCopy = TableModelUtils.getInstance().copyTableModel(this, tableModel);
    }
    catch (Exception e)
    {
      throw new QueryException("Found an unhandled exception:", e);
    }
    finally
    {
      closeDataSource();
    }

    // put the copy into the cache ...
    if (isCache())
    {
      final net.sf.ehcache.Element storeElement = new net.sf.ehcache.Element(key, tableModelCopy);
      storeElement.setTimeToLive(getCacheDuration());
      cache.put(storeElement);
      cache.flush();
      
      // Print cache status size
      logger.debug("Cache status: " + cache.getMemoryStoreSize() + " in memory, " + 
              cache.getDiskStoreSize() + " in disk");
    }

    // and finally return the copy.
    return tableModelCopy;
  }


  /**
   * @param parameters
   * @param beginTime
   */
  private void logIfDurationAboveThreshold(final long beginTime, final String queryId, final String query, final ArrayList<Parameter> parameters)
  {
    long endTime = System.currentTimeMillis();
    long duration = (endTime - beginTime) / 1000;//precision not an issue: integer op is ok
    if (duration > queryTimeThreshold)
    {
      //log query and duration
      String logMsg = "Query " + queryId + " took " + duration + "s.\n";
      logMsg += "\t Query contents: << " + query.trim() + " >>\n";
      if (parameters.size() > 0)
      {
        logMsg += "\t Parameters: \n";
        for (Parameter parameter : parameters)
        {
          logMsg += "\t\t" + parameter.toString() + "\n";
        }
      }
      logger.debug(logMsg);
    }
  }


  private static ParameterDataRow createParameterDataRowFromParameters(final ArrayList<Parameter> parameters) throws InvalidParameterException
  {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    for (final Parameter parameter : parameters)
    {
      names.add(parameter.getName());
      values.add(parameter.getValue());
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow(names.toArray(new String[]
            {
            }), values.toArray());

    return parameterDataRow;

  }


  /**
   * for serialization
   **/
  private static ArrayList<Parameter> createParametersFromParameterDataRow(final ParameterDataRow row)
  {
    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
    if(row != null) for (String name : row.getColumnNames())
    {
      Object value = row.get(name);
      Parameter param = new Parameter (name, value != null ? value : null);
      Parameter.Type type = Parameter.Type.inferTypeFromObject(value);
      param.setType(type);
      parameters.add(param);
    }
    return parameters;
  }


  protected TableModel postProcessTableModel(TableModel tm)
  {
    // we can use this method to override the general behavior. By default, no post processing is done
    return tm;
  }


  /**
   * Extra arguments to be used for the cache key. Defaults to null but classes that
   * extend SimpleDataAccess may decide to implement it
   * @return
   */
  protected Object getExtraCacheKey()
  {
    return null;
  }


  protected abstract TableModel performRawQuery(ParameterDataRow parameterDataRow) throws QueryException;


  public abstract void closeDataSource() throws QueryException;


  public String getQuery()
  {
    return query;
  }


  public String getConnectionId()
  {
    return connectionId;
  }


  @Override
  public ArrayList<PropertyDescriptor> getInterface()
  {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    properties.add(new PropertyDescriptor("query", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("connection", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("cache", PropertyDescriptor.Type.BOOLEAN, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("cacheDuration", PropertyDescriptor.Type.NUMERIC, PropertyDescriptor.Placement.ATTRIB));
    return properties;
  }


  private static int getQueryTimeThresholdFromConfig(int defaultValue)
  {
    String strVal = CdaBoot.getInstance().getGlobalConfig().getConfigProperty(QUERY_TIME_THRESHOLD_PROPERTY);
    if (!Util.isNullOrEmpty(strVal))
    {
      try
      {
        return Integer.parseInt(strVal);
      }
      catch (NumberFormatException nfe)
      {
      }//ignore, use default
    }
    return defaultValue;
  }
  
  
  //TODO: these methods should be moved...
  
  public static JSONObject getcacheQueryTable(String encodedCacheKey) throws JSONException, ExporterException, UnsupportedEncodingException, IOException, ClassNotFoundException {
    
    JSONObject result = new JSONObject();
    Cache cdaCache = AbstractDataAccess.getCache();

    TableCacheKey lookupCacheKey = getTableCacheKeyFromString(encodedCacheKey);
    net.sf.ehcache.Element elem = cdaCache.getQuiet(lookupCacheKey);

    if(elem != null){
      // put query results
      JsonExporter exporter = new JsonExporter(null);
      result.put("table", exporter.getTableAsJson((TableModel) elem.getObjectValue(), 100));
      result.put("success", true);
    }
    else {
      result.put("success", false);
      result.put("errorMsg", "item not found");
    }
    
    return result;
    
  }
  
  public static JSONObject getCachedQueriesOverview() throws JSONException {
    
    HashMap<String, HashMap<String, Integer>> cdaMap = new HashMap<String, HashMap<String,Integer>>();
    
    Cache cdaCache = AbstractDataAccess.getCache();
    JSONArray results = new JSONArray();
    
    for(Object key : cdaCache.getKeys()) {

      TableCacheKey cacheKey = (TableCacheKey) key;
      String cdaSettingsId = cacheKey.getCdaSettingsId();
      String dataAccessId = cacheKey.getDataAccessId();
      
      //aggregate occurrences
      HashMap<String, Integer> dataAccessIdMap = cdaMap.get(cdaSettingsId);
      if( dataAccessIdMap == null ){
        dataAccessIdMap = new HashMap<String, Integer>();
        dataAccessIdMap.put(dataAccessId, 1);
        cdaMap.put(cdaSettingsId, dataAccessIdMap);
      }
      else {
        Integer count = dataAccessIdMap.get(dataAccessId);
        if(count == null){
          dataAccessIdMap.put(dataAccessId, 1);
        }
        else {
          dataAccessIdMap.put(dataAccessId, ++count);
        }
      }
    }
    
    for(String cdaSettingsId :  cdaMap.keySet()){
      for(String dataAccessId : cdaMap.get(cdaSettingsId).keySet() ){
        Integer count = cdaMap.get(cdaSettingsId).get(dataAccessId);
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("cdaSettingsId", cdaSettingsId);
        queryInfo.put("dataAccessId", dataAccessId); 
        queryInfo.put("count", count.intValue());
        
        results.put(queryInfo);
      }
    }
    
    JSONObject result = new JSONObject();
    result.put("success", true);
    result.put("results", results);
    return result;
  }
  
  public static JSONObject removeQueryFromCache(String serializedCacheKey) throws UnsupportedEncodingException, IOException, ClassNotFoundException, JSONException 
  {
    TableCacheKey key = getTableCacheKeyFromString(serializedCacheKey);
    
    Cache cdaCache = AbstractDataAccess.getCache();
    boolean success = cdaCache.remove(key);
    
    JSONObject result = new JSONObject();
    result.put("success", success);
    return result;
  }
  
  public static JSONObject listQueriesInCache(String cdaSettingsId, String dataAccessId) throws JSONException, ExporterException, IOException {
    
    JSONArray results = new JSONArray();
    
    Cache cdaCache = AbstractDataAccess.getCache();
    
    for(Object key : cdaCache.getKeys()) {
      
      if(key instanceof TableCacheKey){
        
        JSONObject queryInfo = new JSONObject();
        
        TableCacheKey cacheKey = (TableCacheKey) key;
        
        if(!StringUtils.equals(cdaSettingsId, cacheKey.getCdaSettingsId()) ||
           !StringUtils.equals(dataAccessId, cacheKey.getDataAccessId()))
        {//not what we're looking for
          continue;
        }
        
        //query
        queryInfo.put("query", cacheKey.query);
        //parameters
        ParameterDataRow prow = cacheKey.parameterDataRow;
        JSONObject parameters = new JSONObject();
        if(prow != null) for(String paramName : prow.getColumnNames()){
          parameters.put(paramName, prow.get(paramName));
        }
        queryInfo.put("parameters", parameters);
        
        //cacheKey.query;
        net.sf.ehcache.Element elem = cdaCache.getQuiet(key);
        
        if(elem != null){
                  
          TableModel tableModel = (TableModel) elem.getObjectValue();
          queryInfo.put("rows", tableModel.getRowCount());
          
          //inserted
          queryInfo.put("inserted", elem.getLatestOfCreationAndUpdateTime());
          queryInfo.put("accessed", elem.getLastAccessTime());
          queryInfo.put("hits", elem.getHitCount()); 
          
          //use id to get table;
          //identifier
          String identifier = getTableCacheKeyAsString(cacheKey);
          queryInfo.put("key", identifier);
          
          results.put(queryInfo);
        }
      }
      else {
        logger.warn("Found non-TableCacheKey object in cache, skipping...");
      }
    }
    
    JSONObject result = new JSONObject();
    result.put("cdaSettingsId", cdaSettingsId);
    result.put("dataAccessId", dataAccessId);
    result.put("results", results);
    //total stats
    result.put("cacheLength", cdaCache.getSize());
    result.put("memoryStoreLength", cdaCache.getMemoryStoreSize());
    result.put("diskStoreLength", cdaCache.getDiskStoreSize());
    return result;
  }
  
  public static JSONObject listQueriesInCache() throws JSONException, ExporterException, IOException {
    
    JSONArray results = new JSONArray();
    
    Cache cdaCache = AbstractDataAccess.getCache();
    
    for(Object key : cdaCache.getKeys()) {
      
      if(key instanceof TableCacheKey){
        
        JSONObject queryInfo = new JSONObject();
        
        TableCacheKey cacheKey = (TableCacheKey) key;
        
        //query
        queryInfo.put("query", cacheKey.query);
        //parameters
        ParameterDataRow prow = cacheKey.parameterDataRow;
        JSONObject parameters = new JSONObject();
        if(prow != null) for(String paramName : prow.getColumnNames()){
          parameters.put(paramName, prow.get(paramName));
        }
        queryInfo.put("parameters", parameters);
        
        //cacheKey.query;
        net.sf.ehcache.Element elem = cdaCache.getQuiet(key);
                
        //inserted
        queryInfo.put("inserted", elem.getLatestOfCreationAndUpdateTime());
        queryInfo.put("accessed", elem.getLastAccessTime());
        queryInfo.put("hits", elem.getHitCount()); 
        
        //use id to get table;TODO: more efficient solution than b64
        //identifier
        String identifier = getTableCacheKeyAsString(cacheKey);
        queryInfo.put("key", identifier);
        
        results.put(queryInfo);
      }
      else {
        logger.warn("Found non-TableCacheKey object in cache, skipping...");
      }
    }
    
    JSONObject result = new JSONObject();
    result.put("results", results);
    result.put("cacheLength", cdaCache.getSize());
    result.put("memoryStoreLength", cdaCache.getMemoryStoreSize());
    result.put("diskStoreLength", cdaCache.getDiskStoreSize());
    return result;
  }


  /**
   * @param cacheKey
   * @return
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private static String getTableCacheKeyAsString(TableCacheKey cacheKey) throws IOException, UnsupportedEncodingException {
    //TODO: more efficient solution than b64?
    ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
    ObjectOutputStream objStream = new ObjectOutputStream(keyStream);
    cacheKey.writeObject(objStream);
    String identifier = new String(Base64.encodeBase64(keyStream.toByteArray()), "UTF-8");
    return identifier;
  }
  
  private static TableCacheKey getTableCacheKeyFromString(String encodedCacheKey) throws IOException, UnsupportedEncodingException, ClassNotFoundException {
    ByteArrayInputStream keyStream = new ByteArrayInputStream( Base64.decodeBase64(encodedCacheKey.getBytes()));
    ObjectInputStream objStream = new ObjectInputStream(keyStream);   
    TableCacheKey cacheKey = new TableCacheKey();
    cacheKey.readObject(objStream);
    return cacheKey;
  }
  
  
}
