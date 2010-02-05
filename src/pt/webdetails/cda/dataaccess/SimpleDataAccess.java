package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * Implementation of the SimpleDataAccess
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:04:10 AM
 */
public abstract class SimpleDataAccess extends AbstractDataAccess
{

  private static class TableCacheKey
  {
    private String query;
    private ParameterDataRow parameterDataRow;

    private TableCacheKey(final String query, final ParameterDataRow parameterDataRow)
    {
      if (query == null)
      {
        throw new NullPointerException();
      }
      if (parameterDataRow == null)
      {
        throw new NullPointerException();
      }

      this.query = query;
      this.parameterDataRow = parameterDataRow;
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

      if (!parameterDataRow.equals(that.parameterDataRow))
      {
        return false;
      }
      return query.equals(that.query);

    }

    public int hashCode()
    {
      int result = query.hashCode();
      result = 31 * result + parameterDataRow.hashCode();
      return result;
    }
  }

  private static final Log logger = LogFactory.getLog(SimpleDataAccess.class);

  private String connectionId;
  private String query;

  public SimpleDataAccess(final Element element)
  {

    super(element);
    connectionId = element.attributeValue("connection");
    query = element.selectSingleNode("./Query").getText();

  }


  protected TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException
  {

    final ParameterDataRow parameterDataRow = new ParameterDataRow();
    final Cache cache = getCache();

    // create the cache-key which is both query and parameter values
    final TableCacheKey key = new TableCacheKey(getQuery(), parameterDataRow);

    if (isCache())
    {
      final net.sf.ehcache.Element element = cache.get(key);
      if (element != null)
      {
        final TableModel cachedTableModel = (TableModel) element.getObjectValue();
        if (cachedTableModel != null)
        {
          // we have a entry in the cache ... great!
          return cachedTableModel;
        }
      }
    }

    final TableModel tableModel = performRawQuery(parameterDataRow);

    // Copy the tableModel and cache it
    // Handle the TableModel

    final TableModel tableModelCopy = TableModelUtils.getInstance().transformTableModel(this, tableModel);

    closeDataSource();

    // put the copy into the cache ...
    if (isCache())
    {

      final net.sf.ehcache.Element storeElement = new net.sf.ehcache.Element(key, tableModelCopy);
      cache.put(storeElement);
    }

    // and finally return the copy.
    return tableModelCopy;
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

}
