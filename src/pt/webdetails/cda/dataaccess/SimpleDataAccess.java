package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import javax.swing.table.TableModel;

/**
 * Implementation of the SimpleDataAccess
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:04:10 AM
 */
public abstract class SimpleDataAccess extends AbstractDataAccess {


  private static final Log logger = LogFactory.getLog(SimpleDataAccess.class);

  private String connectionId;
  static final DataAccessEnums.DATA_ACCESS_TYPE dataAccessType = DataAccessEnums.DATA_ACCESS_TYPE.SIMPLE_DATA_ACCESS;

  private String query;


  public SimpleDataAccess(final Element element) {

    super(element);
    connectionId = element.attributeValue("connection");
    query = element.selectSingleNode("./Query").getText();

  }

  @Override
  public TableModel queryData() throws QueryException{


    TableModel tableModel = queryDataSource();

    // Copy the tableModel and cache it
    logger.warn("TODO - Handle the datasource (copy, cache, manipulate");


    closeDataSource();


    return tableModel;
  }


  public abstract TableModel queryDataSource() throws QueryException;

  public abstract void closeDataSource() throws QueryException;


  public String getQuery() {
    return query;
  }

  public String getConnectionId() {
    return connectionId;
  }

}
