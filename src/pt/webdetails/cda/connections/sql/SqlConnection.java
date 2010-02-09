package pt.webdetails.cda.connections.sql;

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 9, 2010
 * Time: 12:13:28 PM
 */
public interface SqlConnection extends Connection
{


  public ConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException;


}
