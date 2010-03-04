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
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.kettle.DynamicTransformation;
import pt.webdetails.cda.utils.kettle.DynamicTransConfig;
import pt.webdetails.cda.utils.kettle.DynamicTransMetaConfig;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;
import pt.webdetails.cda.utils.kettle.RowProductionManager;
import pt.webdetails.cda.utils.kettle.TableModelInput;
import pt.webdetails.cda.utils.kettle.DynamicTransConfig.EntryType;
import pt.webdetails.cda.utils.kettle.DynamicTransMetaConfig.Type;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 11:38:19 PM
 */
public class JoinCompoundDataAccess extends CompoundDataAccess implements RowProductionManager
{

  private static final Log logger = LogFactory.getLog(JoinCompoundDataAccess.class);
  private static final String TYPE = "sql";

  private String leftId;
  private String rightId;
  private String[] leftKeys;
  private String[] rightKeys;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

  public JoinCompoundDataAccess(final Element element)
  {
    super(element);

    logger.warn("TODO - Verify that JoinCompoundDataAccess.TYPE is supposed to be 'sql'");

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

    leftKeys = left.attributeValue("keys").split(",");
    rightKeys = right.attributeValue("keys").split(",");
  }


  public String getType()
  {
    logger.warn("TODO - Verify that JoinCompoundDataAccess.TYPE is supposed to be 'sql'");
    return TYPE;
  }


  protected TableModel queryDataSource(final ParameterDataRow parameter) throws QueryException
  {
    TableModel output = null;
    inputCallables.clear();
    
    try
    {
      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(new QueryOptions());
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(new QueryOptions());

      logger.warn("TODO - Grab the correct keys");

      StringBuilder mergeJoinXML = new StringBuilder("<step><name>mergeJoin</name><type>MergeJoin</type><join_type>INNER</join_type><step1>input1</step1><step2>input2</step2>");
      mergeJoinXML.append("<keys_1>");
      for (int i = 0; i < leftKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(tableModelA.getColumnName(Integer.parseInt(leftKeys[i]))).append("</key>");
      }
      mergeJoinXML.append("</keys_1><keys_2>");
      for (int i = 0; i < rightKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(tableModelB.getColumnName(Integer.parseInt(rightKeys[i]))).append("</key>");
      }
      mergeJoinXML.append("</keys_2></step>");

      DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(Type.EMPTY, "JoinCompoundData", null, null);
      DynamicTransConfig transConfig = new DynamicTransConfig();
      
      transConfig.addConfigEntry(EntryType.STEP, "input1", "<step><name>input1</name><type>Injector</type></step>");
      transConfig.addConfigEntry(EntryType.STEP, "input2", "<step><name>input2</name><type>Injector</type></step>");
      transConfig.addConfigEntry(EntryType.STEP, "mergeJoin", mergeJoinXML.toString());

      transConfig.addConfigEntry(EntryType.HOP, "input1", "mergeJoin");
      transConfig.addConfigEntry(EntryType.HOP, "input2", "mergeJoin");
      
      TableModelInput input1 = new TableModelInput();
      transConfig.addInput("input1", input1);
      inputCallables.add(input1.getCallableRowProducer(tableModelA, true));
      TableModelInput input2 = new TableModelInput();
      transConfig.addInput("input2", input2);
      inputCallables.add(input2.getCallableRowProducer(tableModelB, true));
      
      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
      transConfig.addOutput("mergeJoin", outputListener);

      DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
      trans.execute(null, null, this);
      logger.info(trans.getReadWriteThroughput());
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

  @Override
  public void startRowProduction()
  {
    try
    {
      executorService.invokeAll(inputCallables);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
