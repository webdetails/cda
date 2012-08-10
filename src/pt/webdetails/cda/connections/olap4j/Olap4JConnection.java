package pt.webdetails.cda.connections.olap4j;

import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.OlapConnectionProvider;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:23:55
 *
 * @author Thomas Morgner.
 */
public interface Olap4JConnection extends Connection
{
  public OlapConnectionProvider getInitializedConnectionProvider() throws InvalidConnectionException;

  public String getUrl();
  public String getDriver();
  public String getUser();
  public String getPass();
  public String getRole();
  public String getRoleField();
  public String getUserField();
  public String getPasswordField();
}
