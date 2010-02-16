package pt.webdetails.cda.connections.mondrian;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DataSourceProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.JndiDataSourceProvider;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.Util;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:18 PM
 */
public class JndiConnection extends AbstractMondrianConnection
{

  private static final Log logger = LogFactory.getLog(JndiConnection.class);
  public static final String TYPE = "mondrianJndi";

  private CdaSettings cdaSettings;
  private JndiDataSourceProvider connectionProvider;
  private JndiConnectionInfo connectionInfo;

  public JndiConnection(final Element connection) throws InvalidConnectionException
  {

    super(connection);

  }


  @Override
  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {

    connectionInfo = new JndiConnectionInfo(connection);

  }

  @Override
  public String getType()
  {
    return TYPE;
  }

  public DataSourceProvider getInitializedDataSourceProvider() throws InvalidConnectionException
  {


    logger.debug("Creating new jndi connection");

    connectionProvider = new JndiDataSourceProvider(connectionInfo.getJndi());

    return connectionProvider;
  }

  public void setCdaSettings(final CdaSettings cdaSettings)
  {
    this.cdaSettings = cdaSettings;
  }

  public JndiConnectionInfo getConnectionInfo()
  {
    return connectionInfo;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final JndiConnection that = (JndiConnection) o;

    if (connectionInfo != null ? !connectionInfo.equals(that.connectionInfo) : that.connectionInfo != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }
}