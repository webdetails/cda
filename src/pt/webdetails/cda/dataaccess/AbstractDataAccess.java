package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * This is the top level implementation of a DataAccess. Only the common methods are used here
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:05:38 AM
 */
public abstract class AbstractDataAccess implements DataAccess
{

  private static final Log logger = LogFactory.getLog(AbstractDataAccess.class);

  private CdaSettings cdaSettings;
  private String id;
  private DataAccessEnums.ACCESS_TYPE access = DataAccessEnums.ACCESS_TYPE.PUBLIC;
  private boolean cache = false;
  private int cacheDuration = 3600;


  protected AbstractDataAccess(final Element element)
  {

    id = element.attributeValue("id");

    if (element.attributeValue("access") != null && element.attributeValue("access").equals("private"))
    {
      access = DataAccessEnums.ACCESS_TYPE.PRIVATE;
    }

    if (element.attributeValue("cache") != null && element.attributeValue("cache").equals("true"))
    {
      cache = true;
    }

    if (element.attribute("cacheDuration") != null)
    {
      cacheDuration = Integer.parseInt(element.attributeValue("cacheDuration"));
    }

  }

  @Override
  public TableModel doQuery() throws QueryException
  {


    TableModel tableModel, newTableModel;

    logger.warn("TODO - Implement cache");
    boolean isCached = false;

    if (isCached)
    {
      tableModel = null; // TODO - change this
    }
    else
    {
      tableModel = queryDataSource();
    }

    // Handle the TableModel

    TableModelUtils tableModelUtils = TableModelUtils.getInstance();
    newTableModel = tableModelUtils.transformTableModel(this, tableModel);

    // Close it
    if(!isCached){
      closeDataSource();
    }

    logger.debug("Query " + getId() +  " done successfully - returning tableModel");
    return tableModel;

  }


  public abstract TableModel queryDataSource() throws QueryException;

  public abstract void closeDataSource() throws QueryException;


  @Override
  public String getId()
  {
    return id;
  }


  public DataAccessEnums.ACCESS_TYPE getAccess()
  {
    return access;
  }

  public boolean isCache()
  {
    return cache;
  }

  public int getCacheDuration()
  {
    return cacheDuration;
  }

  public CdaSettings getCdaSettings()
  {
    return cdaSettings;
  }

  public void setCdaSettings(CdaSettings cdaSettings)
  {
    this.cdaSettings = cdaSettings;
  }


}
