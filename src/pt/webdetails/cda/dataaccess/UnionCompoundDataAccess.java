package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
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
public class UnionCompoundDataAccess extends CompoundDataAccess
{

  private static final Log logger = LogFactory.getLog(UnionCompoundDataAccess.class);
  private static final String TYPE = "join";
  private String leftId;
  private String rightId;


  public UnionCompoundDataAccess(final Element element)
  {
    super(element);

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

  }

  public String getType()
  {
    return TYPE;
  }

  protected TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException
  {


    try
    {

      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(new QueryOptions());
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(new QueryOptions());

      return TableModelUtils.getInstance().appendTableModel(tableModelA,tableModelB);

    }
    catch (UnknownDataAccessException e)
    {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    }


  }
}
