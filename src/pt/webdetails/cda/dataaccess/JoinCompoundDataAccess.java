package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 11:38:19 PM
 */
public class JoinCompoundDataAccess extends CompoundDataAccess
{

  private static final Log logger = LogFactory.getLog(SqlDataAccess.class);
  private static final String TYPE = "sql";

  private String leftId;
  private String rightId;
  private int[] leftKeys;
  private int[] rightKeys;

  public JoinCompoundDataAccess(final Element element)
  {
    super(element);

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = left.attributeValue("id");

    logger.warn("TODO - Grab the correct keys");
    leftKeys = new int[]{0};
    rightKeys = new int[]{0};
        
  }


  public String getType()
  {
    return TYPE;
  }


  protected TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException
  {

    // get Left table model
    logger.warn("TODO - Pass the correct queryOptions");
    try
    {
      TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(new QueryOptions());
      TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(new QueryOptions());

      // Daniel, do your magic
      logger.info("Daniel, do your magic...");

    }
    catch (UnknownDataAccessException e)
    {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    }


    // get Right table model


    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
