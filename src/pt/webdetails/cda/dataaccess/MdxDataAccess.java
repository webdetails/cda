package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoCubeFileProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianConnectionProvider;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.settings.UnknownConnectionException;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class MdxDataAccess extends PREDataAccess
{

  private static final Log logger = LogFactory.getLog(MdxDataAccess.class);

  public enum BANDED_MODE
  {

    CLASSIC, COMPACT
  };
  private BANDED_MODE bandedMode = BANDED_MODE.CLASSIC;

  public MdxDataAccess(final Element element)
  {
    super(element);

    try
    {
      bandedMode = BANDED_MODE.valueOf(element.selectSingleNode("./BandedMode").getText().toUpperCase());

    }
    catch (Exception e)
    {
      // Getting defaults
      try
      {
        String _mode = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.BandedMDXMode");
        if (_mode != null)
        {
          bandedMode = BANDED_MODE.valueOf(_mode);
        }
      }
      catch (Exception ex)
      {
        // ignore, let the default take it's place
      }

    }


  }

  public MdxDataAccess()
  {
  }

  protected AbstractNamedMDXDataFactory createDataFactory()
  {
    if (getBandedMode() == BANDED_MODE.CLASSIC)
    {
      return new BandedMDXDataFactory();

    }
    else{
      return new CompactBandedMDXDataFactory();
    }
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {

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

    if (CdaEngine.getInstance().isStandalone())
    {
      mdxDataFactory.setCubeFileProvider(new DefaultCubeFileProvider(mondrianConnectionInfo.getCatalog()));
    }
    else
    {
      mdxDataFactory.setCubeFileProvider(new PentahoCubeFileProvider(mondrianConnectionInfo.getCatalog()));
      mdxDataFactory.setMondrianConnectionProvider(new PentahoMondrianConnectionProvider());
    }

    mdxDataFactory.setQuery("query", getQuery());

    return mdxDataFactory;


  }

  public BANDED_MODE getBandedMode()
  {
    return bandedMode;
  }

  public String getType()
  {
    return "mdx";
  }

  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.MDX;
  }

  @Override
  protected TableModel postProcessTableModel(TableModel tm)
  {
    logger.debug("Determining if we need to transform the BandedMDXTableModel");

    /*
    if( tm instanceof BandedMDXDataFactory){
    BandedMDXDataFactory df = (BandedMDXDataFactory) tm;
    TableModel t = new CompactBandedMDXTableModel(df., rowLimit)
    }
     */
    return tm;
  }

  @Override
  protected Object getExtraCacheKey()
  {
    return new ExtraCacheKey(bandedMode);
  }

  protected static class ExtraCacheKey
  {

    private BANDED_MODE bandedMode;

    public ExtraCacheKey(BANDED_MODE bandedMode)
    {
      this.bandedMode = bandedMode;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final ExtraCacheKey other = (ExtraCacheKey) obj;
      if (this.bandedMode != other.bandedMode && (this.bandedMode == null || !this.bandedMode.equals(other.bandedMode)))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 71 * hash + (this.bandedMode != null ? this.bandedMode.hashCode() : 0);
      return hash;
    }
  }
}
