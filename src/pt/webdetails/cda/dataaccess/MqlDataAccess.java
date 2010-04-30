package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.platform.plugin.connection.PentahoPmdConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
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
public class MqlDataAccess extends PREDataAccess {

  private static final ConnectionType connectionType = ConnectionType.MQL;

  public MqlDataAccess(final Element element) {
    super(element);
  }

  public MqlDataAccess() {
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {
    final MetadataConnection connection = (MetadataConnection) getCdaSettings().getConnection(getConnectionId());

    final PmdDataFactory returnDataFactory = new PmdDataFactory();
    returnDataFactory.setXmiFile(connection.getMetadataConnectionInfo().getXmiFile());
    returnDataFactory.setDomainId(connection.getMetadataConnectionInfo().getDomainId());
    if (CdaEngine.getInstance().isStandalone()) {
      returnDataFactory.setConnectionProvider(new PmdConnectionProvider());
    } else {
      returnDataFactory.setConnectionProvider(new PentahoPmdConnectionProvider());
    }
    returnDataFactory.setQuery("query", getQuery());

    try {
			returnDataFactory.open();
		} catch (ReportDataFactoryException e) {
			throw new InvalidConnectionException("Error opening mql data factory.", e);
		}

    return returnDataFactory;
  }

  public String getType() {
    return "mql";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.MQL;
  }
}
