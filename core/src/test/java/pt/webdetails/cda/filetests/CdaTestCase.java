/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.filetests;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;

import javax.swing.table.TableModel;

import java.io.ByteArrayOutputStream;
import java.io.File;


public abstract class CdaTestCase extends TestCase {

  private CdaTestEnvironment testEnvironment;
  private static final String USER_DIR = System.getProperty( "user.dir" );


  public CdaTestCase( String name ) {
    super( name );
  }

  public CdaTestCase() {
  }

  protected void setUp() throws Exception {
    super.setUp();
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment( factory ) );

    // cda-specific environment
    testEnvironment = new CdaTestEnvironment( factory );
    // cda init
    CdaEngine.init( testEnvironment );
    // making sure the custom data factories are registered
    //    registerCustomDataFactories();
    // due to http://jira.pentaho.com/browse/PDI-2975
    System.setProperty( "org.osjava.sj.root", getSimpleJndiPath() );
  }

  protected static String getSimpleJndiPath() {

    if ( USER_DIR.endsWith( "bin/test/classes" ) ) {
      // command-line run
      return USER_DIR + File.separator + "simplejndi";
    } else {
      // IDE run
      return USER_DIR + File.separator + "test-resources" + File.separator + "simplejndi";
    }

  }

  protected Log log() {
    return LogFactory.getLog( getClass() );
  }

  protected SettingsManager getSettingsManager() {
    return getEngine().getSettingsManager();
  }

  protected CdaEngine getEngine() {
    return CdaEngine.getInstance();
  }

  protected CdaSettings parseSettingsFile( String cdaSettingsId ) throws Exception {
    return getSettingsManager().parseSettingsFile( cdaSettingsId );
  }

  protected ICdaEnvironment getEnvironment() {
    return CdaEngine.getEnvironment();
  }

  protected TableModel doQuery( CdaSettings cdaSettings, QueryOptions queryOptions ) throws Exception {
    return getEngine().doQuery( cdaSettings, queryOptions );
  }

  protected String exportTableModel( TableModel table, ExportOptions opts ) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getEngine().getExporter( opts ).export( baos, table );
    return Util.toString( baos.toByteArray() );
  }

}
