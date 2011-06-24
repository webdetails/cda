package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.Arrays;
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
import pt.webdetails.cda.utils.kettle.SortException;
import pt.webdetails.cda.utils.kettle.SortTableModel;

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
          final TableModel rawTableModel) throws TableModelException, SortException
  {

    // We will:
    //  1. Evaluate Calculated columns
    //  2. Show only the output columns we want;
    //  3. Sort
    //  3. Pagination

    // 1
    TableModel t;
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
    ArrayList<Integer> outputIndexes = dataAccess.getOutputs();
    /*
    if (queryOptions.isPaginate() == false && outputIndexes.isEmpty() && queryOptions.getSortBy().isEmpty())
    {
    // No, the original one is good enough
    return t;
    }
     */
    // 2
    // If output mode == exclude, we need to translate the excluded outputColuns
    // into included ones
    if (dataAccess.getOutputMode() == DataAccess.OutputMode.EXCLUDE && outputIndexes.size() > 0)
    {

      ArrayList<Integer> newOutputIndexes = new ArrayList<Integer>();
      for (int i = 0; i < t.getColumnCount(); i++)
      {
        if (!outputIndexes.contains(i))
        {
          newOutputIndexes.add(i);
        }
      }
      outputIndexes = newOutputIndexes;
    }


    final int columnCount = outputIndexes.size();
    if (columnCount != 0)
    {

      if ((Collections.max(outputIndexes) > t.getColumnCount() - 1))
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

      final int rowCount = t.getRowCount();
      logger.debug(rowCount == 0 ? "No data found" : "Found " + rowCount + " rows");


      final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);
      for (int r = 0; r < rowCount; r++)
      {
        for (int j = 0; j < outputIndexes.size(); j++)
        {
          final int outputIndex = outputIndexes.get(j);
          typedTableModel.setValueAt(t.getValueAt(r, outputIndex), r, j);
        }
      }
      t = typedTableModel;

    }

    // Now, handle sorting

    if (!queryOptions.getSortBy().isEmpty())
    {
      // no action
      t = (new SortTableModel()).doSort(t, queryOptions.getSortBy());
    }


    // Create a metadata-aware table model

    final Class[] colTypes = new Class[t.getColumnCount()];
    final String[] colNames = new String[t.getColumnCount()];

    for (int i = 0; i < t.getColumnCount(); i++)
    {
      colTypes[i] = t.getColumnClass(i);
      colNames[i] = t.getColumnName(i);
    }

    final int rowCount = t.getRowCount();
    MetadataTableModel result = new MetadataTableModel(colNames, colTypes, rowCount);
    result.setMetadata("totalRows", rowCount);
    for (int r = 0; r < rowCount; r++)
    {
      for (int j = 0; j < t.getColumnCount(); j++)
      {
        result.setValueAt(t.getValueAt(r, j), r, j);
      }
    }
    // Paginate
    return paginateTableModel(result, queryOptions);


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
      if (!colName.startsWith("::table-by-index::")
              && !colName.startsWith("::column::"))
      {
        namedColumns.add(colName);
        namedColumnsClasses.add(t.getColumnClass(i));
      }
    }

    final int count = namedColumns.size();
    final ArrayList<ColumnDefinition> calculatedColumnsList = dataAccess.getCalculatedColumns();


    final Class[] colTypes = namedColumnsClasses.toArray(new Class[]
            {
            });
    final String[] colNames = namedColumns.toArray(new String[]
            {
            });

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
    final String[] colNames =
    {
      "id", "name", "type"
    };
    final Class[] colTypes =
    {
      String.class, String.class, String.class
    };


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (DataAccess dataAccess : dataAccessMap.values())
    {
      if (dataAccess.getAccess() == DataAccessEnums.ACCESS_TYPE.PUBLIC)
      {
        typedTableModel.addRow(new Object[]
                {
                  dataAccess.getId(), dataAccess.getName(), dataAccess.getType()
                });
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
    final String[] colNames =
    {
      "name", "type", "defaultValue", "pattern", "access"
    };
    final Class[] colTypes =
    {
      String.class, String.class, String.class, String.class, String.class
    };


    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (Parameter p : parameters)
    {
      typedTableModel.addRow(new Object[]
              {
                p.getName(), p.getTypeAsString(), p.getDefaultValue(), p.getPattern(), p.getAccess().toString()
              });
    }


    return typedTableModel;


  }


  /**
   * Method to append a tablemodel into another. We'll make no guarantees about the types
   *
   * @param tableModelA TableModel to be modified
   * @param tableModelB Contents to be appended
   * #
   */
  public TableModel appendTableModel(final TableModel tableModelA, final TableModel tableModelB)
  {

    // We will believe the data is correct - no type checking

    int colCountA = tableModelA.getColumnCount(),
            colCountB = tableModelB.getColumnCount();
    boolean usingA = colCountA > colCountB;
    int colCount = usingA ? colCountA : colCountB;
    TableModel referenceTable = (usingA? tableModelA : tableModelB);
    
    final Class[] colTypes = new Class[colCount];
    final String[] colNames = new String[colCount];

    for (int i = 0; i < referenceTable.getColumnCount(); i++)
    {
      colTypes[i] = referenceTable.getColumnClass(i);
      colNames[i] = referenceTable.getColumnName(i);
    }

    int rowCount = tableModelA.getRowCount() + tableModelB.getRowCount();


    // Table A
    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);
    for (int r = 0; r < tableModelA.getRowCount(); r++)
    {
      for (int c = 0; c < colTypes.length; c++)
      {
        typedTableModel.setValueAt(tableModelA.getValueAt(r, c), r, c);
      }
    }

    // Table B
    int rowCountOffset = tableModelA.getRowCount();
    for (int r = 0; r < tableModelB.getRowCount(); r++)
    {
      for (int c = 0; c < colTypes.length; c++)
      {
        typedTableModel.setValueAt(tableModelB.getValueAt(r, c), r + rowCountOffset, c);
      }
    }


    return typedTableModel;

  }


  private TableModel paginateTableModel(MetadataTableModel t, QueryOptions queryOptions)
  {

    if (!queryOptions.isPaginate() || (queryOptions.getPageSize() == 0 && queryOptions.getPageStart() == 0))
    {
      return t;
    }


    final int rowCount = Math.min(queryOptions.getPageSize(), t.getRowCount() - queryOptions.getPageStart());
    logger.debug("Paginating " + queryOptions.getPageSize() + " pages from page " + queryOptions.getPageStart());


    final Class[] colTypes = new Class[t.getColumnCount()];
    final String[] colNames = new String[t.getColumnCount()];

    for (int i = 0; i < t.getColumnCount(); i++)
    {
      colTypes[i] = t.getColumnClass(i);
      colNames[i] = t.getColumnName(i);
    }

    final MetadataTableModel resultTableModel = new MetadataTableModel(colNames, colTypes, rowCount, t.getAllMetadata());
    resultTableModel.setMetadata("pageSize", queryOptions.getPageSize());
    resultTableModel.setMetadata("pageStart", queryOptions.getPageStart());

    for (int r = 0; r < rowCount; r++)
    {
      for (int j = 0; j < t.getColumnCount(); j++)
      {
        resultTableModel.setValueAt(t.getValueAt(r + queryOptions.getPageStart(), j), r, j);
      }
    }

    return resultTableModel;


  }
}
