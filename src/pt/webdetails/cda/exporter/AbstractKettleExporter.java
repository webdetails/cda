package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.utils.kettle.DynamicTransConfig;
import pt.webdetails.cda.utils.kettle.DynamicTransMetaConfig;
import pt.webdetails.cda.utils.kettle.DynamicTransformation;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;
import pt.webdetails.cda.utils.kettle.RowProductionManager;
import pt.webdetails.cda.utils.kettle.TableModelInput;

/**
 * Generic Kettle class to handle exports
 * User: pedro
 * Date: Mar 12, 2010
 * Time: 3:01:27 PM
 */
public abstract class AbstractKettleExporter implements Exporter, RowProductionManager
{
  
  private static final Log logger = LogFactory.getLog(AbstractKettleExporter.class);

  protected ExecutorService executorService = Executors.newCachedThreadPool();
  protected Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ");
  private String filename;


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


  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException  {

    TableModel output = null;
    inputCallables.clear();

    try
    {


      final DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(DynamicTransMetaConfig.Type.EMPTY, "Exporter", null, null);
      final DynamicTransConfig transConfig = new DynamicTransConfig();

      transConfig.addConfigEntry(DynamicTransConfig.EntryType.STEP, "input", "<step><name>input</name><type>Injector</type></step>");
      transConfig.addConfigEntry(DynamicTransConfig.EntryType.STEP, "export", getExportStepDefinition("export"));

      transConfig.addConfigEntry(DynamicTransConfig.EntryType.HOP, "input", "export");

      TableModelInput input = new TableModelInput();
      transConfig.addInput("input", input);
      inputCallables.add(input.getCallableRowProducer(tableModel, true));



      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
      transConfig.addOutput("export", outputListener);

      DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
      trans.executeCheckedSuccess(null, null, this);
      logger.info(trans.getReadWriteThroughput());


      // Transformation executed ok, lets return the file

      output = outputListener.getRowsWritten();
    }
    catch (KettleException e)
    {
      throw new ExporterException("Kettle exception during " + getType() +  " query ", e);
    }
    catch (Exception e)
    {
      throw new ExporterException("Unknown exception during " + getType() +  " query ", e);
    }


  }


  protected String getFileName(){

    filename = "pentaho-cda-" + getType() + "-" + dateFormat.format(Calendar.getInstance().getTime()) + "-" + UUID.randomUUID().toString();
    return filename;

  }

  protected abstract String getExportStepDefinition(String name);


  public abstract String getMimeType();

  public abstract String getType();

}
