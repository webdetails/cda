package pt.webdetails.cda.utils;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.DataAccess;

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


  public TableModel transformTableModel(final DataAccess dataAccess
                                        /*, QueryOptions queryOptions*/,
                                        final TableModel tableModel)
  {


    logger.warn("transformTableModel Not implemented yet");

    return copyTableModel(dataAccess, tableModel);

  }


  public TableModel copyTableModel(final DataAccess dataAccess, final TableModel t)
  {

    final int count =  t.getColumnCount();

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
