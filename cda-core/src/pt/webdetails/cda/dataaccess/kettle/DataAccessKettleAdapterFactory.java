package pt.webdetails.cda.dataaccess.kettle;

import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.query.QueryOptions;

/**
 * Creates data access to Kettle adapater using data access type
 * 
 * @author Michael Spector
 */
public class DataAccessKettleAdapterFactory {

	/**
	 * @return adapter or <code>null</code> if not defined for the given type
	 */
	public static DataAccessKettleAdapter create(DataAccess dataAccess, QueryOptions queryOptions) {
		if ("sql".equals(dataAccess.getType())) {
			return new SQLKettleAdapter((SqlDataAccess) dataAccess, queryOptions);
		}
		return null;
	}
}
