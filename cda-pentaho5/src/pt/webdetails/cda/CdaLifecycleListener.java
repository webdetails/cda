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

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;

import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;

import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;


/**
 * This class inits Cda plugin within the bi-platform
 *
 * @author gorman
 */
public class CdaLifecycleListener extends SimpleLifeCycleListener implements IPlatformReadyListener {

  static Log logger = LogFactory.getLog( CdaLifecycleListener.class );
  private final Class[] customDataFactories = {
      CompactBandedMDXDataFactory.class, ExtBandedMDXDataFactory.class, ExtDenormalizedMDXDataFactory.class };


  public void init() throws PluginLifecycleException {

  }


  public void loaded() throws PluginLifecycleException {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try {
      super.loaded();
      PluginEnvironment.init( CdaPluginEnvironment.getInstance() );
      CdaEngine.init( new PentahoCdaEnvironment() );
      CdaEngine.getInstance().getConfigProperty( "just load", null );

      // registering custom data factories
      registerCustomDataFactories();

    } catch ( Exception e ) {
      logger.error( "loading error", e );
    } finally {
      Thread.currentThread().setContextClassLoader( contextCL );
    }
  }


  public void unLoaded() throws PluginLifecycleException {
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return PentahoPluginEnvironment.getInstance();
  }

  @Override public void ready() throws PluginLifecycleException {

  }

  private void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
          clazz.getName(), "", "", true, false, true, false, false, false, false, false,
          new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }

}
