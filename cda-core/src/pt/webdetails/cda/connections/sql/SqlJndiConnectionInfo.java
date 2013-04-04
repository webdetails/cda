package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:54:53
 *
 * @author Thomas Morgner.
 */
public class SqlJndiConnectionInfo extends pt.webdetails.cda.connections.JndiConnectionInfo
{
  public SqlJndiConnectionInfo(final Element connection) {
    super(connection);
  }
  
  public SqlJndiConnectionInfo(String jndi, String userName, String password, String userFormula, String passFormula){
    super(jndi, userName, password, userFormula, passFormula);
  }

}
