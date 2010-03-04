package pt.webdetails.cda.utils.kettle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.JndiUtil;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * A dynamically generated Kettle transformation. The transformation is composed
 * of:
 * <ul>
 * <li>the TRANS entry (with optional XML definition)
 * <li>a map of step names to XML definitions (optional if they are defined in
 * the TRANS XML)
 * <li>a map of step names to step error handling XML definitions (optional)
 * <li>a map of source step name to destination step name hops (optional if they
 * are defined in the TRANS XML)
 * <li>a map of variable names to values (optional)
 * <li>a map of parameter names to default values (optional)
 * <li>a map of step names to RowProducers (optional but used in the typical
 * scenario)
 * <li>a map of step names to RowListeners (optional but used in the typical
 * scenario)
 * 
 * @author Daniel Einspanjer
 */
public class DynamicTrans
{
  /**
   * Enumeration of the possible states of this DynamicTrans
   */
  public enum State {
    INVALID, CREATED, RUNNING, FINISHED_SUCCESS, FINISHED_ERROR
  }

  private State state = State.INVALID;

  public State getState()
  {
    return state;
  }

  private final TransMeta          transMeta;
  private final Trans              trans;
  private final DynamicTransConfig transConfig;
  private int                      secondsDuration;
  private Result                   result;

  static {
    EnvUtil.environmentInit();
    JndiUtil.initJNDI();
    try {
      StepLoader.init();
    } catch (final KettleException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Construct a Kettle Transformation based on the given config
   * 
   * @param transConfig
   *          a DynamicTransConfig that will be permanently frozen during
   *          construction of the DynamicTrans
   * @param transMetaConfig
   *          describes the source and settings of the transMeta
   * @throws KettleXMLException
   *           if one of the XML snippits in the config is invalid according to
   *           Kettle
   */
  public DynamicTrans(final DynamicTransConfig transConfig, final DynamicTransMetaConfig transMetaConfig) throws KettleException
  {
    if (transConfig == null) throw new IllegalArgumentException("config is null");
    transConfig.freeze();
    this.transConfig = transConfig;

    if (transMetaConfig == null) throw new IllegalArgumentException("config is null");

    final VariableSpace parentVariableSpace = Variables.getADefaultVariableSpace();
    parentVariableSpace.injectVariables(transConfig.getFrozenVariableConfigEntries());

    transMeta = transMetaConfig.getTransMeta(parentVariableSpace);

    for (final Entry<String, String> entry : transConfig.getFrozenStepConfigEntries().entrySet()) {
      final StepMeta stepMeta = new StepMeta(XMLHandler.getSubNode(XMLHandler.loadXMLString(entry.getValue()), StepMeta.XML_TAG), transMeta.getDatabases(),
          transMeta.getCounters());
      transMeta.addOrReplaceStep(stepMeta);
    }

    final List<StepMeta> steps = transMeta.getSteps();
    for (final Entry<String, String> entry : transConfig.getFrozenStepErrorHandlingConfigEntries().entrySet()) {
      final StepErrorMeta stepErrorMeta = new StepErrorMeta(transMeta,
          XMLHandler.getSubNode(XMLHandler.loadXMLString(entry.getValue()), StepErrorMeta.XML_TAG), steps);
      stepErrorMeta.getSourceStep().setStepErrorMeta(stepErrorMeta);
    }

    for (final StepMeta stepMeta : steps) {
      final StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if (sii != null) {
        sii.searchInfoAndTargetSteps(steps);
      }

    }

    for (final Entry<String, String> entry : transConfig.getFrozenHopConfigEntries().entrySet()) {
      final TransHopMeta hop = new TransHopMeta(transMeta.findStep(entry.getKey()), transMeta.findStep(entry.getValue()));
      transMeta.addTransHop(hop);
    }

    trans = new Trans(transMeta);
    state = State.CREATED;
  }

  /**
   * @return current status of the Transformation as reported by Kettle
   */
  public String getStatus()
  {
    return trans.getStatus();
  }

  public void execute(final String[] arguments, final Map<String, String> parameters, final RowProductionManager rowProductionManager) throws KettleException
  {
    if (rowProductionManager == null) throw new IllegalArgumentException("rowProductionManager is null");

    final long startMillis = System.currentTimeMillis();

    if (parameters != null) {
      for (final Entry<String, String> entry : parameters.entrySet()) {
        trans.setParameterValue(entry.getKey(), entry.getValue());
      }
    }

    trans.prepareExecution(arguments);

    for (final Entry<String, RowListener> entry : transConfig.getFrozenOutputs().entrySet()) {
      final StepInterface si = trans.getStepInterface(entry.getKey(), 0);
      si.addRowListener(entry.getValue());
    }

    for (final Entry<String, RowProducerBridge> entry : transConfig.getFrozenInputs().entrySet()) {
      final RowProducerBridge bridge = entry.getValue();
      bridge.setRowProducer(trans.addRowProducer(entry.getKey(), 0));
    }

    trans.startThreads();

    rowProductionManager.startRowProduction();

    trans.waitUntilFinished();
    result = trans.getResult();
    secondsDuration = (int) (System.currentTimeMillis() - startMillis);
  }

  public String getReadWriteThroughput()
  {
    String throughput = null;
    if (secondsDuration != 0) {
      String readClause = null, writtenClause = null;
      if (result.getNrLinesRead() > 0) {
        readClause = String.format("lines read: %d ( %d lines/s)", result.getNrLinesRead(), (result.getNrLinesRead() / secondsDuration));
      }
      if (result.getNrLinesWritten() > 0) {
        writtenClause = String.format("%slines written: %d ( %d lines/s)", (result.getNrLinesRead() > 0 ? "; " : ""), result.getNrLinesWritten(), (result
            .getNrLinesWritten() / secondsDuration));
      }
      if (readClause != null || writtenClause != null) {
        throughput = String.format("Transformation %s%s", (result.getNrLinesRead() > 0 ? readClause : ""),
            (result.getNrLinesWritten() > 0 ? writtenClause : ""));
      }
    }
    return throughput;
  }
}
