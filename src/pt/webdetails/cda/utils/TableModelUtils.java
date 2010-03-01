package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.DataAccessEnums;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.query.QueryOptions;

/**
 * Utility class to handle TableModel operations
 * <p/>
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 12:31:54 PM
 */
public class TableModelUtils
{

  private static final Log logger = LogFactory.getLog(TableModelUtils.class);
  private static TableModelUtils _instance;

  public TableModelUtils()
  {
  }

  public TableModel postProcessTableModel(final DataAccess dataAccess,
                                          final QueryOptions queryOptions,
                                          final TableModel rawTableModel) throws TableModelException
  {

    // We will:
    //  1. Evaluate Calculated columns
    //  2. Show only the output columns we want;
    //  3. return the correct pagination

    final TableModel t;
    final ArrayList<ColumnDefinition> columnDefinitions = dataAccess.getCalculatedColumns();
    if (columnDefinitions.isEmpty())
    {
      t = rawTableModel;
    }
    else
    {
      t = new CalculatedTableModel(rawTableModel, columnDefinitions.toArray(new ColumnDefinition[columnDefinitions.size()]));
    }

    // First we need to check if there's nothing to do.
    final ArrayList<Integer> outputIndexes = dataAccess.getOutputs();
    if (queryOptions.isPaginate() == false && outputIndexes.isEmpty() && queryOptions.getSortBy().isEmpty())
    {
      // No, the original one is good enough
      return t;
    }

    final int columnCount = outputIndexes.size();

    if (Collections.max(outputIndexes) > t.getColumnCount() - 1)
    {
      throw new TableModelException("Output index higher than number of columns in tableModel", null);

    }

    final Class[] colTypes = new Class[columnCount];
    final String[] colNames = new String[columnCount];

    for (int i = 0; i < outputIndexes.size(); i++)
    {
      final int outputIndex = outputIndexes.get(i);
      colTypes[i] = t.getColumnClass(outputIndex);
      colNames[i] = t.getColumnName(outputIndex);
    }

    final int rowCount;
    if (queryOptions.isPaginate())
    {
      rowCount = Math.min(queryOptions.getPageSize(), t.getRowCount() - queryOptions.getPageStart());
    }
    else
    {
      rowCount = t.getRowCount();
    }

    logger.debug(rowCount == 0 ? "No data found" : "Found " + rowCount + " rows");


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);
    for (int r = 0; r < rowCount; r++)
    {
      for (int j = 0; j < outputIndexes.size(); j++)
      {
        final int outputIndex = outputIndexes.get(j);
        typedTableModel.setValueAt(t.getValueAt(r + queryOptions.getPageStart(), outputIndex), r, j);
      }
    }
    return typedTableModel;

  }

  public TableModel copyTableModel(final DataAccess dataAccess, final TableModel t)
  {

    // We're removing the ::table-by-index:: cols


    // Build an array of column indexes whose name is different from ::table-by-index::.*
    ArrayList<String> namedColumns = new ArrayList<String>();
    ArrayList<Class> namedColumnsClasses = new ArrayList<Class>();
    for (int i = 0; i < t.getColumnCount(); i++)
    {
      String colName = t.getColumnName(i);
      if (!colName.startsWith("::table-by-index::"))
      {
        namedColumns.add(colName);
        namedColumnsClasses.add(t.getColumnClass(i));
      }
    }

    final int count = namedColumns.size();
    final ArrayList<ColumnDefinition> calculatedColumnsList = dataAccess.getCalculatedColumns();


    final Class[] colTypes = namedColumnsClasses.toArray(new Class[]{});
    final String[] colNames = namedColumns.toArray(new String[]{});

    for (int i = 0; i < count; i++)
    {
      colTypes[i] = t.getColumnClass(i);

      final ColumnDefinition col = dataAccess.getColumnDefinition(i);
      colNames[i] = col != null ? col.getName() : t.getColumnName(i);
    }
    final int rowCount = t.getRowCount();
    logger.debug(rowCount == 0 ? "No data found" : "Found " + rowCount + " rows");


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);
    for (int r = 0; r < rowCount; r++)
    {
      for (int c = 0; c < count; c++)
      {
        typedTableModel.setValueAt(t.getValueAt(r, c), r, c);
      }
    }
    return typedTableModel;
  }


  public TableModel dataAccessMapToTableModel(HashMap<String, DataAccess> dataAccessMap)
  {


    int rowCount = dataAccessMap.size();

    // Define names and types
    final String[] colNames = {"id", "name", "type"};
    final Class[] colTypes = {String.class, String.class, String.class};


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (DataAccess dataAccess : dataAccessMap.values())
    {
      if (dataAccess.getAccess() == DataAccessEnums.ACCESS_TYPE.PUBLIC)
      {
        typedTableModel.addRow(new Object[]{dataAccess.getId(), dataAccess.getName(), dataAccess.getType()});
      }
    }

    return typedTableModel;

  }


  public static synchronized TableModelUtils getInstance()
  {

    if (_instance == null)
    {
      _instance = new TableModelUtils();
    }

    return _instance;
  }

  public TableModel dataAccessParametersToTableModel(final ArrayList<Parameter> parameters)
  {

    int rowCount = parameters.size();

    // Define names and types
    final String[] colNames = {"name", "type", "defaultValue", "pattern"};
    final Class[] colTypes = {String.class, String.class, String.class, String.class};


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (Parameter p : parameters)
    {
      typedTableModel.addRow(new Object[]{p.getName(), p.getType(), p.getDefaultValue(), p.getPattern()});
    }


    return typedTableModel;


  }
}
