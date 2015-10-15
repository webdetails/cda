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

package pt.webdetails.cda.exporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.kettle.DataAccessKettleAdapter;
import pt.webdetails.cda.dataaccess.kettle.KettleAdapterException;
import pt.webdetails.cda.utils.kettle.RowCountListener;
import pt.webdetails.robochef.DynamicTransConfig;
import pt.webdetails.robochef.DynamicTransMetaConfig;
import pt.webdetails.robochef.DynamicTransformation;
import pt.webdetails.robochef.RowProductionManager;
import pt.webdetails.robochef.TableModelInput;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * Direct exporter from data access to stream using Kettle
 *
 * @author Michael Spector
 */
public class DefaultStreamExporter implements RowProductionManager, StreamExporter {

  private static final Log logger = LogFactory.getLog( DefaultStreamExporter.class );
  private static long DEFAULT_ROW_PRODUCTION_TIMEOUT = 120;
  private static TimeUnit DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private DataAccessKettleAdapter dataAccess;
  private AbstractKettleExporter exporter;
  private ExecutorService executorService = Executors.newCachedThreadPool();

  public DefaultStreamExporter( AbstractKettleExporter exporter,
                                DataAccessKettleAdapter dataAccess ) {
    this.exporter = exporter;
    this.dataAccess = dataAccess;
  }

  @Override
  public void export( OutputStream out ) throws ExporterException {
    Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

    try {

      boolean hasFilter = false;
      boolean hasCalculatedColumns = dataAccess.hasCalculatedColumns();
      StepMeta filterStepMeta = null;

      DynamicTransConfig transConfig = new DynamicTransConfig();

      StepMeta dataAccessStepMeta = dataAccess.getKettleStepMeta( "DataAccess" );

      StepMeta injectorStepMeta = null;

      StepMeta formulaStepMeta = null;

      String[] parameterNames = dataAccess.getParameterNames();
      DataRow parameters = dataAccess.getParameters();
      if ( parameterNames.length > 0 ) {
        injectorStepMeta = new StepMeta( "Input", new InjectorMeta() );
        injectorStepMeta.setCopies( 1 );
        transConfig.addConfigEntry( DynamicTransConfig.EntryType.STEP, injectorStepMeta.getName(),
            injectorStepMeta.getXML() );
        if ( dataAccessStepMeta.getStepMetaInterface() instanceof TableInputMeta ) {
          ( (TableInputMeta) dataAccessStepMeta.getStepMetaInterface() ).setLookupFromStep( injectorStepMeta );
        }
      }

      transConfig.addConfigEntry( DynamicTransConfig.EntryType.STEP,
          dataAccessStepMeta.getName(), dataAccessStepMeta.getXML() );

      if ( dataAccess.getDataAccessOutputs().size() > 0 ) {
        hasFilter = true;
        String[] s = getStepFields( dataAccess, dataAccessStepMeta, parameterNames );
        filterStepMeta = dataAccess.getFilterStepMeta( "Filter", s /*, sqlDataAccess */ );
        transConfig.addConfigEntry( DynamicTransConfig.EntryType.STEP,
            filterStepMeta.getName(), filterStepMeta.getXML() );
      }

      StepMeta exportStepMeta = exporter.getExportStepMeta( "Export" );
      transConfig.addConfigEntry( DynamicTransConfig.EntryType.STEP,
          exportStepMeta.getName(), exportStepMeta.getXML() );

      if ( hasCalculatedColumns ) {
        formulaStepMeta = dataAccess.getFormulaStepMeta( "Formula" );
        transConfig.addConfigEntry( DynamicTransConfig.EntryType.STEP, formulaStepMeta.getName(),
            formulaStepMeta.getXML() );
      }

      if ( parameterNames.length > 0 ) {
        transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
            injectorStepMeta.getName(), dataAccessStepMeta.getName() );
      }

      if ( hasFilter == true ) {
        if ( hasCalculatedColumns ) {
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              dataAccessStepMeta.getName(), formulaStepMeta.getName() );
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              formulaStepMeta.getName(), filterStepMeta.getName() );
        } else {
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              dataAccessStepMeta.getName(), filterStepMeta.getName() );
        }
        transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
            filterStepMeta.getName(), exportStepMeta.getName() );
      } else {
        if ( hasCalculatedColumns ) {
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              dataAccessStepMeta.getName(), formulaStepMeta.getName() );
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              formulaStepMeta.getName(), exportStepMeta.getName() );
        } else {
          transConfig.addConfigEntry( DynamicTransConfig.EntryType.HOP,
              dataAccessStepMeta.getName(), exportStepMeta.getName() );
        }
      }

      // Prepare parameters as data of the injector step:
      if ( parameterNames.length > 0 ) {
        List<String> columnNames = new LinkedList<String>();
        List<Class<?>> columnClasses = new LinkedList<Class<?>>();
        List<Object> values = new LinkedList<Object>();
        for ( String parameterName : parameterNames ) {
          Object value = parameters.get( parameterName );
          if ( value instanceof Object[] ) {
            Object[] array = (Object[]) value;
            int arrayLength = array.length;
            if ( arrayLength > 0 ) {
              for ( int c = 0; c < arrayLength; ++c ) {
                columnNames.add( parameterName + "_" + c );
                columnClasses.add( array[ c ].getClass() );
                values.add( array[ c ] );
              }
            } else {
              columnNames.add( parameterName );
              columnClasses.add( Object.class );
              values.add( null );
            }
          } else {
            columnNames.add( parameterName );
            columnClasses.add( value == null ? Object.class : value.getClass() );
            values.add( value );
          }
        }

        TypedTableModel model = new TypedTableModel(
            columnNames.toArray( new String[ columnNames.size() ] ),
            columnClasses.toArray( new Class[ columnClasses.size() ] ) );
        model.addRow( values.toArray() );

        TableModelInput input = new TableModelInput();
        transConfig.addInput( injectorStepMeta.getName(), input );
        inputCallables.add( input.getCallableRowProducer( model, true ) );
      }

      RowCountListener countListener = new RowCountListener();
      transConfig.addOutput( exportStepMeta.getName(), countListener );

      ExtendedDynamicTransMetaConfig transMetaConfig = new ExtendedDynamicTransMetaConfig(
          DynamicTransMetaConfig.Type.EMPTY, "Streaming Exporter",
          null, null, dataAccess.getDatabases() );

      //     DynamicTransformation
      DynamicTransformation trans = new DynamicTransformation( transConfig, transMetaConfig, inputCallables );

      trans.executeCheckedSuccess( null, null, this );
      logger.info( trans.getReadWriteThroughput() );

      // Transformation executed ok, let's return the file
      exporter.copyFileToOutputStream( out );

      logger.debug( countListener.getRowsWritten() + " rows written." );

    } catch ( KettleAdapterException e ) {
      throw new ExporterException( "Data access to Kettle adapter exception during "
        + exporter.getType() + " query ", e );
    } catch ( KettleException e ) {
      throw new ExporterException( "Kettle exception during " + exporter.getType() + " query ", e );
    } catch ( Exception e ) {
      throw new ExporterException( "Unknown exception during " + exporter.getType() + " query ", e );
    }
  }

  private String[] getStepFields( DataAccessKettleAdapter dataAccess, StepMeta dataAccessStepMeta,
                                  String[] parameterNames ) throws KettleAdapterException, KettleException {
    ExtendedDynamicTransMetaConfig transMetaConfig = new ExtendedDynamicTransMetaConfig(
        DynamicTransMetaConfig.Type.EMPTY, "Streaming Exporter",
        null, null, dataAccess.getDatabases() );
    InjectorMeta injectorMeta = new InjectorMeta();
    //In order to correctly fetch the fields, we need to make sure we insert the parameters on to this InjectorMeta
    insertValuesIntoInjectorMeta( injectorMeta, parameterNames );

    StepMeta injectorStepMeta = new StepMeta( "Input", injectorMeta );

    TransMeta extTransMeta = transMetaConfig.getTransMeta( Variables.getADefaultVariableSpace() );
    extTransMeta.addStep( injectorStepMeta );

    return extTransMeta.
      getStepFields( dataAccessStepMeta ).getFieldNames();
  }

  private void insertValuesIntoInjectorMeta( InjectorMeta injectorMeta, String[] parameterNames ) {
    int size = parameterNames.length;
    //[CDA-112] - Making sure we can handle more than ten columns means we need to specify arrays for
    //type, length and precision, the content of the arrays won't actually matter at this point, we just need them to be
    //filled, so enough space can be allocated to the paramData Object array
    int[] tlp = new int[ size ];
    for ( int i = 0; i < size; i++ ) {
      tlp[ i ] = 0;
    }
    injectorMeta.setFieldname( parameterNames );
    injectorMeta.setType( tlp );
    injectorMeta.setLength( tlp );
    injectorMeta.setPrecision( tlp );
  }

  @Override
  public void startRowProduction( Collection<Callable<Boolean>> inputCallables ) {
    String timeoutStr = CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.DefaultRowProductionTimeout" );
    long timeout = StringUtil.isEmpty( timeoutStr ) ? DEFAULT_ROW_PRODUCTION_TIMEOUT : Long.parseLong( timeoutStr );
    String unitStr =
        CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit" );
    TimeUnit unit = StringUtil.isEmpty( unitStr ) ? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT : TimeUnit.valueOf( unitStr );
    startRowProduction( timeout, unit, inputCallables );
  }

  @Override
  public void startRowProduction( long timeout, TimeUnit unit, Collection<Callable<Boolean>> inputCallables ) {
    try {
      List<Future<Boolean>> results = executorService.invokeAll( inputCallables, timeout, unit );
      for ( Future<Boolean> result : results ) {
        result.get();
      }
    } catch ( InterruptedException e ) {
      logger.error( e );
    } catch ( ExecutionException e ) {
      logger.error( e );
    }
  }

  @Override
  public String getMimeType() {
    return exporter.getMimeType();
  }

  @Override
  public String getAttachmentName() {
    return exporter.getAttachmentName();
  }

  public static class ExtendedDynamicTransMetaConfig extends DynamicTransMetaConfig {

    private DatabaseMeta[] databases;

    public ExtendedDynamicTransMetaConfig( Type type, String name,
                                           String configDataSource, RepositoryConfig repoConfig,
                                           DatabaseMeta[] databases ) throws KettleException {
      super( type, name, configDataSource, repoConfig );
      this.databases = databases;
    }

    @Override
    protected TransMeta getTransMeta( VariableSpace variableSpace ) throws KettleException {
      TransMeta transMeta = super.getTransMeta( variableSpace );
      if ( databases != null ) {
        for ( DatabaseMeta database : databases ) {
          transMeta.addOrReplaceDatabase( database );
        }
      }
      return transMeta;
    }
  }
}
