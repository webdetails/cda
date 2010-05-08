package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.kettle.KettleConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:20:39
 *
 * @author Thomas Morgner.
 */
public class KettleDataAccess extends PREDataAccess
{

  public KettleDataAccess(final Element element)
  {
    super(element);
  }

  public KettleDataAccess()
  {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final KettleConnection connection = (KettleConnection) getCdaSettings().getConnection(getConnectionId());

    final KettleDataFactory dataFactory = new KettleDataFactory();
    dataFactory.setQuery("query", connection.createTransformationProducer(getQuery()));
    return dataFactory;
  }

  public String getType()
  {
    return "kettle";
  }

  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.KETTLE;
  }
}
