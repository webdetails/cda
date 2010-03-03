package pt.webdetails.cda.utils.kettle.kettle;

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
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * A dynamically generated Kettle transformation.
 * The transformation is composed of:
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
  public enum State
  {
    INVALID, CREATED, RUNNING, FINISHED_SUCCESS, FINISHED_ERROR
  }

  private State state = State.INVALID;

  public State getState()
  {
    return state;
  }

  private TransMeta transMeta;
  private Trans     trans;

  /**
   * Construct a Kettle Transformation based on the given config
   * 
   * @param config
   *          a DynamicTransConfig that will be permanently frozen during
   *          construction of the DynamicTrans
   * @throws KettleXMLException
   *           if one of the XML snippits in the config is invalid according to
   *           Kettle
   */
  public DynamicTrans(DynamicTransConfig config) throws KettleException
  {
    if (config == null) { throw new IllegalArgumentException("config is null"); }
    config.freeze();

    EnvUtil.environmentInit();
    JndiUtil.initJNDI();
    StepLoader.init();

    VariableSpace parentVariableSpace = Variables.getADefaultVariableSpace();
    parentVariableSpace.injectVariables(config.getFrozenVariableConfigEntries());

    transMeta = new TransMeta(parentVariableSpace);

    Entry<String, String> transConfig = config.getFrozenTransConfigEntry();
    transMeta.setName(transConfig.getKey());

    if (transConfig.getValue() != null)
    {
      transMeta.loadXML( 
          XMLHandler.getSubNode(XMLHandler.loadXMLString(transConfig.getValue()), "transformation"),
          null, true, parentVariableSpace);
    }

    for (Entry<String, String> entry : config.getFrozenStepConfigEntries().entrySet())
    {
      StepMeta stepMeta = new StepMeta(XMLHandler.getSubNode(XMLHandler.loadXMLString(entry.getValue()), StepMeta.XML_TAG), transMeta.getDatabases(), transMeta
          .getCounters());
      transMeta.addOrReplaceStep(stepMeta);
    }

    final List<StepMeta> steps = transMeta.getSteps();
    for (Entry<String, String> entry : config.getFrozenStepErrorHandlingConfigEntries().entrySet())
    {
      StepErrorMeta stepErrorMeta = new StepErrorMeta(transMeta, XMLHandler.getSubNode(XMLHandler.loadXMLString(entry.getValue()), StepErrorMeta.XML_TAG),
          steps);
      stepErrorMeta.getSourceStep().setStepErrorMeta(stepErrorMeta);
    }

    for (StepMeta stepMeta : steps)
    {
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if (sii != null) sii.searchInfoAndTargetSteps(steps);

    }

    for (Entry<String, String> entry : config.getFrozenHopConfigEntries().entrySet())
    {
      TransHopMeta hop = new TransHopMeta(transMeta.findStep(entry.getKey()), transMeta.findStep(entry.getValue()));
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

  public Result execute(String[] arguments, Map<String, String> parameters) throws KettleException
  {
    for (Entry<String, String> entry : parameters.entrySet())
    {
      trans.setParameterValue(entry.getKey(), entry.getValue());
    }
    trans.prepareExecution(arguments);

    return trans.getResult();
  }
}
