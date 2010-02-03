package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import javax.swing.table.TableModel;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class SqlDataAccess extends SimpleDataAccess {

  private static final Log logger = LogFactory.getLog(SqlDataAccess.class);

  public SqlDataAccess(final Element element) {
    super(element);
  }

  @Override
  public TableModel queryData() {

    logger.fatal("Not Implemented Yet!!");
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }


}
