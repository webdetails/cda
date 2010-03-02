package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.kettle.kettle.KettleUtils;
import pt.webdetails.cda.utils.kettle.kettle.RowMetaToTableModel;
import pt.webdetails.cda.utils.kettle.kettle.TableModelInput;

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
  private String[] leftKeys;
  private String[] rightKeys;

  public JoinCompoundDataAccess(final Element element)
  {
    super(element);

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

    leftKeys = left.attributeValue("keys").split(",");
    rightKeys = right.attributeValue("keys").split(",");
  }


  public String getType()
  {
    return TYPE;
  }


  protected TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException
  {
    TableModel output = null;

    try
    {
      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(new QueryOptions());
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(new QueryOptions());

      logger.warn("TODO - Grab the correct keys");
      final String[] leftColumns = new String[leftKeys.length];
      final String[] rightColumns = new String[leftKeys.length];

      for (int i = 0; i < leftKeys.length; i++)
      {
        leftColumns[i] = tableModelA.getColumnName(Integer.parseInt(leftKeys[i]));
      }

      for (int i = 0; i < rightKeys.length; i++)
      {
        rightColumns[i] = tableModelB.getColumnName(Integer.parseInt(rightKeys[i]));
      }


      TransMeta transMeta = KettleUtils.initTransMeta("JoinCompoundData");

      String input1Name = "input1";
      TableModelInput input1Meta = new TableModelInput(input1Name);
      StepMeta input1 = input1Meta.getStepMeta();
      transMeta.addStep(input1);

      String input2Name = "input2";
      TableModelInput input2Meta = new TableModelInput(input2Name);
      StepMeta input2 = input2Meta.getStepMeta();
      transMeta.addStep(input2);

      String mergeJoinName = "mergeJoin";
      MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
      mergeJoinMeta.setStepMeta1(input1);
      mergeJoinMeta.setKeyFields1(leftColumns);
      mergeJoinMeta.setStepMeta2(input2);
      mergeJoinMeta.setKeyFields2(rightColumns);
      mergeJoinMeta.setJoinType("INNER");
      StepMeta mergeJoin = new StepMeta(StepLoader.getInstance().getStepPluginID(mergeJoinMeta), mergeJoinName, mergeJoinMeta);
      transMeta.addStep(mergeJoin);

      transMeta.addTransHop(new TransHopMeta(input1, mergeJoin));
      transMeta.addTransHop(new TransHopMeta(input2, mergeJoin));

      Trans trans = new Trans(transMeta);
      trans.prepareExecution(null);

      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);

      StepInterface si = trans.getStepInterface(mergeJoinName, 0);
      si.addRowListener(outputListener);

      input1Meta.connectRowProducer(trans);
      input2Meta.connectRowProducer(trans);

      trans.startThreads();

      // Need to produce rows in another thread, otherwise, we could deadlock
      ExecutorService executorService = Executors.newCachedThreadPool();
      Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();
      inputCallables.add(input1Meta.produceRows(tableModelA));
      inputCallables.add(input2Meta.produceRows(tableModelB));
      executorService.invokeAll(inputCallables);

      trans.waitUntilFinished();

      output = outputListener.getRowsWritten();
    }
    catch (UnknownDataAccessException e)
    {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    }
    catch (Exception e)
    {
      throw new QueryException("Exception during query ", e);
    }

    return output;
  }
}
