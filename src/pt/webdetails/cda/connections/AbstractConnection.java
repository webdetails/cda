package pt.webdetails.cda.connections;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractConnection implements Connection {

  private String id;


  public AbstractConnection(final Element connection) throws InvalidConnectionException {

    id = connection.attributeValue("id");

    initializeConnection(connection);

  }

  protected abstract void initializeConnection(Element connection) throws InvalidConnectionException;


  @Override
  public String getId() {
    return id;
  }

  @Override
  public abstract ConnectionProvider getConnectionProvider();


}
