package pt.webdetails.cda.utils;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.settings.CdaSettings;

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


  public TableModel transformTableModel(final DataAccess dataAccess /*, QueryOptions queryOptions*/ , final TableModel tableModel){


    logger.warn("transformTableModel Not implemented yet"); 

    return copyTableModel(tableModel);

  }


  private TableModel copyTableModel(final TableModel t)
  {
    final int count = t.getColumnCount();
    final Class[] colTypes = new Class[count];
    final String[] colNames = new String[count];
    for (int i = 0; i < count; i++)
    {
      colTypes[i] = t.getColumnClass(i);
      colNames[i] = t.getColumnName(i);
    }
    final int rowCount = t.getRowCount();
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
