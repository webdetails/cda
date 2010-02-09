package pt.webdetails.cda.connections.mondrian;

import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.InvalidConnectionException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractMondrianConnection extends AbstractConnection implements MondrianConnection
{


  public AbstractMondrianConnection(final Element connection) throws InvalidConnectionException
  {

    super(connection);

  }

}