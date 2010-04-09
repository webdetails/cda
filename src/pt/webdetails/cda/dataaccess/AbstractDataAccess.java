package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.TableModelException;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * This is the top level implementation of a DataAccess. Only the common methods are used here
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:05:38 AM
 */
public abstract class AbstractDataAccess implements DataAccess {

  private static final Log logger = LogFactory.getLog(AbstractDataAccess.class);
  private static CacheManager cacheManager;
  private CdaSettings cdaSettings;
  private String id;
  private String name;
  private DataAccessEnums.ACCESS_TYPE access = DataAccessEnums.ACCESS_TYPE.PUBLIC;
  private boolean cache = false;
  private int cacheDuration = 3600;
  private ArrayList<Parameter> parameters;
  private ArrayList<Integer> outputs;
  private ArrayList<ColumnDefinition> columnDefinitions;
  private HashMap<Integer, ColumnDefinition> columnDefinitionIndexMap;
  private static final ConnectionType connectionType = null;

  protected AbstractDataAccess() {}
  
  protected AbstractDataAccess(final Element element) {
    name = "";
    columnDefinitionIndexMap = new HashMap<Integer, ColumnDefinition>();
    columnDefinitions = new ArrayList<ColumnDefinition>();
    outputs = new ArrayList<Integer>();
    parameters = new ArrayList<Parameter>();

    parseOptions(element);

  }

  public abstract String getType();

  private void parseOptions(final Element element) {
    id = element.attributeValue("id");

    final Element nameElement = (Element) element.selectSingleNode("./Name");
    if (nameElement != null) {
      name = nameElement.getTextTrim();
    }

    if (element.attributeValue("access") != null && element.attributeValue("access").equals("private")) {
      access = DataAccessEnums.ACCESS_TYPE.PRIVATE;
    }

    if (element.attributeValue("cache") != null && element.attributeValue("cache").equals("true")) {
      cache = true;
    }

    if (element.attribute("cacheDuration") != null) {
      cacheDuration = Integer.parseInt(element.attributeValue("cacheDuration"));
    }


    // Parse parameters
    final List<Element> parameterNodes = element.selectNodes("Parameters/Parameter");

    for (final Element p : parameterNodes) {
      parameters.add(new Parameter(p));
    }

    // Parse outputs
    final Element outputNode = (Element) element.selectSingleNode("Output");
    if (outputNode != null) {
      final String[] indexes = outputNode.attributeValue("indexes").split(",");
      for (final String index : indexes) {
        outputs.add(Integer.parseInt(index));
      }
    }

    // Parse Columns
    final List<Element> columnNodes = element.selectNodes("Columns/*");

    for (final Element p : columnNodes) {
      columnDefinitions.add(new ColumnDefinition(p));
    }

    // Build the columnDefinitionIndexMap
    final ArrayList<ColumnDefinition> cols = getColumns();
    for (final ColumnDefinition columnDefinition : cols) {
      columnDefinitionIndexMap.put(columnDefinition.getIndex(), columnDefinition);
    }

  }

  protected static synchronized Cache getCache() throws CacheException {
    if (cacheManager == null) {
      cacheManager = CacheManager.create();
    }

    if (cacheManager.cacheExists("pentaho-cda-dataaccess") == false) {
      cacheManager.addCache("pentaho-cda-dataaccess");
    }
    return cacheManager.getCache("pentaho-cda-dataaccess");
  }

  public static synchronized void clearCache() throws CacheException {
    if (cacheManager != null && cacheManager.cacheExists("pentaho-cda-dataaccess")) {
      cacheManager.removeCache("pentaho-cda-dataaccess");
    }
  }

  public TableModel doQuery(final QueryOptions queryOptions) throws QueryException {


    /*
     *  Do the tableModel PostProcessing
     *  1. Sort (todo)
     *  2. Show only the output columns
     *  3. Paginate
     *  4. Call the appropriate exporter
     *
     */

    try {
      final TableModel tableModel = queryDataSource(queryOptions);
      final TableModel outputTableModel = TableModelUtils.getInstance().postProcessTableModel(this, queryOptions, tableModel);
      logger.debug("Query " + getId() + " done successfully - returning tableModel");
      return outputTableModel;
    } catch (TableModelException e) {
      throw new QueryException("Could not create outputTableModel ", e);
    }


  }

  public TableModel listParameters(final DiscoveryOptions discoveryOptions) {

    return TableModelUtils.getInstance().dataAccessParametersToTableModel(getParameters());

  }

  protected abstract TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException;

  public abstract void closeDataSource() throws QueryException;

  public ArrayList<ColumnDefinition> getColumns() {

    final ArrayList<ColumnDefinition> list = new ArrayList<ColumnDefinition>();

    for (final ColumnDefinition definition : columnDefinitions) {
      if (definition.getType() == ColumnDefinition.TYPE.COLUMN) {
        list.add(definition);
      }
    }
    return list;

  }

  public ColumnDefinition getColumnDefinition(final int idx) {

    return columnDefinitionIndexMap.get(new Integer(idx));

  }

  public ArrayList<ColumnDefinition> getCalculatedColumns() {

    final ArrayList<ColumnDefinition> list = new ArrayList<ColumnDefinition>();

    for (final ColumnDefinition definition : columnDefinitions) {
      if (definition.getType() == ColumnDefinition.TYPE.CALCULATED_COLUMN) {
        list.add(definition);
      }
    }
    return list;

  }

  public void storeDescriptor(DataAccessConnectionDescriptor descriptor) {
    ////
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public DataAccessEnums.ACCESS_TYPE getAccess() {
    return access;
  }

  public boolean isCache() {
    return cache;
  }

  public int getCacheDuration() {
    return cacheDuration;
  }

  public CdaSettings getCdaSettings() {
    return cdaSettings;
  }

  public void setCdaSettings(final CdaSettings cdaSettings) {
    this.cdaSettings = cdaSettings;
  }

  public ArrayList<Parameter> getParameters() {
    return parameters;
  }

  public ArrayList<Integer> getOutputs() {
    return outputs;
  }

  public ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors() {
    return this.getDataAccessConnectionDescriptors();
  }


  public ArrayList<PropertyDescriptor> getInterface() {
    
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING));
    properties.add(new PropertyDescriptor("access", PropertyDescriptor.Type.STRING));
    properties.add(new PropertyDescriptor("parameters", PropertyDescriptor.Type.ARRAY));
    properties.add(new PropertyDescriptor("output", PropertyDescriptor.Type.ARRAY));
    properties.add(new PropertyDescriptor("columns", PropertyDescriptor.Type.ARRAY));
    return properties;
  }

  public abstract ConnectionType getConnectionType();

  public Connection[] getAvailableConnections() {
    return ConnectionCatalog.getInstance(false).getConnectionsByType(getConnectionType());
  }

  public Connection[] getAvailableConnections(boolean skipCache) {
    return ConnectionCatalog.getInstance(skipCache).getConnectionsByType(getConnectionType());
  }
}
