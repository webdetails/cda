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


package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;

public class SqlJndiConnectionInfo extends pt.webdetails.cda.connections.JndiConnectionInfo {
  public SqlJndiConnectionInfo( final Element connection ) {
    super( connection );
  }

  public SqlJndiConnectionInfo( String jndi, String userName, String password, String userFormula,
                                String passFormula ) {
    super( jndi, userName, password, userFormula, passFormula );
  }

}
