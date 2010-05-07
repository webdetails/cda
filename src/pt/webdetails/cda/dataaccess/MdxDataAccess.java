package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoCubeFileProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianConnectionProvider;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class MdxDataAccess extends PREDataAccess {

  private static final Log logger = LogFactory.getLog(MdxDataAccess.class);

  public MdxDataAccess(final Element element) {
    super(element);
  }

  public MdxDataAccess() {
  }

  protected AbstractNamedMDXDataFactory createDataFactory() {
    return new BandedMDXDataFactory();
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException {

    logger.debug("Creating BandedMDXDataFactory");

    final MondrianConnection connection = (MondrianConnection) getCdaSettings().getConnection(getConnectionId());
    final MondrianConnectionInfo mondrianConnectionInfo = connection.getConnectionInfo();

    final AbstractNamedMDXDataFactory mdxDataFactory = createDataFactory();
    mdxDataFactory.setDataSourceProvider(connection.getInitializedDataSourceProvider());
    mdxDataFactory.setJdbcPassword(mondrianConnectionInfo.getPass());
    mdxDataFactory.setJdbcUser(mondrianConnectionInfo.getUser());
    mdxDataFactory.setRole(mondrianConnectionInfo.getMondrianRole());
    mdxDataFactory.setRoleField(mondrianConnectionInfo.getRoleField());
    mdxDataFactory.setJdbcPasswordField(mondrianConnectionInfo.getPasswordField());    
    mdxDataFactory.setJdbcUserField(mondrianConnectionInfo.getUserField());
    
    if (CdaEngine.getInstance().isStandalone()) {
      mdxDataFactory.setCubeFileProvider(new DefaultCubeFileProvider(mondrianConnectionInfo.getCatalog()));
    } else {
      mdxDataFactory.setCubeFileProvider(new PentahoCubeFileProvider(mondrianConnectionInfo.getCatalog()));
      mdxDataFactory.setMondrianConnectionProvider(new PentahoMondrianConnectionProvider());
    }

    mdxDataFactory.setQuery("query", getQuery());

    return mdxDataFactory;


  }

  public String getType() {
    return "mdx";
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.MDX;
  }
}
