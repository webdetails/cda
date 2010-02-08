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
public abstract class AbstractDataAccess implements DataAccess
{
  private static final Log logger = LogFactory.getLog(AbstractDataAccess.class);

  private static CacheManager cacheManager;

  private CdaSettings cdaSettings;
  private String id;
  private DataAccessEnums.ACCESS_TYPE access = DataAccessEnums.ACCESS_TYPE.PUBLIC;
  private boolean cache = false;
  private int cacheDuration = 3600;
  private ArrayList<Parameter> parameters;
  private ArrayList<Integer> outputs;
  private ArrayList<ColumnDefinition> columnDefinitions;
  private HashMap<Integer, ColumnDefinition> columnDefinitionIndexMap;

  protected AbstractDataAccess(final Element element)
  {

    columnDefinitionIndexMap = new HashMap<Integer, ColumnDefinition>();
    columnDefinitions = new ArrayList<ColumnDefinition>();
    outputs = new ArrayList<Integer>();
    parameters = new ArrayList<Parameter>();

    parseOptions(element);

  }


  private void parseOptions(final Element element)
  {
    id = element.attributeValue("id");

    if (element.attributeValue("access") != null && element.attributeValue("access").equals("private"))
    {
      access = DataAccessEnums.ACCESS_TYPE.PRIVATE;
    }

    if (element.attributeValue("cache") != null && element.attributeValue("cache").equals("true"))
    {
      cache = true;
    }

    if (element.attribute("cacheDuration") != null)
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
    final Element outputNode = (Element) element.selectSingleNode("Output");
    if (outputNode != null)
    {
      final String[] indexes = outputNode.attributeValue("indexes").split(",");
      for (final String index : indexes)
      {
        outputs.add(Integer.parseInt(index));
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

  protected static synchronized Cache getCache() throws CacheException
  {
    if (cacheManager == null)
    {
      cacheManager = CacheManager.create();
    }

    if (cacheManager.cacheExists("pentaho-cda-dataaccess") == false)
    {
      cacheManager.addCache("pentaho-cda-dataaccess");
    }
    return cacheManager.getCache("pentaho-cda-dataaccess");
  }

  @Override
  public TableModel doQuery(final QueryOptions queryOptions) throws QueryException
  {

    // Get parameters from definition and apply it's values
    final ArrayList<Parameter> parameters = (ArrayList<Parameter>) getParameters().clone();

    for (final Parameter parameter : parameters)
    {
      final Parameter parameterPassed = queryOptions.getParameter(parameter.getName());
      if (parameterPassed != null)
      {
        parameter.setStringValue(parameterPassed.getStringValue());
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


    /*
    *  Do the tableModel PostProcessing
    *  1. Sort (todo)
    *  2. Show only the output columns
    *  3. Paginate
    *  4. Call the appropriate exporter
    *
    */

    try
    {
      final TableModel tableModel = queryDataSource(parameterDataRow);
      final TableModel outputTableModel = TableModelUtils.getInstance().postProcessTableModel(this,queryOptions,tableModel);
      logger.debug("Query " + getId() + " done successfully - returning tableModel");
      return outputTableModel;
    }
    catch (TableModelException e)
    {
      throw new QueryException("Could not create outputTableModel ", e);
    }



  }


  private ParameterDataRow createParameterDataRowFromParameters(final ArrayList<Parameter> parameters) throws InvalidParameterException
  {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    for (final Parameter parameter : parameters)
    {
      names.add(parameter.getName());
      values.add(parameter.getValue());
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow(names.toArray(new String[]{}), values.toArray());

    return parameterDataRow;

  }

  protected abstract TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException;

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


  @Override
  public String getId()
  {
    return id;
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
    return outputs;
  }
}
