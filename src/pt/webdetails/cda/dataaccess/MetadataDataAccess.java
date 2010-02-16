package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:17:20
 *
 * @author Thomas Morgner.
 */
public class MetadataDataAccess extends PREDataAccess
{
  public MetadataDataAccess(final Element element)
  {
    super(element);
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final MetadataConnection connection = (MetadataConnection) getCdaSettings().getConnection(getConnectionId());

    final PmdDataFactory returnDataFactory = new PmdDataFactory();
    returnDataFactory.setXmiFile(connection.getMetadataConnectionInfo().getXmiFile());
    returnDataFactory.setDomainId(connection.getMetadataConnectionInfo().getDomainId());
    returnDataFactory.setConnectionProvider(new PmdConnectionProvider());
    returnDataFactory.setQuery("query", getQuery());

    return returnDataFactory;
  }

  public String getType()
  {
    return "metadata";
  }
}
