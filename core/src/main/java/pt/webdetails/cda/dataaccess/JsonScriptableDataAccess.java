/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.dataaccess;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cda.dataaccess.Parameter.Type;

public class JsonScriptableDataAccess extends ScriptableDataAccess {

  private static final Log logger = LogFactory.getLog( AbstractDataAccess.class );

  public JsonScriptableDataAccess( final Element element ) {
    super( element );
  }

  public JsonScriptableDataAccess() {
  }

  @Override
  public String getType() {
    return "jsonScriptable";
  }

  // Do not log the script.
  @Override
  protected String getLogQuery() {
    return null;
  }

  @Override
  public String getQuery() {
    String query = this.query;
    try {
      JSONObject jsonQuery = new JSONObject( query );
      if ( !jsonQuery.isNull( "metadata" ) && !jsonQuery.isNull( "resultset" ) ) {
        StringBuilder builder = new StringBuilder();

        //get fields metadata and resultset
        JSONArray metadata = jsonQuery.getJSONArray( "metadata" );
        JSONArray resultSet = jsonQuery.getJSONArray( "resultset" );

        int columns = metadata.length();
        String[] columnTypes = new String[ columns ];

        String colNames = "";
        String colTypes = "";

        for ( int i = 0; i < columns; i++ ) {
          JSONObject column = metadata.getJSONObject( i );
          String classType = getClassFromType( Type.parse( column.getString( "colType" ) ) );

          if ( i > 0 ) {
            colNames += ", ";
            colTypes += ", ";
          }
          colNames += "\"" + column.getString( "colName" ) + "\"";
          colTypes += classType + ".class";
          columnTypes[ i ] = classType;
        }

        //build TypedTableModel instance
        builder.append( addTypedModelObject( colNames, colTypes ) );

        //build TypedTableModel rows
        for ( int i = 0, L = resultSet.length(); i < L; i++ ) { //row
          JSONArray row = resultSet.getJSONArray( i );
          builder.append( "model.addRow(new Object[]{" );

          for ( int j = 0; j < columns; j++ ) { //row parameter
            if ( j > 0 ) {
              builder.append( ", " );
            }
            builder.append( addParameter( row.getString( j ), columnTypes[ j ] ) );
          }
          builder.append( "});\n" );
        }
        builder.append( "return model;\n" );
        query = builder.toString();
      }

    } catch ( JSONException e ) {
      logger.error( e.getMessage() );
    }

    return query;
  }

  private String addTypedModelObject( String columnNames, String columnTypes ) {
    String typedModel = "import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;\n\n"
      + "String[] columnNames = new String[]{\n" + columnNames + "\n};\n\n"
      + "Class[] columnTypes = new Class[]{\n" + columnTypes + "\n};\n\n"
      + "TypedTableModel model = new TypedTableModel(columnNames, columnTypes);\n\n";

    return typedModel;
  }

  private String addParameter( String value, String type ) {
    if ( value.equals( "null" ) ) {
      return value;
    }

    if ( type.equals( "String" ) ) {
      value = "\"" + value + "\"";
    }

    return "new " + type + "(" + value + ")";
  }

  private String getClassFromType( Type type ) {
    if ( Type.NUMERIC.equals( type ) ) {
      return "Double";
    } else if ( Type.INTEGER.equals( type ) ) {
      return "Long";
    } else if ( Type.STRING.equals( type ) ) {
      return "String";
    } else {
      return "";
    }
  }
}
