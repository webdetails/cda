/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.robochef;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;

import org.pentaho.di.core.logging.CentralLogStore;


import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * A dynamically generated Kettle transformation. The transformation is composed of: <ul> <li>the TRANS entry (with
 * optional XML definition) <li>a map of step names to XML definitions (optional if they are defined in the TRANS XML)
 * <li>a map of step names to step error handling XML definitions (optional) <li>a map of source step name to
 * destination step name hops (optional if they are defined in the TRANS XML) <li>a map of variable names to values
 * (optional) <li>a map of parameter names to default values (optional) <li>a map of step names to RowProducers
 * (optional but used in the typical scenario) <li>a map of step names to RowListeners (optional but used in the typical
 * scenario)
 *
 * @author Daniel Einspanjer
 */
public class DynamicTransformation {
  /**
   * Enumeration of the possible states of this DynamicTransformation
   */
  public enum State {
    INVALID, CREATED, RUNNING, FINISHED_SUCCESS, FINISHED_ERROR
  }

  private State state = State.INVALID;

  public State getState() {
    return state;
  }

  private final TransMeta transMeta;
  private final Trans trans;
  private final DynamicTransConfig transConfig;
  private int secondsDuration;
  private Result result;
  private Collection<Callable<Boolean>> inputCallables;


  private static boolean hasCheckedForMethod = false;
  private static Method logCleaningMethod = null;
  private static boolean isInitialized = false;
  private static boolean isInitializedWithJDNI = false;

  public static synchronized void init( boolean initializeJNDI ) {
    if ( !isInitialized ) {
      try {
        KettleEnvironment.init( initializeJNDI );
        isInitialized = true;
        isInitializedWithJDNI = initializeJNDI;
      } catch ( final KettleException e ) {
        throw new IllegalStateException( e );
      }
    }
    if ( initializeJNDI && !isInitializedWithJDNI ) {
      throw new IllegalStateException(
        "DynamicTransformation was already initialized without JNDI. Call init(true) before new DynamicTransformation"
					+ "()" );
    }
  }

  /**
   * Construct a Kettle Transformation based on the given config
   *
   * @param transConfig     a DynamicTransConfig that will be permanently frozen during construction of the
   *                        DynamicTransformation
   * @param transMetaConfig describes the source and settings of the transMeta
   * @param inputCallables  Collection of callables RowProducers
   * @throws KettleXMLException if one of the XML snippits in the config is invalid according to Kettle
   */
  public DynamicTransformation( final DynamicTransConfig transConfig, final DynamicTransMetaConfig transMetaConfig,
                                Collection<Callable<Boolean>> inputCallables )
    throws KettleException {
    DynamicTransformation.init( false );
    if ( transConfig == null ) {
      throw new IllegalArgumentException( "config is null" );
    }
    transConfig.freeze();
    this.transConfig = transConfig;

    if ( transMetaConfig == null ) {
      throw new IllegalArgumentException( "config is null" );
    }

    final VariableSpace parentVariableSpace = Variables.getADefaultVariableSpace();
    parentVariableSpace.injectVariables( transConfig.getFrozenVariableConfigEntries() );

    transMeta = transMetaConfig.getTransMeta( parentVariableSpace );

    if ( inputCallables == null ) {
      throw new IllegalArgumentException( "inputCallables is null" );
    }
    this.inputCallables = inputCallables;

    for ( final Entry<String, String> entry : transConfig.getFrozenStepConfigEntries().entrySet() ) {
      final StepMeta stepMeta = new StepMeta( XMLHandler.getSubNode( XMLHandler.loadXMLString( entry.getValue() ),
        StepMeta.XML_TAG ), transMeta.getDatabases(), transMeta.getCounters() );
      transMeta.addOrReplaceStep( stepMeta );
    }

    final List<StepMeta> steps = transMeta.getSteps();
    for ( final Entry<String, String> entry : transConfig.getFrozenStepErrorHandlingConfigEntries().entrySet() ) {
      final StepErrorMeta stepErrorMeta = new StepErrorMeta( transMeta, XMLHandler.getSubNode( XMLHandler
        .loadXMLString( entry.getValue() ), StepErrorMeta.XML_TAG ), steps );
      stepErrorMeta.getSourceStep().setStepErrorMeta( stepErrorMeta );
    }

    for ( final StepMeta stepMeta : steps ) {
      final StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if ( sii != null ) {
        sii.searchInfoAndTargetSteps( steps );
      }

    }

    for ( final Entry<String, String> entry : transConfig.getFrozenHopConfigEntries().entrySet() ) {
      final TransHopMeta hop = new TransHopMeta( transMeta.findStep( entry.getKey() ), transMeta.findStep( entry
        .getValue() ) );
      transMeta.addTransHop( hop );
    }

    trans = new Trans( transMeta );
    trans.setLogLevel( LogLevel.NOTHING );
    state = State.CREATED;
  }

  /**
   * @return current status of the Transformation as reported by Kettle
   */
  public String getStatus() {
    return trans.getStatus();
  }

  public void executeCheckedSuccess( final String[] arguments, final Map<String, String> parameters,
                                     final RowProductionManager rowProductionManager ) throws KettleException {
    if ( !execute( arguments, parameters, rowProductionManager ) ) {
      throw new KettleException( String.format( "The transformation execution ended with state %s (%d errors)",
        state, result.getNrErrors() ) );
    }
  }

  public boolean execute( final String[] arguments, final Map<String, String> parameters,
                          final RowProductionManager rowProductionManager ) throws KettleException {
    if ( rowProductionManager == null ) {
      throw new IllegalArgumentException( "rowProductionManager is null" );
    }

    final long startMillis = System.currentTimeMillis();

    if ( parameters != null ) {
      for ( final Entry<String, String> entry : parameters.entrySet() ) {
        trans.setParameterValue( entry.getKey(), entry.getValue() );
      }
    }

    trans.prepareExecution( arguments );

    for ( final Entry<String, RowListener> entry : transConfig.getFrozenOutputs().entrySet() ) {
      final StepInterface si = trans.getStepInterface( entry.getKey(), 0 );
      si.addRowListener( entry.getValue() );
    }

    for ( final Entry<String, RowProducerBridge> entry : transConfig.getFrozenInputs().entrySet() ) {
      final RowProducerBridge bridge = entry.getValue();
      bridge.setRowProducer( trans.addRowProducer( entry.getKey(), 0 ) );
    }

    trans.startThreads();
    state = State.RUNNING;

    rowProductionManager.startRowProduction( inputCallables );

    trans.waitUntilFinished();
    secondsDuration = (int) ( System.currentTimeMillis() - startMillis );

    result = trans.getResult();

    if ( result.getNrErrors() == 0 ) {
      state = State.FINISHED_SUCCESS;
    } else {
      state = State.FINISHED_ERROR;
    }

    //Log cleaning
    trans.cleanup();
    cleanLogs( trans.getLogChannelId() );
    cleanLogs( transMeta.getLogChannelId() );


    return state == State.FINISHED_SUCCESS;
  }


  private void cleanLogs( String logChannelId ) {
    CentralLogStore.init();
    CentralLogStore.discardLines( logChannelId, true );
    // Remove the entries from the registry

    synchronized( this.getClass() ) {
      if ( !hasCheckedForMethod ) {
        hasCheckedForMethod = true;
        Class c = LoggingRegistry.class;
        Class parTypes[] = new Class[ 1 ];
        parTypes[ 0 ] = String.class;
        try {
          logCleaningMethod = c.getDeclaredMethod( "removeIncludingChildren", parTypes );
        } catch ( NoSuchMethodException nsme ) {
          //Kettle 4.1 or lower - do nothing
          logCleaningMethod = null;
        }
      }
    }


    if ( logCleaningMethod != null ) {
      Object parameters[] = new Object[ 1 ];
      parameters[ 0 ] = logChannelId;
      try {
        logCleaningMethod.invoke( LoggingRegistry.getInstance(), parameters );
      } catch ( IllegalArgumentException ex ) {
      } catch ( InvocationTargetException ex ) {
      } catch ( IllegalAccessException ilae ) {
      }
    }

    //		LoggingRegistry.getInstance().  removeIncludingChildren(logChannelId);
  }


  public String getReadWriteThroughput() {
    String throughput = null;
    if ( secondsDuration != 0 ) {
      String readClause = null, writtenClause = null;
      if ( result.getNrLinesRead() > 0 ) {
        readClause = String.format( "lines read: %d ( %d lines/s)", result.getNrLinesRead(), ( result
          .getNrLinesRead() / secondsDuration ) );
      }
      if ( result.getNrLinesWritten() > 0 ) {
        writtenClause = String.format( "%slines written: %d ( %d lines/s)", ( result.getNrLinesRead() > 0 ? "; "
          : "" ), result.getNrLinesWritten(), ( result.getNrLinesWritten() / secondsDuration ) );
      }
      if ( readClause != null || writtenClause != null ) {
        throughput = String.format( "Transformation %s%s", ( result.getNrLinesRead() > 0 ? readClause : "" ),
          ( result.getNrLinesWritten() > 0 ? writtenClause : "" ) );
      }
    }
    return throughput;
  }
}
