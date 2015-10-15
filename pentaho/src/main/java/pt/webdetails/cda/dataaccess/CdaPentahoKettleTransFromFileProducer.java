/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cda.dataaccess;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.platform.plugin.RepositoryResourceLoader;

import javax.swing.table.TableModel;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class CdaPentahoKettleTransFromFileProducer extends KettleTransFromFileProducer {


  private ParameterMapping[] definedVariableNames;
  private String[] definedArgumentNames;
  private transient Trans currentlyRunningTransformation;

  public CdaPentahoKettleTransFromFileProducer( final String repositoryName,
                                                final String transformationFile,
                                                final String stepName,
                                                final String username,
                                                final String password,
                                                final String[] definedArgumentNames,
                                                final ParameterMapping[] definedVariableNames ) {

    super( repositoryName, transformationFile, stepName, username, password, definedArgumentNames,
      definedVariableNames );
    this.definedVariableNames = definedVariableNames;
    this.definedArgumentNames = definedArgumentNames;
  }


  private String[] fillArguments( final DataRow parameters ) {
    final String[] params = new String[ definedArgumentNames.length ];
    for ( int i = 0; i < definedArgumentNames.length; i++ ) {
      final String name = definedArgumentNames[ i ];
      final Object value = parameters.get( name );
      if ( value == null ) {
        params[ i ] = null;
      } else {
        params[ i ] = String.valueOf( value );
      }
    }
    return params;
  }


  private Repository connectToRepository()
    throws ReportDataFactoryException, KettleException {
    if ( getRepositoryName() == null ) {
      throw new NullPointerException();
    }

    final RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    try {
      repositoriesMeta.readData();
    } catch ( KettleException ke ) {
      // we're a bit low to bubble a dialog to the user here..
      // when ramaiz fixes readData() to stop throwing exceptions
      // even when successful we can remove this and use
      // the more favorable repositoriesMeta.getException() or something
      // like it (I'm guessing on the method name)
    }

    // Find the specified repository.
    final RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( getRepositoryName() );

    if ( repositoryMeta == null ) {
      // repository object is not necessary for filesystem transformations
      return null;
    }

    final Repository repository =
        PluginRegistry.getInstance().loadClass( RepositoryPluginType.class,
          repositoryMeta.getId(),
          Repository.class );
    repository.init( repositoryMeta );
    repository.connect( getUsername(), getPassword() );
    return repository;
  }


  public TableModel performQuery( final DataRow parameters,
                                  final int queryLimit,
                                  final ResourceManager resourceManager,
                                  final ResourceKey resourceKey )
    throws KettleException, ReportDataFactoryException {
    if ( getStepName() == null ) {
      throw new ReportDataFactoryException( "No step name defined." );
    }

    final String[] params = fillArguments( parameters );


    final Repository repository = connectToRepository();
    try {
      final TransMeta transMeta = loadTransformation( repository, resourceManager, resourceKey );
      transMeta.setArguments( params );
      final Trans trans = new Trans( transMeta );
      for ( final ParameterMapping mapping : definedVariableNames ) {
        final String sourceName = mapping.getName();
        final String variableName = mapping.getAlias();
        final Object value = parameters.get( sourceName );
        if ( value != null ) {
          trans.setParameterValue( variableName, String.valueOf( value ) );
        }
      }

      transMeta.setInternalKettleVariables();
      trans.prepareExecution( transMeta.getArguments() );

      TableProducer tableProducer = null;
      final List<StepMetaDataCombi> stepList = trans.getSteps();
      for ( StepMetaDataCombi aStepList : stepList ) {
        if ( !getStepName().equals( aStepList.stepname ) ) {
          continue;
        }
        final RowMetaInterface row = transMeta.getStepFields( getStepName() );
        tableProducer = new TableProducer( row, queryLimit, isStopOnError() );
        aStepList.step.addRowListener( tableProducer );
        break;
      }

      if ( tableProducer == null ) {
        throw new ReportDataFactoryException( "Cannot find the specified transformation step " + getStepName() );
      }

      currentlyRunningTransformation = trans;
      trans.startThreads();
      trans.waitUntilFinished();
      trans.cleanup();
      if ( trans.getErrors() > 0 ) {
        throw new KettleException( "Transformation finished with errors" );
      }
      return tableProducer.getTableModel();
    } finally {
      currentlyRunningTransformation = null;
      if ( repository != null ) {
        repository.disconnect();
      }
    }

  }

  public void cancelQuery() {
    final Trans currentlyRunningTransformation = this.currentlyRunningTransformation;
    if ( currentlyRunningTransformation != null ) {
      currentlyRunningTransformation.stopAll();
      this.currentlyRunningTransformation = null;
    }
  }




  @Override
  protected String computeFullFilename( ResourceKey key ) {
    while ( key != null ) {
      final Object schema = key.getSchema();
      if ( !RepositoryResourceLoader.SOLUTION_SCHEMA_NAME.equals( schema )
          && !"pt.webdetails.cda.settings.CdaRepositoryResourceLoader:".equals( schema ) ) {
        // these are not the droids you are looking for ..
        key = key.getParent();
        continue;
      }

      final Object identifier = key.getIdentifier();
      if ( identifier instanceof String ) {
        // get a local file reference ...
        final String file = (String) identifier;
        // pedro alves - Getting the file through normal apis
        final String fileName = PentahoSystem.getApplicationContext().getSolutionPath( file );
        if ( fileName != null ) {
          return fileName;
        }
      }
      key = key.getParent();
    }

    return super.computeFullFilename( key );
  }


  protected static class TableProducer implements RowListener {
    private TypedTableModel tableModel;
    private int rowsWritten;
    private RowMetaInterface rowMeta;
    private int queryLimit;
    private boolean stopOnError;

    private boolean firstCall;
    private boolean error;

    private TableProducer( final RowMetaInterface rowMeta, final int queryLimit, final boolean stopOnError ) {
      this.rowMeta = rowMeta;
      this.queryLimit = queryLimit;
      this.stopOnError = stopOnError;
      this.firstCall = true;
    }

    /**
     * This method is called when a row is written to another step (even if there is no next step)
     *
     * @param rowMeta the metadata of the row
     * @param row     the data of the row
     * @throws KettleStepException an exception that can be thrown to hard stop the step
     */
    public void rowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) throws KettleStepException {
      if ( firstCall ) {
        this.tableModel = createTableModel( rowMeta );
        firstCall = false;
      }

      if ( queryLimit > 0 && rowsWritten > queryLimit ) {
        return;
      }

      try {
        rowsWritten += 1;

        final int count = tableModel.getColumnCount();
        final Object[] dataRow = new Object[ count ];
        for ( int columnNo = 0; columnNo < count; columnNo++ ) {
          final ValueMetaInterface valueMeta = rowMeta.getValueMeta( columnNo );

          switch ( valueMeta.getType() ) {
            case ValueMetaInterface.TYPE_BIGNUMBER:
              dataRow[ columnNo ] = rowMeta.getBigNumber( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              dataRow[ columnNo ] = rowMeta.getBoolean( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_DATE:
              dataRow[ columnNo ] = rowMeta.getDate( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              dataRow[ columnNo ] = rowMeta.getInteger( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_NONE:
              dataRow[ columnNo ] = rowMeta.getString( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              dataRow[ columnNo ] = rowMeta.getNumber( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_STRING:
              dataRow[ columnNo ] = rowMeta.getString( row, columnNo );
              break;
            case ValueMetaInterface.TYPE_BINARY:
              dataRow[ columnNo ] = rowMeta.getBinary( row, columnNo );
              break;
            default:
              dataRow[ columnNo ] = rowMeta.getString( row, columnNo );
          }
        }
        tableModel.addRow( dataRow );
      } catch ( KettleValueException kve ) {
        throw new KettleStepException( kve );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }

    private TypedTableModel createTableModel( final RowMetaInterface rowMeta ) {
      final int colCount = rowMeta.size();
      final String[] fieldNames = new String[ colCount ];
      final Class<?>[] fieldTypes = new Class<?>[ colCount ];
      for ( int columnNo = 0; columnNo < colCount; columnNo++ ) {
        final ValueMetaInterface valueMeta = rowMeta.getValueMeta( columnNo );
        fieldNames[ columnNo ] = valueMeta.getName();

        switch ( valueMeta.getType() ) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            fieldTypes[ columnNo ] = BigDecimal.class;
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            fieldTypes[ columnNo ] = Boolean.class;
            break;
          case ValueMetaInterface.TYPE_DATE:
            fieldTypes[ columnNo ] = Date.class;
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            fieldTypes[ columnNo ] = Integer.class;
            break;
          case ValueMetaInterface.TYPE_NONE:
            fieldTypes[ columnNo ] = String.class;
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            fieldTypes[ columnNo ] = Double.class;
            break;
          case ValueMetaInterface.TYPE_STRING:
            fieldTypes[ columnNo ] = String.class;
            break;
          case ValueMetaInterface.TYPE_BINARY:
            fieldTypes[ columnNo ] = byte[].class;
            break;
          default:
            fieldTypes[ columnNo ] = String.class;
        }

      }
      return new TypedTableModel( fieldNames, fieldTypes );
    }

    /**
     * This method is called when a row is read from another step
     *
     * @param rowMeta the metadata of the row
     * @param row     the data of the row
     * @throws KettleStepException an exception that can be thrown to hard stop the step
     */
    public void rowReadEvent( final RowMetaInterface rowMeta, final Object[] row ) throws KettleStepException {
    }

    /**
     * This method is called when the error handling of a row is writing a row to the error stream.
     *
     * @param rowMeta the metadata of the row
     * @param row     the data of the row
     * @throws KettleStepException an exception that can be thrown to hard stop the step
     */
    public void errorRowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) throws KettleStepException {
      if ( stopOnError ) {
        throw new KettleStepException( "Aborting transformation due to error detected" );
      }
      error = true;
    }

    public TableModel getTableModel() throws ReportDataFactoryException {
      if ( stopOnError && error ) {
        throw new ReportDataFactoryException( "Transformation produced an error." );
      }

      if ( tableModel == null ) {
        return createTableModel( rowMeta );
      }
      return tableModel;
    }
  }


}

