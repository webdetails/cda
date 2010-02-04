package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.settings.CdaSettings;
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
  private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
  private ArrayList<Integer> outputs = new ArrayList<Integer>();
  private ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();


  protected AbstractDataAccess(final Element element)
  {

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
  public TableModel doQuery() throws QueryException
  {

    final ParameterDataRow parameterDataRow = new ParameterDataRow();
    final boolean isCached = false;

    TableModel tableModel = queryDataSource(parameterDataRow);

    // Handle the TableModel

    final TableModelUtils tableModelUtils = TableModelUtils.getInstance();
    TableModel newTableModel = tableModelUtils.transformTableModel(this, tableModel);

    // Close it - the data-access implementation takes care of whether a real close is needed.
    closeDataSource();

    logger.debug("Query " + getId() + " done successfully - returning tableModel");
    return newTableModel;

  }

  protected abstract TableModel queryDataSource(ParameterDataRow parameterDataRow) throws QueryException;

  protected abstract void closeDataSource() throws QueryException;

  public ArrayList<ColumnDefinition> getColumns()
  {

    ArrayList<ColumnDefinition> list = (ArrayList<ColumnDefinition>) columnDefinitions.clone();

    for (ColumnDefinition definition : list)
    {
      if (definition.getType() != ColumnDefinition.TYPE.COLUMN)
      {
        list.remove(definition);
      }
    }
    return list;

  }

  public ArrayList<ColumnDefinition> getCalculatedColumns()
  {
    ArrayList<ColumnDefinition> list = (ArrayList<ColumnDefinition>) columnDefinitions.clone();

    for (ColumnDefinition definition : list)
    {
      if (definition.getType() != ColumnDefinition.TYPE.CALCULATED_COLUMN)
      {
        list.remove(definition);
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
