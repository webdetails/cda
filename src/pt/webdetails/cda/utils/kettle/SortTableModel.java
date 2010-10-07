/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.utils.kettle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.TableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransConfig.EntryType;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransMetaConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransMetaConfig.Type;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransformation;
import plugins.org.pentaho.di.robochef.kettle.RowProductionManager;
import plugins.org.pentaho.di.robochef.kettle.TableModelInput;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.utils.Util;

/**
 *
 * @author pedro
 */
public class SortTableModel implements RowProductionManager
{

  private static final Log logger = LogFactory.getLog(SortTableModel.class);
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();
  
  private static final long DEFAULT_ROW_PRODUCTION_TIMEOUT = 120;
  private static final TimeUnit DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT = TimeUnit.SECONDS;


  public SortTableModel()
  {
  }


  public TableModel doSort(TableModel unsorted, ArrayList<String> sortBy) throws SortException
  {

    if (unsorted == null || unsorted.getRowCount() == 0)
    {
      return unsorted;
    }
    else
    {
      TableModel output = null;
      inputCallables.clear();


      try
      {

        String sort = getSortXmlStep(unsorted, sortBy);

        DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(Type.EMPTY, "JoinCompoundData", null, null);
        DynamicTransConfig transConfig = new DynamicTransConfig();

        transConfig.addConfigEntry(EntryType.STEP, "input", "<step><name>input</name><type>Injector</type></step>");
        transConfig.addConfigEntry(EntryType.STEP, "sort", sort);
        transConfig.addConfigEntry(EntryType.HOP, "input", "sort");

        TableModelInput input = new TableModelInput();
        transConfig.addInput("input", input);
        inputCallables.add(input.getCallableRowProducer(unsorted, true));


        RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
        transConfig.addOutput("sort", outputListener);

        DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
        trans.executeCheckedSuccess(null, null, this);
        logger.info(trans.getReadWriteThroughput());
        output = outputListener.getRowsWritten();

        return output;

      }
      catch (Exception e)
      {
        throw new SortException("Exception during sorting ", e);
      }
    }


  }


  public void startRowProduction()
  {
    String timeoutStr = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeout");
    long timeout = Util.isNullOrEmpty(timeoutStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT : Long.parseLong(timeoutStr);
    String unitStr =  CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit");
    TimeUnit unit = Util.isNullOrEmpty(unitStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT : TimeUnit.valueOf(unitStr);
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


  private String getSortXmlStep(TableModel unsorted, ArrayList<String> sortBy) throws SortException
  {

    StringBuilder sortXML = new StringBuilder(
            "  <step>\n"
            + "    <name>sort</name>\n"
            + "    <type>SortRows</type>\n"
            + "    <description/>\n"
            + "    <distribute>Y</distribute>\n"
            + "    <copies>1</copies>\n"
            + "         <partitioning>\n"
            + "           <method>none</method>\n"
            + "           <schema_name/>\n"
            + "           </partitioning>\n"
            + "      <directory>%%java.io.tmpdir%%</directory>\n"
            + "      <prefix>out</prefix>\n"
            + "      <sort_size>1000000</sort_size>\n"
            + "      <free_memory>25</free_memory>\n"
            + "      <compress>N</compress>\n"
            + "      <compress_variable/>\n"
            + "      <unique_rows>N</unique_rows>\n"
            + "    <fields>\n");


    for (String s : sortBy)
    {
      SortDescriptor sort = new SortDescriptor((s));

      sortXML.append("      <field>\n"
              + "        <name>" + unsorted.getColumnName(sort.getIndex()) + "</name>\n"
              + "        <ascending>" + sort.getIsAscendingString() + "</ascending>\n"
              + "        <case_sensitive>N</case_sensitive>\n"
              + "      </field>\n");

    }


    sortXML.append("    </fields>\n"
            + "     <cluster_schema/>\n"
            + " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n"
            + "      <xloc>615</xloc>\n"
            + "      <yloc>188</yloc>\n"
            + "      <draw>Y</draw>\n"
            + "      </GUI>\n"
            + "    </step>\n");

    return sortXML.toString();
  }

  private class SortDescriptor
  {

    private Integer index;
    private String direction;
    private static final String REGEXP = "^(\\d+)([AD]?)$";
    Pattern p = Pattern.compile(REGEXP);


    public SortDescriptor(String sortBy) throws SortException
    {

      Matcher m = p.matcher(sortBy);
      if (m.matches())
      {
        // valid one
        index = Integer.parseInt(m.group(1));

        if (m.group(2).equals("D"))
        {
          setDirection("DESC");
        }
        else
        {
          setDirection("ASC");
        }

      }
      else
      {
        throw new SortException("Invalid searchBy option: " + sortBy, null);
      }


    }


    public String getIsAscendingString()
    {
      if (getDirection().equals("ASC"))
      {
        return "Y";
      }
      else
      {
        return "N";
      }
    }


    public String getDirection()
    {
      return direction;
    }


    public void setDirection(String direction)
    {
      this.direction = direction;
    }


    public Integer getIndex()
    {
      return index;
    }


    public void setIndex(Integer index)
    {
      this.index = index;
    }
  }
}
