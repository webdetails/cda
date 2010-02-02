package pt.webdetails.cda.connections;

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

/**
 * Holds the Connections Settings of a file
 *
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface Connection {

  public String getId();

  public ConnectionProvider getConnectionProvider();

}
