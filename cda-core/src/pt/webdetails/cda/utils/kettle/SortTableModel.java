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

import java.util.Comparator;

import org.pentaho.reporting.libraries.base.util.StringUtils;
import pt.webdetails.robochef.DynamicTransConfig;
import pt.webdetails.robochef.DynamicTransConfig.EntryType;
import pt.webdetails.robochef.DynamicTransMetaConfig;
import pt.webdetails.robochef.DynamicTransMetaConfig.Type;
import pt.webdetails.robochef.DynamicTransformation;
import pt.webdetails.robochef.RowProductionManager;
import pt.webdetails.robochef.TableModelInput;
import pt.webdetails.cda.CdaEngine;

public class SortTableModel implements RowProductionManager {

  private static final Log logger = LogFactory.getLog( SortTableModel.class );
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private static final long DEFAULT_ROW_PRODUCTION_TIMEOUT = 120;
  private static final TimeUnit DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT = TimeUnit.SECONDS;


  public SortTableModel() {
  }

  public TableModel doSort( TableModel unsorted, List<String> sortBy ) throws SortException {
    String sortType = CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.SortingType" );
    if ( "DEFAULT".equals( sortType ) || StringUtils.isEmpty( sortType ) ) {
      return defaultSort( unsorted, sortBy );
    } else {
      return customSort( unsorted, sortBy, sortType );
    }
  }

  public TableModel customSort( TableModel unsorted, List<String> sortBy, String comparatorClass )
    throws SortException {
    try {
      @SuppressWarnings( "unchecked" )
      Class<? extends Comparator<Integer>> comp =
        (Class<? extends Comparator<Integer>>) Class.forName( comparatorClass );
      SortableTableModel sortable = new SortableTableModel( unsorted );
      sortable.sort( comp, sortBy );
      return sortable;
    } catch ( Exception e ) {
      throw new SortException( "Exception during sorting ", e );
    }
  }

  public TableModel defaultSort( TableModel unsorted, List<String> sortBy ) throws SortException {

    if ( unsorted == null || unsorted.getRowCount() == 0 ) {
      return unsorted;
    } else {
      TableModel output = null;
      Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

      try {
        String sort = getSortXmlStep( unsorted, sortBy );

        DynamicTransMetaConfig transMetaConfig =
          new DynamicTransMetaConfig( Type.EMPTY, "JoinCompoundData", null, null );
        DynamicTransConfig transConfig = new DynamicTransConfig();

        transConfig.addConfigEntry( EntryType.STEP, "input",
          "<step><name>input</name><type>Injector</type><copies>1</copies></step>" );
        transConfig.addConfigEntry( EntryType.STEP, "sort", sort );
        transConfig.addConfigEntry( EntryType.HOP, "input", "sort" );

        TableModelInput input = new TableModelInput();
        transConfig.addInput( "input", input );
        inputCallables.add( input.getCallableRowProducer( unsorted, true ) );


        RowMetaToTableModel outputListener = new RowMetaToTableModel( false, true, false );
        transConfig.addOutput( "sort", outputListener );

        DynamicTransformation trans = new DynamicTransformation( transConfig, transMetaConfig, inputCallables );
        trans.executeCheckedSuccess( null, null, this );
        logger.info( trans.getReadWriteThroughput() );
        output = outputListener.getRowsWritten();

        return output;

      } catch ( Exception e ) {
        throw new SortException( "Exception during sorting ", e );
      }
    }
  }

  public void startRowProduction( Collection<Callable<Boolean>> inputCallables ) {
    String timeoutStr = CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.DefaultRowProductionTimeout" );
    long timeout = StringUtils.isEmpty( timeoutStr ) ? DEFAULT_ROW_PRODUCTION_TIMEOUT : Long.parseLong( timeoutStr );
    String unitStr =
      CdaEngine.getInstance().getConfigProperty( "pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit" );
    TimeUnit unit = StringUtils.isEmpty( unitStr ) ? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT : TimeUnit.valueOf( unitStr );
    startRowProduction( timeout, unit, inputCallables );
  }

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

  private String getSortXmlStep( TableModel unsorted, List<String> sortBy ) throws SortException {
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
        + "    <fields>\n" );

    for ( String s : sortBy ) {
      SortDescriptor sort = new SortDescriptor( ( s ) );

      sortXML.append( "      <field>\n"
        + "        <name>" + unsorted.getColumnName( sort.getIndex() ) + "</name>\n"
        + "        <ascending>" + sort.getIsAscendingString() + "</ascending>\n"
        + "        <case_sensitive>N</case_sensitive>\n"
        + "      </field>\n" );
    }

    sortXML.append( "    </fields>\n"
      + "     <cluster_schema/>\n"
      + " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n"
      + "      <xloc>615</xloc>\n"
      + "      <yloc>188</yloc>\n"
      + "      <draw>Y</draw>\n"
      + "      </GUI>\n"
      + "    </step>\n" );

    return sortXML.toString();
  }

  private class SortDescriptor {

    private Integer index;
    private String direction;
    private static final String REGEXP = "^(\\d+)([AD]?)$";
    Pattern p = Pattern.compile( REGEXP );

    public SortDescriptor( String sortBy ) throws SortException {

      Matcher m = p.matcher( sortBy );
      if ( m.matches() ) {
        // valid one
        index = Integer.parseInt( m.group( 1 ) );

        if ( m.group( 2 ).equals( "D" ) ) {
          setDirection( "DESC" );
        } else {
          setDirection( "ASC" );
        }

      } else {
        throw new SortException( "Invalid searchBy option: " + sortBy, null );
      }

    }

    public String getIsAscendingString() {
      if ( getDirection().equals( "ASC" ) ) {
        return "Y";
      } else {
        return "N";
      }
    }

    public String getDirection() {
      return direction;
    }

    public void setDirection( String direction ) {
      this.direction = direction;
    }

    public Integer getIndex() {
      return index;
    }

    public void setIndex( Integer index ) {
      this.index = index;
    }
  }
}
