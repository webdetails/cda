package pt.webdetails.cda.dataaccess;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.table.TableModel;

import mondrian.olap.InvalidArgumentException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaContentGenerator;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.InvalidOutputIndexException;
import pt.webdetails.cda.utils.TableModelException;
import pt.webdetails.cda.utils.TableModelUtils;
import pt.webdetails.cda.utils.Util;
import pt.webdetails.cda.utils.kettle.SortException;

/**
 * This is the top level implementation of a DataAccess. Only the common methods are used here
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:05:38 AM
 */
public abstract class AbstractDataAccess implements DataAccess
{

  private static final Log logger = LogFactory.getLog(AbstractDataAccess.class);
  private static CacheManager cacheManager;
  private CdaSettings cdaSettings;
  private String id;
  private String name;
  private DataAccessEnums.ACCESS_TYPE access = DataAccessEnums.ACCESS_TYPE.PUBLIC;
  private boolean cache = false;
  private int cacheDuration = 3600;
  private ArrayList<Parameter> parameters;
  private HashMap<Integer, OutputMode> outputMode;
  private HashMap<Integer, ArrayList<Integer>> outputs;
  private ArrayList<ColumnDefinition> columnDefinitions;
  private HashMap<Integer, ColumnDefinition> columnDefinitionIndexMap;
  private FormulaContext formulaContext;
  private static final String CACHE_NAME = "pentaho-cda-dataaccess";
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_CFG_FILE_DIST = "ehcache-dist.xml";
  private static final String PLUGIN_PATH = "system/" + CdaContentGenerator.PLUGIN_NAME + "/";
  private static final String PARAM_ITERATOR_BEGIN = "$FOREACH(";
  private static final String PARAM_ITERATOR_END = ")";
  private static final String PARAM_ITERATOR_ARG_SEPARATOR = ",";
  private static final String USE_TERRACOTTA_PROPERTY = "pt.webdetails.cda.UseTerracotta";


  protected AbstractDataAccess()
  {
  }


  protected AbstractDataAccess(final Element element)
  {
    name = "";
    columnDefinitionIndexMap = new HashMap<Integer, ColumnDefinition>();
    columnDefinitions = new ArrayList<ColumnDefinition>();
    parameters = new ArrayList<Parameter>();
    outputs = new HashMap<Integer, ArrayList<Integer>>();
    outputs.put(1, new ArrayList<Integer>());
    outputMode = new HashMap<Integer, OutputMode>();
    outputMode.put(1, OutputMode.INCLUDE);

    parseOptions(element);

  }


  /**
   * @param id
   * @param name
   */
  protected AbstractDataAccess(String id, String name)
  {
    this.name = name;
    this.id = id;

    columnDefinitionIndexMap = new HashMap<Integer, ColumnDefinition>();
    columnDefinitions = new ArrayList<ColumnDefinition>();
    parameters = new ArrayList<Parameter>();
    outputs = new HashMap<Integer, ArrayList<Integer>>();
    outputs.put(1, new ArrayList<Integer>());
    outputMode = new HashMap<Integer, OutputMode>();
    outputMode.put(1, OutputMode.INCLUDE);
  }


  /**
   * 
   * @param params
   */
  public void setParameters(Collection<Parameter> params)
  {
    if (this.formulaContext != null)
    {
      for (Parameter param : params)
      {
        param.setFormulaContext(this.formulaContext);
      }
    }

    this.parameters.clear();
    this.parameters.addAll(params);
  }


  public void setFormulaContext(FormulaContext formulaContext)
  {
    this.formulaContext = formulaContext;
    for (Parameter param : this.parameters)
    {
      param.setFormulaContext(formulaContext);
    }
  }


  public abstract String getType();


  private void parseOptions(final Element element)
  {
    id = element.attributeValue("id");

    final Element nameElement = (Element) element.selectSingleNode("./Name");
    if (nameElement != null)
    {
      name = nameElement.getTextTrim();
    }

    if (element.attributeValue("access") != null && element.attributeValue("access").equals("private"))
    {
      access = DataAccessEnums.ACCESS_TYPE.PRIVATE;
    }

    if (element.attributeValue("cache") != null && element.attributeValue("cache").equals("true"))
    {
      cache = true;
    }

    if (element.attribute("cacheDuration") != null && !element.attribute("cacheDuration").toString().equals(""))
    {
      cacheDuration = Integer.parseInt(element.attributeValue("cacheDuration"));
    }


    // Parse parameters
    final List<Element> parameterNodes = element.selectNodes("Parameters/Parameter");

    for (final Element p : parameterNodes)
    {
      parameters.add(new Parameter(p));
    }

    // Parse outputs
    final List<Element> outputNodes = element.selectNodes("Output");

    for (final Element outputNode : outputNodes)
    {
      ArrayList<Integer> myOutputs = new ArrayList<Integer>();
      if (outputNode != null)
      {
        int localId = 1;
        if (outputNode.attribute("id") != null && !outputNode.attribute("id").toString().equals(""))
        {
          // if parseInt fails an exception will be thrown and the cda file will not be accepted
          localId = Integer.parseInt(outputNode.attributeValue("id"));
        }
        else
        {
          // if an output has not a defined or empty id then it will have key = 1
          localId = 1;
        }

        try
        {
          outputMode.put(localId, OutputMode.valueOf(outputNode.attributeValue("mode").toUpperCase()));
        }
        catch (Exception e)
        {
          // if there are any errors, go back to the default					
          outputMode.put(localId, OutputMode.INCLUDE);
        }

        final String[] indexes = outputNode.attributeValue("indexes").split(",");
        for (final String index : indexes)
        {
          myOutputs.add(Integer.parseInt(index));
        }
        outputs.put(localId, myOutputs);
      }
    }

    // Parse Columns
    final List<Element> columnNodes = element.selectNodes("Columns/*");

    for (final Element p : columnNodes)
    {
      columnDefinitions.add(new ColumnDefinition(p));
    }

    // Build the columnDefinitionIndexMap
    final ArrayList<ColumnDefinition> cols = getColumns();
    for (final ColumnDefinition columnDefinition : cols)
    {
      columnDefinitionIndexMap.put(columnDefinition.getIndex(), columnDefinition);
    }

  }


  public static synchronized Cache getCache() throws CacheException
  {
    if (cacheManager == null)
    {// 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
      boolean useTerracotta = Boolean.parseBoolean(CdaBoot.getInstance().getGlobalConfig().getConfigProperty(USE_TERRACOTTA_PROPERTY));
      String cacheConfigFile = useTerracotta ? CACHE_CFG_FILE_DIST : CACHE_CFG_FILE;

      if (CdaEngine.getInstance().isStandalone())
      {//look for the one under src/jar
        URL cfgFile = CdaBoot.class.getResource(cacheConfigFile);
        cacheManager = new CacheManager(cfgFile);//CacheManager.create(cfgFile);
      }
      else
      {//look at cda folder in pentaho
        try
		{
    	 String cfgFile = PentahoSystem.getApplicationContext().getSolutionPath(PLUGIN_PATH + cacheConfigFile);
    	 cacheManager = new CacheManager(cfgFile);
    	}
    	catch(CacheException exc)
		{//fallback to standalone 
    	  URL cfgFile = CdaBoot.class.getResource(cacheConfigFile);
    	  cacheManager = new CacheManager(cfgFile);
    	}
      }
      // enable clean shutdown so ehcache's diskPersistent attribute can work
      if (!useTerracotta)
      {
        enableCacheProperShutdown(true);
      }

    }

    if (cacheManager.cacheExists(CACHE_NAME) == false)
    {
      cacheManager.addCache(CACHE_NAME);
    }

    return cacheManager.getCache(CACHE_NAME);
  }


  private static void enableCacheProperShutdown(final boolean force)
  {
    if (!force)
    {
      try
      {
        System.getProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY);
        return;//unless force, ignore if already set
      }
      catch (NullPointerException npe)
      {
      } // key null, continue
      catch (InvalidArgumentException iae)
      {
      }// key not there, continue
      catch (SecurityException se)
      {
        return;//no permissions to set
      }
    }
    System.setProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
  }


  public static synchronized void clearCache() throws CacheException
  {
    
    Cache cache = getCache();
    cache.removeAll();
  }


  public TableModel doQuery(final QueryOptions queryOptions) throws QueryException
  {

    Map<String, Iterable<String>> iterableParameters = getIterableParametersValues(queryOptions);

    if (!iterableParameters.isEmpty())
    {
      return doQueryOnIterableParameters(queryOptions, iterableParameters);
    }

    /*
     *  Do the tableModel PostProcessing
     *  1. Sort
     *  2. Show only the output columns
     *  3. Paginate
     *  4. Call the appropriate exporter
     *
     */

    try
    {
      final TableModel tableModel = queryDataSource(queryOptions);
      final TableModel outputTableModel = TableModelUtils.getInstance().postProcessTableModel(this, queryOptions, tableModel);
      logger.debug("Query " + getId() + " done successfully - returning tableModel");
      return outputTableModel;
    }
    catch (InvalidOutputIndexException e)
    {
      throw new QueryException("Error while setting output index id ", e);
    }
    catch (SortException e)
    {
      throw new QueryException("Error while sorting output ", e);
    }
    catch (TableModelException e)
    {
      throw new QueryException("Could not create outputTableModel ", e);
    }
  }


  public TableModel listParameters(final DiscoveryOptions discoveryOptions)
  {

    return TableModelUtils.getInstance().dataAccessParametersToTableModel(getParameters());

  }


  protected abstract TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException;


  public abstract void closeDataSource() throws QueryException;


  public ArrayList<ColumnDefinition> getColumns()
  {

    final ArrayList<ColumnDefinition> list = new ArrayList<ColumnDefinition>();

    for (final ColumnDefinition definition : columnDefinitions)
    {
      if (definition.getType() == ColumnDefinition.TYPE.COLUMN)
      {
        list.add(definition);
      }
    }
    return list;

  }


  public ColumnDefinition getColumnDefinition(final int idx)
  {

    return columnDefinitionIndexMap.get(new Integer(idx));

  }


  public ArrayList<ColumnDefinition> getCalculatedColumns()
  {

    final ArrayList<ColumnDefinition> list = new ArrayList<ColumnDefinition>();

    for (final ColumnDefinition definition : columnDefinitions)
    {
      if (definition.getType() == ColumnDefinition.TYPE.CALCULATED_COLUMN)
      {
        list.add(definition);
      }
    }
    return list;

  }


  public void storeDescriptor(DataAccessConnectionDescriptor descriptor)
  {
    ////
  }


  public String getId()
  {
    return id;
  }


  public String getName()
  {
    return name;
  }


  public void setName(final String name)
  {
    this.name = name;
  }


  public DataAccessEnums.ACCESS_TYPE getAccess()
  {
    return access;
  }


  public boolean isCache()
  {
    return cache;
  }


  public int getCacheDuration()
  {
    return cacheDuration;
  }


  public CdaSettings getCdaSettings()
  {
    return cdaSettings;
  }


  public void setCdaSettings(final CdaSettings cdaSettings)
  {
    this.cdaSettings = cdaSettings;
  }


  public ArrayList<Parameter> getParameters()
  {
    return parameters;
  }


  public ArrayList<Integer> getOutputs()
  {
    if (outputs.isEmpty())
    {
      return new ArrayList<Integer>();
    }
    return (ArrayList<Integer>) outputs.get(1);
  }


  public ArrayList<Integer> getOutputs(int id)
  {
    return (ArrayList<Integer>) outputs.get(id);
  }


  /**
   * 
   * @param outputIndexes indexes of columns to output
   */
  public void setOutputs(HashMap<Integer, ArrayList<Integer>> outputIndexes)
  {
    this.outputs = outputIndexes;
  }


  public OutputMode getOutputMode()
  {
    return outputMode.get(1);
  }


  public OutputMode getOutputMode(int id)
  {
    return outputMode.get(id);
  }


  public ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors()
  {
    return this.getDataAccessConnectionDescriptors();
  }


  public ArrayList<PropertyDescriptor> getInterface()
  {

    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("access", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("parameters", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("output", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("columns", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    return properties;
  }


  public abstract ConnectionType getConnectionType();


  public Connection[] getAvailableConnections()
  {
    return ConnectionCatalog.getInstance(false).getConnectionsByType(getConnectionType());
  }


  public Connection[] getAvailableConnections(boolean skipCache)
  {
    return ConnectionCatalog.getInstance(skipCache).getConnectionsByType(getConnectionType());
  }


  public String getTypeForFile()
  {
    return this.getClass().toString().toLowerCase().replaceAll("class pt.webdetails.cda.dataaccess.(.*)connection", "$1");
  }


  /**
   * Identify $FOREACH directives and get their iterators
   */
  private Map<String, Iterable<String>> getIterableParametersValues(
          final QueryOptions queryOptions) throws QueryException
  {

    //name, values
    Map<String, Iterable<String>> iterableParameters = new HashMap<String, Iterable<String>>();
    final String splitRegex = PARAM_ITERATOR_ARG_SEPARATOR + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";//ignore separator inside dquotes

    for (Parameter param : queryOptions.getParameters())
    {
      String value = param.getStringValue();
      if (value != null && value.startsWith(PARAM_ITERATOR_BEGIN))
      {
        String[] args = Util.getContentsBetween(value, PARAM_ITERATOR_BEGIN, PARAM_ITERATOR_END).split(splitRegex);

        if (args.length < 2)
        {
          throw new QueryException("Parameter '" + param.getName() + "': "
                  + "Error iterating parameter.", new IllegalArgumentException("$FOREACH: need at least dataAccessId and column index"));
        }

        //dataAccessId
        String dataAccessId = StringUtils.trim(args[0]);
        try
        {//validate
          getCdaSettings().getDataAccess(dataAccessId);
        }
        catch (UnknownDataAccessException e)
        {
          throw new QueryException("$FOREACH: Invalid dataAccessId.", e);
        }

        //column index
        int columnIdx = 0;
        try
        {
          columnIdx = Integer.parseInt(StringUtils.trim(args[1]));
        }
        catch (NumberFormatException nfe)
        {
          throw new QueryException("$FOREACH: Unable to parse 2nd argument.", nfe);
        }

        //parameters for query
        String[] dataAccessParams = null;
        if (args.length > 2)
        {
          dataAccessParams = new String[args.length - 2];
          System.arraycopy(args, 2, dataAccessParams, 0, dataAccessParams.length);
        }

        Iterable<String> paramValues = expandParameterIteration(dataAccessId, columnIdx, dataAccessParams);

        if (paramValues == null)
        {//no values, clear so it can fallback to default (if any)
          param.setValue(null);
        }
        else
        {
          iterableParameters.put(param.getName(), paramValues);
        }
      }
    }
    return iterableParameters;
  }


  /**
   * Get a value iterator from a $FOREACH directive 
   * @return Iterable over values, or null if no results
   */
  private Iterable<String> expandParameterIteration(String dataAccessId, int outColumnIdx, String[] dataAccessParameters)
          throws QueryException
  {
    final String EXC_TEXT = "Unable to expand parameter iteration. ";

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId(dataAccessId);

    //set query parameters
    if (dataAccessParameters != null)
    {
      for (String paramDef : dataAccessParameters)
      {
        int attribIdx = StringUtils.indexOf(paramDef, '=');
        if(attribIdx > 0){
          String paramName = StringUtils.trim(StringUtils.substring(paramDef, 0, attribIdx));
          String paramValue = StringUtils.trim(StringUtils.substring(paramDef, attribIdx+1));
          queryOptions.addParameter(paramName, paramValue);
        }
        else {
          logger.error("Bad parameter definition, skipping: " + paramDef);
        }
      }
    }

    //do query and get selected columns
    logger.debug("expandParameterIteration: Doing inner query on CdaSettings [ " + cdaSettings.getId()
            + " (" + queryOptions.getDataAccessId() + ")]");
    try
    {

      DataAccess dataAccess = getCdaSettings().getDataAccess(queryOptions.getDataAccessId());
      TableModel tableModel = dataAccess.doQuery(queryOptions);

      if (outColumnIdx < 0 || outColumnIdx >= tableModel.getColumnCount())
      {
        throw new QueryException(EXC_TEXT, new IllegalArgumentException("Output column index " + outColumnIdx + " out of range."));
      }

      if (tableModel.getRowCount() < 1)
      {
        return null;
      }
      return new StringColumnIterable(tableModel, outColumnIdx);

    }
    catch (UnknownDataAccessException e)
    {
      throw new QueryException(EXC_TEXT, e);
    }

  }


  private TableModel doQueryOnIterableParameters(QueryOptions queryOptions, Map<String, Iterable<String>> iterableParameters)
          throws QueryException
  {
    //all iterators need to have at least one value..
    List<String> names = new ArrayList<String>();
    List<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
    List<Iterable<String>> iterables = new ArrayList<Iterable<String>>();
    List<String> values = new ArrayList<String>();
    TableModel result = null;

    try
    {
      //0) init
      int paramCount = 0;
      for (String name : iterableParameters.keySet())
      {
        names.add(name);
        iterables.add(iterableParameters.get(name));
        iterators.add(iterables.get(paramCount).iterator());
        values.add(iterators.get(paramCount).next());
        paramCount++;
      }
      boolean exhausted = false;

      while (!exhausted)
      {// til last iteration from bottom of stack
        //1.1) set parameters
        for (int i = 0; i < paramCount; i++)
        {
          queryOptions.setParameter(names.get(i), values.get(i));
        }
        //1.2) execute query
        TableModel tableModel = doQuery(queryOptions);
//				//1.3) cache only, just keep last result //join results x
//				if (result == null) { result = tableModel; }
//				else { result = TableModelUtils.getInstance().appendTableModel(result,tableModel); }
        result = tableModel;
        //2) get next set of values
        for (int i = 0; i < paramCount; i++)
        {// traverse until we can get a next() or bottom of stack reached
          if (iterators.get(i).hasNext())
          {
            values.set(i, iterators.get(i).next());//new value
            break;
          }
          else if (i < paramCount - 1)
          {//this one exhausted, reset if not last
            iterators.set(i, iterables.get(i).iterator());//reset
            values.set(i, iterators.get(i).next());
          }
          else
          {
            exhausted = true;//end of the line, no more resets
          }
        }
      }
    }
    catch (NoSuchElementException e)
    {
      //will happen if one of the iterators has no value
    }

    return result;
  }

  /**
   * Iterates a table model over a given column index.
   */
  private class StringColumnIterable implements Iterable<String>
  {

    private final TableModel table;
    private final int columnIndex;


    public StringColumnIterable(TableModel tableModel, int columnIndex)
    {
      this.table = tableModel;
      this.columnIndex = columnIndex;
    }


    @Override
    public Iterator<String> iterator()
    {
      return new StringColumnIterator();
    }

    private class StringColumnIterator implements Iterator<String>
    {

      int rowIndex = 0;


      @Override
      public boolean hasNext()
      {
        return table.getRowCount() > rowIndex;
      }


      @Override
      public String next()
      {
        if (!hasNext())
        {
          throw new NoSuchElementException();
        }
        return table.getValueAt(rowIndex++, columnIndex).toString();
      }


      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    }
  }
}
