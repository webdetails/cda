/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
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
 * User: pedro Date: Feb 4, 2010 Time: 12:31:54 PM
 */
public class TableModelUtils {

  private static final Log logger = LogFactory.getLog( TableModelUtils.class );
  private static final String DT_FILTER = "dtFilter";
  private static final String DT_SEARCHABLE = "dtSearchableColumns";

  private static TableModelUtils _instance;

  /**
   * @deprecated all methods made static
   */
  @Deprecated
  public TableModelUtils() {
  }

  /**
   * @deprecated all methods made static
   */
  @Deprecated
  public static synchronized TableModelUtils getInstance() {

    if ( _instance == null ) {
      _instance = new TableModelUtils();
    }

    return _instance;
  }


  public static TableModel postProcessTableModel( final DataAccess dataAccess,
                                                  final QueryOptions queryOptions,
                                                  final TableModel rawTableModel )
    throws SortException, InvalidOutputIndexException {

    if ( rawTableModel == null ) {
      throw new IllegalArgumentException( "Cannot process null table." );
    }

    // We will:
    //  1. Evaluate Calculated columns
    //  2. Show only the output columns we want;
    //  3. Sort
    //  4. Pagination

    TableModel table;

    // 1 Evaluate Calculated columns
    table = evaluateCalculatedColumns( dataAccess, rawTableModel );

    //  2. Show only the output columns we want, filter rows
    List<Integer> outputIndexes = getOutputIndexes( dataAccess, queryOptions, table );
    List<String> columnNames = getColumnNames( dataAccess, table );
    DataTableFilter rowFilter = getRowFilter( queryOptions, outputIndexes );
    //mdx and denormalizedMdx queries with an empty result set can return different metadata (less columns),
    //in this cases, the output indexes will be ignored
    boolean useOutputIndexes = true;
    if ( table.getRowCount() == 0 && ( dataAccess.getType().equals( "mdx" ) || dataAccess.getType()
      .equals( "denormalizedMdx" ) ) ) {
      useOutputIndexes = false;
      logger.warn( "Mdx query returned empty result set, output indexes will be ignored." );
    }
    table = useOutputIndexes ?
      filterTable( table, outputIndexes, columnNames, rowFilter, dataAccess.getColumnDefinitions().size() > 0 )
      : filterTable( table, new ArrayList<Integer>(), columnNames, rowFilter, false );

    //  3. Sort
    if ( !queryOptions.getSortBy().isEmpty() ) {
      // no action
      table = ( new SortTableModel() ).doSort( table, queryOptions.getSortBy() );
    }

    // Create a metadata-aware table model

    final Class<?>[] colTypes = new Class[ table.getColumnCount() ];
    final String[] colNames = new String[ table.getColumnCount() ];

    for ( int i = 0; i < table.getColumnCount(); i++ ) {
      colTypes[ i ] = table.getColumnClass( i );
      colNames[ i ] = table.getColumnName( i );
    }

    final int rowCount = table.getRowCount();
    MetadataTableModel result = new MetadataTableModel( colNames, colTypes, rowCount );
    result.setMetadata( "totalRows", rowCount );
    for ( int r = 0; r < rowCount; r++ ) {
      for ( int j = 0; j < table.getColumnCount(); j++ ) {
        result.setValueAt( table.getValueAt( r, j ), r, j );
      }
    }
    //  4. Pagination
    return paginateTableModel( result, queryOptions );


  }


  /**
   * @param dataAccess
   * @param rawTableModel
   * @return
   */
  private static TableModel evaluateCalculatedColumns( final DataAccess dataAccess, final TableModel rawTableModel ) {
    TableModel table;
    final ArrayList<ColumnDefinition> columnDefinitions = dataAccess.getCalculatedColumns();
    if ( columnDefinitions.isEmpty() ) {
      table = rawTableModel;
    } else {
      table = new CalculatedTableModel( rawTableModel,
        columnDefinitions.toArray( new ColumnDefinition[ columnDefinitions.size() ] ), true );
    }
    return table;
  }


  /**
   * @param table
   * @param outputIndexes
   * @param rowFilter     (optional)
   * @param hasColumnDefinitions
   * @return
   * @throws InvalidOutputIndexException
   */
  private static TableModel filterTable( final TableModel table, List<Integer> outputIndexes,
                                         final List<String> columnNames, final DataTableFilter rowFilter,
                                         boolean hasColumnDefinitions ) throws InvalidOutputIndexException {
    int columnCount = outputIndexes.size();

    if ( columnCount == 0 && ( rowFilter != null || hasColumnDefinitions ) ) {
      // still have to go through the motions if we need to filter rows, or if we have columnDefinitions
      for ( int i = 0; i < table.getColumnCount(); i++ ) {
        outputIndexes.add( i );
      }
      columnCount = outputIndexes.size();
    }

    if ( columnCount != 0 ) {
      //logger.info(Collections.max(outputIndexes)+" "+table.getColumnCount());
      if ( ( Collections.max( outputIndexes ) > table.getColumnCount() - 1 ) ) {
        String errorMessage = String.format( "Output index higher than number of columns in tableModel. %s > %s",
          Collections.max( outputIndexes ), table.getColumnCount() );
        logger.error( errorMessage );

        if ( table.getColumnCount() > 0 ) {
          throw new InvalidOutputIndexException( errorMessage, null );
        } else {
          logger.warn( "Unable to validate output indexes because table metadata is empty. Returning table." );
          return table;
        }
      }

      final int rowCount = table.getRowCount();
      logger.debug( rowCount == 0 ? "No data found" : "Found " + rowCount + " rows" );

      final Class<?>[] colTypes = new Class[ columnCount ];
      final String[] colNames = new String[ columnCount ];
      //just set the number of rows/columns
      final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );

      for ( int rowIn = 0, rowOut = 0; rowIn < rowCount; rowIn++, rowOut++ ) {
        //filter rows
        if ( rowFilter != null && !rowFilter.rowContainsSearchTerms( table, rowIn ) ) {
          rowOut--;
          continue;
        }
        //filter columns
        for ( int j = 0; j < outputIndexes.size(); j++ ) {
          final int outputIndex = outputIndexes.get( j );
          typedTableModel.setValueAt( table.getValueAt( rowIn, outputIndex ), rowOut, j );
        }
      }

      //since we set the calculated table model to infer types, they will be available after rows are evaluated
      for ( int i = 0; i < outputIndexes.size(); i++ ) {
        final int outputIndex = outputIndexes.get( i );
        typedTableModel.setColumnName( i, columnNames.get( outputIndex ) );
        typedTableModel.setColumnType( i, table.getColumnClass( outputIndex ) );
      }
      return typedTableModel;
    }
    return table;
  }


  private static DataTableFilter getRowFilter( final QueryOptions queryOptions, final List<Integer> outputIndexes ) {
    String filterText = StringUtils.trim( queryOptions.getExtraSettings().get( DT_FILTER ) );
    if ( !StringUtils.isEmpty( filterText ) ) {
      int[] searchableIndexes = null;
      if ( queryOptions.getExtraSettings().containsKey( DT_SEARCHABLE ) ) {
        try {
          String[] unparsedIndexes = StringUtils.split( queryOptions.getExtraSettings().get( DT_SEARCHABLE ), ',' );
          searchableIndexes = new int[ unparsedIndexes.length ];
          if ( outputIndexes.size() > 0 ) {
            for ( int i = 0; i < searchableIndexes.length; i++ ) {
              int idx = Integer.parseInt( unparsedIndexes[ i ].trim() );
              searchableIndexes[ i ] = outputIndexes.get( idx );
            }
          }
        } catch ( IndexOutOfBoundsException e ) {
          logger.error( DT_SEARCHABLE + " is out of bounds." );
          searchableIndexes = null;
        } catch ( NumberFormatException e ) {
          logger.error( DT_SEARCHABLE + " not a comma-separated list of integers: " + queryOptions.getExtraSettings()
            .get( DT_SEARCHABLE ) );
          searchableIndexes = null;
        }
      }

      if ( searchableIndexes == null ) { //include all
        searchableIndexes = new int[ outputIndexes.size() ];
        for ( int i = 0; i < searchableIndexes.length; i++ ) {
          searchableIndexes[ i ] = outputIndexes.get( i );
        }
      }

      return new DataTableFilter( filterText, searchableIndexes );
    }
    return null;
  }


  private static List<Integer> getOutputIndexes( final DataAccess dataAccess, final QueryOptions queryOptions,
                                                 TableModel table ) throws InvalidOutputIndexException {

    // override outputIndexes if outputColumnName is provided
    List<String> outputColumnNames = queryOptions.getOutputColumnName();
    if ( !outputColumnNames.isEmpty() ) {
      ArrayList<Integer> outputIndexes = new ArrayList<Integer>();
      ArrayList<String> originalColumnNames = new ArrayList<String>();
      for ( int i = 0; i < table.getColumnCount(); i++ ) {
        originalColumnNames.add( table.getColumnName( i ) );
      }
      for ( String outputColumnName : outputColumnNames ) {
        if ( originalColumnNames.contains( outputColumnName ) ) {
          outputIndexes.add( originalColumnNames.indexOf( outputColumnName ) );
        }
      }
      return outputIndexes;
    }

    // First we need to check if there's nothing to do.
    ArrayList<Integer> outputIndexes = dataAccess.getOutputs( queryOptions.getOutputIndexId() );
    if ( outputIndexes == null ) {
      throw new InvalidOutputIndexException( "Invalid outputIndexId: " + queryOptions.getOutputIndexId(), null );
    }
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
    if ( dataAccess.getOutputMode( queryOptions.getOutputIndexId() ) == DataAccess.OutputMode.EXCLUDE
      && outputIndexes.size() > 0 ) {

      ArrayList<Integer> newOutputIndexes = new ArrayList<Integer>();
      for ( int i = 0; i < table.getColumnCount(); i++ ) {
        if ( !outputIndexes.contains( i ) ) {
          newOutputIndexes.add( i );
        }
      }
      outputIndexes = newOutputIndexes;
    }
    return outputIndexes;
  }


  private static List<String> getColumnNames( final DataAccess dataAccess, TableModel table ) {
    List<String> columnNames = new ArrayList<String>();
    List<ColumnDefinition> columnDefinitions = dataAccess.getColumnDefinitions();

    for( int index = 0; index < table.getColumnCount(); index++ ) {
      String name = "";
      if( columnDefinitions != null ) {
        for ( ColumnDefinition colDef : columnDefinitions ) {
          Integer colIndex = colDef.getIndex();
          if ( colIndex != null && colIndex == index ) {
            name = colDef.getName();
          }
        }
      }

      if ( name.isEmpty() ) {
        columnNames.add( table.getColumnName( index ) );
      } else {
        columnNames.add( name );
      }
    }

    return columnNames;
  }

  public static TableModel copyTableModel( final DataAccess dataAccess, final TableModel t ) {

    // We're removing the ::table-by-index:: cols


    // Build an array of column indexes whose name is different from ::table-by-index::.*
    ArrayList<String> namedColumns = new ArrayList<String>();
    ArrayList<Class<?>> namedColumnsClasses = new ArrayList<Class<?>>();
    for ( int i = 0; i < t.getColumnCount(); i++ ) {
      String colName = t.getColumnName( i );
      if ( !colName.startsWith( "::table-by-index::" )
        && !colName.startsWith( "::column::" ) ) {
        namedColumns.add( colName );
        namedColumnsClasses.add( t.getColumnClass( i ) );
      }
    }

    final int count = namedColumns.size();

    final Class<?>[] colTypes = namedColumnsClasses.toArray( new Class[] { } );
    final String[] colNames = namedColumns.toArray( new String[] { } );

    for ( int i = 0; i < count; i++ ) {
      colTypes[ i ] = t.getColumnClass( i );

      final ColumnDefinition col = dataAccess.getColumnDefinition( i );
      colNames[ i ] = col != null ? col.getName() : t.getColumnName( i );
    }
    final int rowCount = t.getRowCount();
    logger.debug( rowCount == 0 ? "No data found" : "Found " + rowCount + " rows" );
    //if the first row has no values, the class will be Object, however, next rows can have values, we evaluate those
    for ( int i = 0; i < colTypes.length; i++ ) {
      if ( colTypes[ i ] == Object.class ) {
        for ( int k = 0; k < t.getRowCount(); k++ ) {
          if ( t.getValueAt( k, i ) != null ) {
            colTypes[ i ] = t.getValueAt( k, i ).getClass();
            break;
          }
        }
      }
    }

    final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );
    for ( int r = 0; r < rowCount; r++ ) {
      for ( int c = 0; c < count; c++ ) {
        typedTableModel.setValueAt( t.getValueAt( r, c ), r, c );
      }
    }
    return typedTableModel;
  }


  public static TableModel dataAccessMapToTableModel( HashMap<String, DataAccess> dataAccessMap ) {

    int rowCount = dataAccessMap.size();

    // Define names and types
    final String[] colNames = {
      "id", "name", "type"
    };

    final Class<?>[] colTypes = {
      String.class, String.class, String.class
    };

    final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );

    //Automatically sorts the given HashMap by key into a TreeMap
    Map<String, DataAccess> dataAccessSortedMap = new TreeMap<String, DataAccess>( dataAccessMap );

    for ( DataAccess dataAccess : dataAccessSortedMap.values() ) {
      if ( dataAccess.getAccess() == DataAccessEnums.ACCESS_TYPE.PUBLIC ) {
        typedTableModel.addRow( new Object[] {
          dataAccess.getId(), dataAccess.getName(), dataAccess.getType()
        } );
      }
    }

    return typedTableModel;

  }


  public static TableModel dataAccessParametersToTableModel( final ArrayList<Parameter> parameters ) {

    int rowCount = parameters.size();

    // Define names and types
    final String[] colNames = {
      "name", "type", "defaultValue", "pattern", "access"
    };

    final Class<?>[] colTypes = {
      String.class, String.class, String.class, String.class, String.class
    };

    final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );

    for ( Parameter p : parameters ) {
      typedTableModel.addRow( new Object[] {
        p.getName(), p.getTypeAsString(), p.getDefaultValue(), p.getPattern(), p.getAccess().toString()
      } );
    }

    return typedTableModel;

  }


  /**
   * Method to append a tablemodel into another. We'll make no guarantees about the types
   *
   * @param tableModelA TableModel to be modified
   * @param tableModelB Contents to be appended #
   */
  public static TableModel appendTableModel( final TableModel tableModelA, final TableModel tableModelB ) {

    // We will believe the data is correct - no type checking

    int colCountA = tableModelA.getColumnCount(),
      colCountB = tableModelB.getColumnCount();
    boolean usingA = colCountA > colCountB;
    int colCount = usingA ? colCountA : colCountB;
    TableModel referenceTable = ( usingA ? tableModelA : tableModelB );

    final Class<?>[] colTypes = new Class[ colCount ];
    final String[] colNames = new String[ colCount ];

    for ( int i = 0; i < referenceTable.getColumnCount(); i++ ) {
      colTypes[ i ] = referenceTable.getColumnClass( i );
      colNames[ i ] = referenceTable.getColumnName( i );
    }

    int rowCount = tableModelA.getRowCount() + tableModelB.getRowCount();


    // Table A
    final TypedTableModel typedTableModel = new TypedTableModel( colNames, colTypes, rowCount );
    for ( int r = 0; r < tableModelA.getRowCount(); r++ ) {
      for ( int c = 0; c < colTypes.length; c++ ) {
        typedTableModel.setValueAt( tableModelA.getValueAt( r, c ), r, c );
      }
    }

    // Table B
    int rowCountOffset = tableModelA.getRowCount();
    for ( int r = 0; r < tableModelB.getRowCount(); r++ ) {
      for ( int c = 0; c < colTypes.length; c++ ) {
        typedTableModel.setValueAt( tableModelB.getValueAt( r, c ), r + rowCountOffset, c );
      }
    }


    return typedTableModel;

  }


  private static TableModel paginateTableModel( MetadataTableModel t, QueryOptions queryOptions ) {

    if ( !queryOptions.isPaginate() || ( queryOptions.getPageSize() == 0 && queryOptions.getPageStart() == 0 ) ) {
      return t;
    }


    final int rowCount = Math.min( queryOptions.getPageSize(), t.getRowCount() - queryOptions.getPageStart() );
    logger.debug( "Paginating " + queryOptions.getPageSize() + " pages from page " + queryOptions.getPageStart() );


    final Class<?>[] colTypes = new Class[ t.getColumnCount() ];
    final String[] colNames = new String[ t.getColumnCount() ];

    for ( int i = 0; i < t.getColumnCount(); i++ ) {
      colTypes[ i ] = t.getColumnClass( i );
      colNames[ i ] = t.getColumnName( i );
    }

    final MetadataTableModel resultTableModel =
      new MetadataTableModel( colNames, colTypes, rowCount, t.getAllMetadata() );
    resultTableModel.setMetadata( "pageSize", queryOptions.getPageSize() );
    resultTableModel.setMetadata( "pageStart", queryOptions.getPageStart() );

    for ( int r = 0; r < rowCount; r++ ) {
      for ( int j = 0; j < t.getColumnCount(); j++ ) {
        resultTableModel.setValueAt( t.getValueAt( r + queryOptions.getPageStart(), j ), r, j );
      }
    }

    return resultTableModel;


  }
}
