package pt.webdetails.cda.connections.sql;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractSqlConnection extends AbstractConnection implements SqlConnection
{


  public AbstractSqlConnection(final Element connection) throws InvalidConnectionException
  {

    super(connection);

  }

   
}
