/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cda.exporter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.SqlDataAccess;
import pt.webdetails.cda.dataaccess.kettle.KettleAdapterException;
import pt.webdetails.cda.dataaccess.kettle.SQLKettleAdapter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cpf.Util;

public class DefaultStreamExporterTest extends AbstractKettleExporterTestBase {

  @Test
  public void testStreamQueryAndExport() throws Exception {
    SqlDataAccess sqlDataAccess = new SqlDataAccess( "id", "name", null, null );
    HashMap<Integer, ArrayList<Integer>> outs = new HashMap<>();
    ArrayList<Integer> arr = new ArrayList<Integer>();
    arr.add( 0 );
    outs.put( 1, arr );
    sqlDataAccess.setOutputs( outs );
    QueryOptions opts = new QueryOptions();
    opts.addParameter( "bogus", "baah" );
    opts.setOutputType( "csv" );
    SQLKettleAdapter sqlAdapter = new BogusSqlKettleDataAccess( sqlDataAccess, opts );

    DefaultStreamExporter kettleFileWriter =
      new DefaultStreamExporter( new CsvExporter( Collections.<String, String>emptyMap() ), sqlAdapter );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    kettleFileWriter.export( out );
    String[] res = Util.toString( out.toByteArray() ).split( System.lineSeparator() );
    assertEquals( "\"a\"", res[ 0 ] );
    assertEquals( "\"val\"", res[ 1 ] );
    assertNotNull( res );
  }

  @Test
  public void testEngineReturnsStreaming() throws Exception {
    CdaEngine engine = CdaEngine.getInstance();

    SqlDataAccess sqlDataAccess = new SqlDataAccess( "id", "name", null, null );
    CdaSettings settings = mock( CdaSettings.class );
    when( settings.getDataAccess( "id" ) ).thenReturn( sqlDataAccess );

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.setDataAccessId( "id" );
    queryOptions.addParameter( "param", "..." );

    queryOptions.setOutputType( "csv" );
    queryOptions.addSetting( CsvExporter.CSV_SEPARATOR_SETTING, "," );
    assertTrue( engine.doExportQuery( settings, queryOptions ) instanceof ExportedStreamQueryResult );

    queryOptions.setOutputType( "xls" );
    assertTrue( engine.doExportQuery( settings, queryOptions ) instanceof ExportedStreamQueryResult );
  }

  static class BogusSqlKettleDataAccess extends SQLKettleAdapter {

    public BogusSqlKettleDataAccess( SqlDataAccess dataAccess, QueryOptions queryOptions ) {
      super( dataAccess, queryOptions );
    }

    public StepMeta getKettleStepMeta( String name ) throws KettleAdapterException {
      DataGridMeta meta = new DataGridMeta();
      meta.setDefault();
      meta.allocate( 1 );
      meta.getFieldName()[ 0 ] = "a";
      meta.getFieldType()[ 0 ] = "String";
      meta.setDataLines( Collections.singletonList( Collections.singletonList( "val" ) ) );
      return new StepMeta( name, meta );
    }

    ;

    @Override
    public DatabaseMeta[] getDatabases() throws KettleAdapterException {
      return new DatabaseMeta[ 0 ];
    }
  }

}
