package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.TableModel;

import net.sf.ehcache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * Implementation of the SimpleDataAccess
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:04:10 AM
 */
public abstract class SimpleDataAccess extends AbstractDataAccess {

  private static class TableCacheKey {

    private Connection connection;
    private String query;
    private ParameterDataRow parameterDataRow;

    private TableCacheKey(final Connection connection, final String query, final ParameterDataRow parameterDataRow) {
      if (connection == null) {
        throw new NullPointerException();
      }
      if (query == null) {
        throw new NullPointerException();
      }
      if (parameterDataRow == null) {
        throw new NullPointerException();
      }

      this.connection = connection;
      this.query = query;
      this.parameterDataRow = parameterDataRow;
    }

    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final TableCacheKey that = (TableCacheKey) o;

      if (connection != null ? !connection.equals(that.connection) : that.connection != null) {
        return false;
      }
      if (parameterDataRow != null ? !parameterDataRow.equals(that.parameterDataRow) : that.parameterDataRow != null) {
        return false;
      }
      if (query != null ? !query.equals(that.query) : that.query != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = connection != null ? connection.hashCode() : 0;
      result = 31 * result + (query != null ? query.hashCode() : 0);
      result = 31 * result + (parameterDataRow != null ? parameterDataRow.hashCode() : 0);
      return result;
    }
  }
  private static final Log logger = LogFactory.getLog(SimpleDataAccess.class);
  private String connectionId;
  private String query;

  public SimpleDataAccess() {
  }

  public SimpleDataAccess(final Element element) {

    super(element);
    connectionId = element.attributeValue("connection");
    query = element.selectSingleNode("./Query").getText();

  }

  protected synchronized TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException {

    final Cache cache = getCache();

    // Get parameters from definition and apply it's values
    final ArrayList<Parameter> parameters = (ArrayList<Parameter>) getParameters().clone();

    for (final Parameter parameter : parameters) {
      final Parameter parameterPassed = queryOptions.getParameter(parameter.getName());
      if (parameterPassed != null) {
        parameter.setStringValue(parameterPassed.getStringValue());
      } else {
        parameter.setStringValue(parameter.getDefaultValue());
      }
    }


    final ParameterDataRow parameterDataRow;
    try {
      parameterDataRow = createParameterDataRowFromParameters(parameters);
    } catch (InvalidParameterException e) {
      throw new QueryException("Error parsing parameters ", e);
    }


    // create the cache-key which is both query and parameter values
    final TableCacheKey key;
    try {
      key = new TableCacheKey(getCdaSettings().getConnection(getConnectionId()), getQuery(), parameterDataRow);
    } catch (UnknownConnectionException e) {
      // I'm sure I'll never be here
      throw new QueryException("Unable to get a Connection for this dataAccess ", e);
    }

    if (isCache()) {
      final net.sf.ehcache.Element element = cache.get(key);
      if (element != null) {
        final TableModel cachedTableModel = (TableModel) element.getObjectValue();
        if (cachedTableModel != null) {
          // we have a entry in the cache ... great!
          logger.debug("Found tableModel in cache. Returning");
          return cachedTableModel;
        }
      }
    }

    final TableModel tableModel = performRawQuery(parameterDataRow);

    // Copy the tableModel and cache it
    // Handle the TableModel

    final TableModel tableModelCopy = TableModelUtils.getInstance().copyTableModel(this, tableModel);

    closeDataSource();

    // put the copy into the cache ...
    if (isCache()) {
      final net.sf.ehcache.Element storeElement = new net.sf.ehcache.Element(key, tableModelCopy);
      storeElement.setTimeToLive(getCacheDuration());
      cache.put(storeElement);
    }

    // and finally return the copy.
    return tableModelCopy;
  }

  private ParameterDataRow createParameterDataRowFromParameters(final ArrayList<Parameter> parameters) throws InvalidParameterException {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    for (final Parameter parameter : parameters) {
      names.add(parameter.getName());
      values.add(parameter.getValue());
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow(names.toArray(new String[]{}), values.toArray());

    return parameterDataRow;

  }

  protected abstract TableModel performRawQuery(ParameterDataRow parameterDataRow) throws QueryException;

  public abstract void closeDataSource() throws QueryException;

  public String getQuery() {
    return query;
  }

  public String getConnectionId() {
    return connectionId;
  }

  @Override
  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    properties.add(new PropertyDescriptor("query", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("connection", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("cache", PropertyDescriptor.Type.BOOLEAN, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("cacheDuration", PropertyDescriptor.Type.NUMERIC, PropertyDescriptor.Placement.ATTRIB));
    return properties;
  }
}
