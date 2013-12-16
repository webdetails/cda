package pt.webdetails.cda.dataaccess.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLParameterLookupParser;

import pt.webdetails.cda.connections.sql.JdbcConnection;
import pt.webdetails.cda.connections.sql.JdbcConnectionInfo;
import pt.webdetails.cda.connections.sql.JndiConnection;
import pt.webdetails.cda.connections.sql.SqlConnection;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownConnectionException;

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

	public SQLKettleAdapter(SqlDataAccess dataAccess, QueryOptions queryOptions) {
		this.dataAccess = dataAccess;
		this.queryOptions = queryOptions;
	}

	public String getKettleStepDefinition(String name) throws KettleAdapterException {
		try {
			TableInputMeta tableInputMeta = new TableInputMeta();
			tableInputMeta.setDatabaseMeta(getDatabaseMeta());

			prepareQuery();
			tableInputMeta.setSQL(translatedQuery);

			StepMeta meta = new StepMeta(name, tableInputMeta);
			return meta.getXML();

		} catch (Exception e) {
			throw new KettleAdapterException("Error initializing Kettle step for SQL data access type", e);
		}
	}

	private void prepareQuery() throws KettleAdapterException {
		if (translatedQuery == null) {
			try {
				parameters = Parameter.createParameterDataRowFromParameters(dataAccess
						.getFilledParameters(queryOptions));
				SQLParameterLookupParser parser = new SQLParameterLookupParser(true);
				translatedQuery = parser.translateAndLookup(dataAccess.getQuery(), parameters);
				parameterNames = parser.getFields();
			} catch (Exception e) {
				throw new KettleAdapterException("Unable to substitute data access parameters", e);
			}
		}
	}

	public DataRow getParameters() throws KettleAdapterException {
		prepareQuery();
		return parameters;
	}

	public String[] getParameterNames() throws KettleAdapterException {
		prepareQuery();
		return parameterNames;
	}

	protected DatabaseMeta getDatabaseMeta() throws KettleAdapterException {
		if (databaseMeta == null) {
			SqlConnection connection;
			try {
				connection = (SqlConnection) dataAccess.getCdaSettings().getConnection(dataAccess.getConnectionId());
			} catch (UnknownConnectionException e) {
				throw new KettleAdapterException(e);
			}

			if (connection instanceof JdbcConnection) {
				JdbcConnectionInfo connectionInfo = ((JdbcConnection) connection).getConnectionInfo();
				databaseMeta = new DatabaseMeta(connection.getId(), "GENERIC", "Native", null, null, null,
						connectionInfo.getUser(), connectionInfo.getPass());
				databaseMeta.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, connectionInfo.getUrl());
				databaseMeta.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS,
						connectionInfo.getDriver());

			} else if (connection instanceof JndiConnection) {
				JndiConnection jndiConnection = (JndiConnection) connection;
				databaseMeta = new DatabaseMeta(connection.getId(), "GENERIC", "JNDI", null, jndiConnection
						.getConnectionInfo().getJndi(), null, null, null);

			} else {
				throw new KettleAdapterException("Unsupported connection type: " + connection.getClass().getName());
			}
		}
		return databaseMeta;
	}

	public DatabaseMeta[] getDatabases() throws KettleAdapterException {
		return new DatabaseMeta[] { getDatabaseMeta() };
	}
}
