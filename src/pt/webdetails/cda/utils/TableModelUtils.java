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
                                          QueryOptions queryOptions,
                                          final TableModel t) throws TableModelException
  {

    // We will:
    //  1. Show only the output columns we want;
    //  2. return the correct pagination


    // First we need to check if there's nothing to do.
    if (queryOptions.isPaginate() == false && dataAccess.getOutputs().size() == 0 && queryOptions.getSortBy().size() == 0)
    {
      // No, the original one is good enough
      return t;
    }


    ArrayList<Integer> outputIndexes = dataAccess.getOutputs();

    Collections.max(outputIndexes);

    final int count = outputIndexes.size();

    if (Collections.max(outputIndexes) > t.getColumnCount() - 1)
    {
      throw new TableModelException("Output index higher than number of columns in tableModel", null);

    }

    final Class[] colTypes = new Class[count];
    final String[] colNames = new String[count];

    int i = 0;
    for (Integer outputIndex : outputIndexes)
    {
      colTypes[i] = t.getColumnClass(outputIndex.intValue());
      colNames[i] = t.getColumnName(outputIndex.intValue());
      i++;
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
      int j = 0;
      for (Integer outputIndex : outputIndexes)
      {

        j++;
        typedTableModel.setValueAt(t.getValueAt(r + queryOptions.getPageStart(), outputIndex.intValue()), r, j);
      }
    }
    return typedTableModel;

  }


  public TableModel copyTableModel(final DataAccess dataAccess, final TableModel t)
  {

    final int count = t.getColumnCount();

    ArrayList<ColumnDefinition> calculatedColumnsList = dataAccess.getCalculatedColumns();

    if (calculatedColumnsList.size() > 0)
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
