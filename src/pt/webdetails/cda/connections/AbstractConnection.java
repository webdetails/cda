package pt.webdetails.cda.connections;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractConnection implements Connection {

  private String id;
  private CdaSettings cdaSettings;

  public AbstractConnection()
  {
  }
  
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
  public abstract String getType();


  public CdaSettings getCdaSettings() {
    return cdaSettings;
  }


  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(final Object obj);


}
