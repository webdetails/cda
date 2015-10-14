/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.dataaccess.kettle;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.formula.FormulaMeta;
import org.pentaho.di.trans.steps.formula.FormulaMetaFunction;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLParameterLookupParser;
import pt.webdetails.cda.connections.sql.JdbcConnection;
import pt.webdetails.cda.connections.sql.JdbcConnectionInfo;
import pt.webdetails.cda.connections.sql.JndiConnection;
import pt.webdetails.cda.connections.sql.SqlConnection;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts from SQL data access to Kettle "Table Input" step
 *
 * @author Michael Spector
 */
public class SQLKettleAdapter implements DataAccessKettleAdapter {

  private SqlDataAccess dataAccess;
  private QueryOptions queryOptions;
  private DatabaseMeta databaseMeta;
  private String translatedQuery;
  private String[] parameterNames;
  private DataRow parameters;

  private final String DUMMY_DATABASE_NAME = "cda_dummy_datasource_name_for_export";

  public SQLKettleAdapter( SqlDataAccess dataAccess, QueryOptions queryOptions ) {
    this.dataAccess = dataAccess;
    this.queryOptions = queryOptions;
  }

  @Override
  public StepMeta getFilterStepMeta( String name, String[] columns )
    throws KettleAdapterException {
    try {
      SelectValuesMeta selectValuesMeta = new SelectValuesMeta();
      selectValuesMeta.setDefault();
      ArrayList<String> fields = new ArrayList<String>();
      List<ColumnDefinition> calculatedColumns = dataAccess.getCalculatedColumns();
      if ( calculatedColumns.size() > 0 ) {
        List<String> extendedColumns = new ArrayList<String>();
        for ( String s : columns ) {
          extendedColumns.add( s );
        }
        for ( ColumnDefinition col : calculatedColumns ) {
          extendedColumns.add( col.getName() );
        }
        columns = extendedColumns.toArray( new String[ extendedColumns.size() ] );
      }
      ArrayList<Integer> outputs = dataAccess.getOutputs();
      for ( int n = 0; n < outputs.size(); n++ ) {
        if ( outputs.get( n ) > columns.length - 1 ) {
          throw new KettleAdapterException(
            "Error initializing Kettle Select Field step for SQL data access type. Invalid index." );
        }

        fields.add( columns[ outputs.get( n ) ] );
      }
      if ( dataAccess.getOutputMode().ordinal() == 0 ) {
        String[] emptyStr = new String[ fields.size() ];
        int[] dummyValue = new int[ fields.size() ];
        for ( int n = 0; n < fields.size(); n++ ) {
          emptyStr[ n ] = "";
          dummyValue[ n ] = -2;
        }
        selectValuesMeta.setSelectRename( emptyStr );
        selectValuesMeta.setSelectLength( dummyValue );
        selectValuesMeta.setSelectPrecision( dummyValue );
        selectValuesMeta.setSelectName( fields.toArray( new String[ fields.size() ] ) );
      } else {
        selectValuesMeta.setDeleteName( fields.toArray( new String[ fields.size() ] ) );
      }
      StepMeta stepMeta = new StepMeta( name, selectValuesMeta );
      stepMeta.setCopies( 1 );
      return stepMeta;
    } catch ( Exception e ) {
      throw new KettleAdapterException( "Error initializing Kettle Select Field step for SQL data access type", e );
    }
  }

  @Override
  public StepMeta getKettleStepMeta( String name ) throws KettleAdapterException {
    try {
      TableInputMeta tableInputMeta = new TableInputMeta();
      tableInputMeta.setDatabaseMeta( getDatabaseMeta() );

      prepareQuery();
      tableInputMeta.setSQL( translatedQuery );

      StepMeta stepMeta = new StepMeta( name, tableInputMeta );
      stepMeta.setCopies( 1 );
      return stepMeta;

    } catch ( Exception e ) {
      throw new KettleAdapterException( "Error initializing Kettle step for SQL data access type", e );
    }
  }

  private void prepareQuery() throws KettleAdapterException {
    if ( translatedQuery == null ) {
      try {
        parameters = Parameter.createParameterDataRowFromParameters( dataAccess.getFilledParameters( queryOptions ) );
        SQLParameterLookupParser parser = new SQLParameterLookupParser( true );
        translatedQuery = parser.translateAndLookup( dataAccess.getQuery(), parameters );
        parameterNames = parser.getFields();
      } catch ( Exception e ) {
        throw new KettleAdapterException( "Unable to substitute data access parameters", e );
      }
    }
  }

  @Override
  public DataRow getParameters() throws KettleAdapterException {
    prepareQuery();
    return parameters;
  }

  @Override
  public String[] getParameterNames() throws KettleAdapterException {
    prepareQuery();
    return parameterNames;
  }

  protected DatabaseMeta getDatabaseMeta() throws KettleAdapterException {
    if ( databaseMeta == null ) {
      SqlConnection connection;
      try {
        connection = (SqlConnection) dataAccess.getCdaSettings().getConnection( dataAccess.getConnectionId() );
      } catch ( UnknownConnectionException e ) {
        throw new KettleAdapterException( e );
      }
      if ( connection instanceof JdbcConnection ) {
        JdbcConnectionInfo connectionInfo = ( (JdbcConnection) connection ).getConnectionInfo();
        databaseMeta = new DatabaseMeta( connection.getId(), "GENERIC", "Native", null, null, null,
          connectionInfo.getUser(), connectionInfo.getPass() );
        databaseMeta.getAttributes().put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, connectionInfo.getUrl() );
        databaseMeta.getAttributes().put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS,
            connectionInfo.getDriver() );
      } else {
        if ( connection instanceof JndiConnection ) {
          JndiConnection jndiConnection = (JndiConnection) connection;
          databaseMeta = new DatabaseMeta( connection.getId(), "GENERIC", "JNDI", null, jndiConnection
            .getConnectionInfo().getJndi(), null, null, null );
        } else {
          throw new KettleAdapterException( "Unsupported connection type: " + connection.getClass().getName() );
        }
      }
    }
    // as of 5.4, databaseMeta must have a database name - http://jira.pentaho.com/browse/PDI-14063
    // this can be removed when PDI-14063 is fixed
    if ( StringUtils.isEmpty( databaseMeta.getDatabaseName() ) ) {
      databaseMeta.setDBName( DUMMY_DATABASE_NAME );
    }
    return databaseMeta;
  }

  @Override
  public DatabaseMeta[] getDatabases() throws KettleAdapterException {
    return new DatabaseMeta[] { getDatabaseMeta() };
  }

  @Override
  public ArrayList<Integer> getDataAccessOutputs() throws KettleAdapterException {
    return dataAccess.getOutputs();
  }

  @Override
  public boolean hasCalculatedColumns() {
    return dataAccess.getCalculatedColumns().size() > 0;
  }

  @Override
  public StepMeta getFormulaStepMeta( String name ) {
    FormulaMeta formulaMeta = new FormulaMeta();

    formulaMeta.setDefault();
    List<FormulaMetaFunction> calcTypes = new ArrayList<FormulaMetaFunction>();
    String formula;
    for ( ColumnDefinition col : dataAccess.getCalculatedColumns() ) {
      formula = col.getFormula();
      if ( formula.indexOf( "=" ) == 0 ) {
        formula = formula.substring( 1 );
      }
      calcTypes.add( new FormulaMetaFunction( col.getName(), formula, col.getType().ordinal(), -1, -1, "" ) );
    }
    formulaMeta.setFormula( calcTypes.toArray( new FormulaMetaFunction[ calcTypes.size() ] ) );
    StepMeta stepMeta = new StepMeta( name, formulaMeta );
    stepMeta.setCopies( 1 );
    return stepMeta;
  }

}
