package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.DataAccess;
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
    //  1. Show only the output columns we want;
    //  2. return the correct pagination


    // First we need to check if there's nothing to do.
    final ArrayList<Integer> outputIndexes = dataAccess.getOutputs();
    if (queryOptions.isPaginate() == false && outputIndexes.isEmpty() && queryOptions.getSortBy().isEmpty())
    {
      // No, the original one is good enough
      return rawTableModel;
    }

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

    // We're removing the ::table-index:: cols
    final int count = t.getColumnCount()/2;

    final ArrayList<ColumnDefinition> calculatedColumnsList = dataAccess.getCalculatedColumns();

    if (!calculatedColumnsList.isEmpty())
    {
      logger.warn("Todo: Implement " + calculatedColumnsList.size() + " Calculated Columns");
    }

    final Class[] colTypes = new Class[count];
    final String[] colNames = new String[count];

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


  public static synchronized TableModelUtils getInstance()
  {

    if (_instance == null)
    {
      _instance = new TableModelUtils();
    }

    return _instance;
  }

}
