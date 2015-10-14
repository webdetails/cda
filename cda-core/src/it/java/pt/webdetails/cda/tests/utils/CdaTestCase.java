/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.tests.utils;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;

import javax.swing.table.TableModel;
import java.io.ByteArrayOutputStream;
import java.io.File;


public abstract class CdaTestCase extends TestCase {

  private CdaTestEnvironment testEnvironment;
  private static final String USER_DIR = System.getProperty( "user.dir" );
  private static final Class[] customDataFactories = {
    CompactBandedMDXDataFactory.class, ExtBandedMDXDataFactory.class, ExtDenormalizedMDXDataFactory.class };

  public CdaTestCase( String name ) {
    super( name );
  }

  public CdaTestCase() {
  }

  protected void setUp() throws Exception {
    super.setUp();
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    log().info( "factory:" + factory );
    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment( factory ) );

    // cda-specific environment
    testEnvironment = new CdaTestEnvironment( factory );
    // cda init
    CdaEngine.init( testEnvironment );
    // making sure the custom data factories are registered
    registerCustomDataFactories();
    // due to http://jira.pentaho.com/browse/PDI-2975
    System.setProperty( "org.osjava.sj.root", getSimpleJndiPath() );
  }

  protected String getSimpleJndiPath() {

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

  protected static void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
        clazz.getName(), "", "", true, false, true, false, false, false, false, false,
        new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }
}
