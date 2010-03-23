package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.kettle.DynamicTransConfig;
import pt.webdetails.cda.utils.kettle.DynamicTransConfig.EntryType;
import pt.webdetails.cda.utils.kettle.DynamicTransMetaConfig;
import pt.webdetails.cda.utils.kettle.DynamicTransMetaConfig.Type;
import pt.webdetails.cda.utils.kettle.DynamicTransformation;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;
import pt.webdetails.cda.utils.kettle.RowProductionManager;
import pt.webdetails.cda.utils.kettle.TableModelInput;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class JoinCompoundDataAccess extends CompoundDataAccess implements RowProductionManager
{

  private static final Log logger = LogFactory.getLog(JoinCompoundDataAccess.class);
  private static final String TYPE = "join";

  private String leftId;
  private String rightId;
  private String[] leftKeys;
  private String[] rightKeys;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

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

  protected TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException
  {
    TableModel output = null;
    inputCallables.clear();

    try
    {
      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(queryOptions);
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(queryOptions);


      String[] leftColumnNames = new String[leftKeys.length];
      for (int i = 0; i < leftKeys.length; i++)
      {
        leftColumnNames[i] = tableModelA.getColumnName(Integer.parseInt(leftKeys[i]));
      }

      String[] rightColumnNames = new String[rightKeys.length];
      for (int i = 0; i < rightKeys.length; i++)
      {
        rightColumnNames[i] = tableModelB.getColumnName(Integer.parseInt(rightKeys[i]));
      }

      String sortLeftXML = getSortXmlStep("sortLeft", leftColumnNames);
      String sortRightXML = getSortXmlStep("sortRight", rightColumnNames);

      StringBuilder mergeJoinXML = new StringBuilder(
          "<step><name>mergeJoin</name><type>MergeJoin</type><join_type>FULL OUTER</join_type><step1>sortLeft</step1><step2>sortRight</step2>");
      mergeJoinXML.append("<keys_1>");

      for (int i = 0; i < leftKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(leftColumnNames[i]).append("</key>");
      }
      mergeJoinXML.append("</keys_1><keys_2>");
      for (int i = 0; i < rightKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(rightColumnNames[i]).append("</key>");
      }
      mergeJoinXML.append("</keys_2></step>");

      DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(Type.EMPTY, "JoinCompoundData", null, null);
      DynamicTransConfig transConfig = new DynamicTransConfig();

      transConfig.addConfigEntry(EntryType.STEP, "input1", "<step><name>input1</name><type>Injector</type></step>");
      transConfig.addConfigEntry(EntryType.STEP, "input2", "<step><name>input2</name><type>Injector</type></step>");
      transConfig.addConfigEntry(EntryType.STEP, "sortLeft", sortLeftXML);
      transConfig.addConfigEntry(EntryType.STEP, "sortRight", sortRightXML);
      transConfig.addConfigEntry(EntryType.STEP, "mergeJoin", mergeJoinXML.toString());

      transConfig.addConfigEntry(EntryType.HOP, "input1", "sortLeft");
      transConfig.addConfigEntry(EntryType.HOP, "input2", "sortRight");
      transConfig.addConfigEntry(EntryType.HOP, "sortLeft", "mergeJoin");
      transConfig.addConfigEntry(EntryType.HOP, "sortRight", "mergeJoin");

      TableModelInput input1 = new TableModelInput();
      transConfig.addInput("input1", input1);
      inputCallables.add(input1.getCallableRowProducer(tableModelA, true));
      TableModelInput input2 = new TableModelInput();
      transConfig.addInput("input2", input2);
      inputCallables.add(input2.getCallableRowProducer(tableModelB, true));

      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
      transConfig.addOutput("mergeJoin", outputListener);

      DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
      trans.executeCheckedSuccess(null, null, this);
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


  private String getSortXmlStep(final String name, final String[] columnNames)
  {

    StringBuilder sortXML = new StringBuilder(
        "  <step>\n" +
            "    <name>" + name + "</name>\n" +
            "    <type>SortRows</type>\n" +
            "    <description/>\n" +
            "    <distribute>Y</distribute>\n" +
            "    <copies>1</copies>\n" +
            "         <partitioning>\n" +
            "           <method>none</method>\n" +
            "           <schema_name/>\n" +
            "           </partitioning>\n" +
            "      <directory>%%java.io.tmpdir%%</directory>\n" +
            "      <prefix>out</prefix>\n" +
            "      <sort_size/>\n" +
            "      <free_memory>25</free_memory>\n" +
            "      <compress>N</compress>\n" +
            "      <compress_variable/>\n" +
            "      <unique_rows>N</unique_rows>\n" +
            "    <fields>\n");

    for (int i = 0; i < columnNames.length; i++)
    {
      sortXML.append("      <field>\n" +
          "        <name>" + columnNames[i] + "</name>\n" +
          "        <ascending>Y</ascending>\n" +
          "        <case_sensitive>N</case_sensitive>\n" +
          "      </field>\n");
    }

    sortXML.append("    </fields>\n" +
        "     <cluster_schema/>\n" +
        " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n" +
        "      <xloc>615</xloc>\n" +
        "      <yloc>188</yloc>\n" +
        "      <draw>Y</draw>\n" +
        "      </GUI>\n" +
        "    </step>\n");

    return sortXML.toString();
  }


  public void startRowProduction()
  {
    long timeout = Long.parseLong(CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeout"));
    TimeUnit unit = TimeUnit.valueOf(CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit"));
    startRowProduction(timeout, unit);
  }


  public void startRowProduction(long timeout, TimeUnit unit)
  {
    try
    {
      List<Future<Boolean>> results = executorService.invokeAll(inputCallables, timeout, unit);
      for (Future<Boolean> result : results)
      {
        result.get();
      }
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (ExecutionException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

