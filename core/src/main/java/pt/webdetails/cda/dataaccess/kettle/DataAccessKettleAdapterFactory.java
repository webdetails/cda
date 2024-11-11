/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.dataaccess.kettle;

import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.query.QueryOptions;

/**
 * Creates data access to Kettle adapater using data access type
 */
public class DataAccessKettleAdapterFactory {

  /**
   * @return adapter or <code>null</code> if not defined for the given type
   */
  public static DataAccessKettleAdapter create( DataAccess dataAccess, QueryOptions queryOptions ) {
    if ( "sql".equals( dataAccess.getType() ) ) {
      return new SQLKettleAdapter( (SqlDataAccess) dataAccess, queryOptions );
    }
    return null;
  }
}
