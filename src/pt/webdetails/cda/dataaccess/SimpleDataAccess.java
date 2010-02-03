package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;

import javax.swing.table.TableModel;

/**
 * Implementation of the SimpleDataAccess
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 11:04:10 AM
 */
public abstract class SimpleDataAccess extends AbstractDataAccess {


  private String connectionId;
  static final DataAccessEnums.DATA_ACCESS_TYPE dataAccessType = DataAccessEnums.DATA_ACCESS_TYPE.SIMPLE_DATA_ACCESS;

  private String query;


  public SimpleDataAccess(final Element element) {

    super(element);
    connectionId = element.attributeValue("connection");
    query = element.selectSingleNode("./Query").getText();

  }

  @Override
  public abstract TableModel queryData();


  public String getQuery() {
    return query;
  }

  public String getConnectionId() {
    return connectionId;
  }

}
