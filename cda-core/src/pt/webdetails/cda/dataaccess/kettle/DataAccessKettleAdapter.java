package pt.webdetails.cda.dataaccess.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;

/**
 * Adapts data access to Kettle transformation step
 * 
 * @author Michael Spector
 */
public interface DataAccessKettleAdapter {

	/**
	 * @param name Step name
	 * @return Kettle step definition
	 * @throws KettleAdapterException
	 */
	public StepMeta getKettleStepMeta(String name) throws KettleAdapterException;

	/**
	 * @return return used database connections if any, otherwise
	 *         <code>null</code>
	 * @throws KettleAdapterException
	 */
	public DatabaseMeta[] getDatabases() throws KettleAdapterException;

	/**
	 * @return parameter names as they appear in query
	 */
	public String[] getParameterNames() throws KettleAdapterException;
	
	/**
	 * @return data access parameters
	 */
	public DataRow getParameters() throws KettleAdapterException;
}
