package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * Class to join 2 datatables
 * Created by Pedro Alves
 * User: pedro
 * Date: Mar 9, 2010
 * Time: 1:13:11 PM
 */
public class UnionCompoundDataAccess extends CompoundDataAccess {

  private static final Log logger = LogFactory.getLog(UnionCompoundDataAccess.class);
  private static final String TYPE = "union";
  private String leftId;
  private String rightId;

  public UnionCompoundDataAccess() {
  }

  public UnionCompoundDataAccess(final Element element) {
    super(element);

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

  }

  public String getType() {
    return TYPE;
  }

  protected TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException {


    try {

      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(queryOptions);
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(queryOptions);

      return TableModelUtils.getInstance().appendTableModel(tableModelA, tableModelB);

    } catch (UnknownDataAccessException e) {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    }


  }

  @Override
  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    //properties.add(new PropertyDescriptor("query", PropertyDescriptor.Type.STRING,PropertyDescriptor.Placement.ATTRIB));
    return properties;
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }
}
