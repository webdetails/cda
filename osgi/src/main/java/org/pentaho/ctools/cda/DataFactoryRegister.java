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
package org.pentaho.ctools.cda;

import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataAttributeCache;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.utils.streaming.SQLStreamingReportDataFactory;

public class DataFactoryRegister {
  public DataFactoryRegister( CdaEngine cdaEngine ) {
  }

  public void init() throws InitializationException {
    registerCustomDataFactories();

    // This is just to force the import of org.pentaho.reporting.engine.classic.core.wizard
    // TODO Figure out a better solution or simply add the Import-Package instruction to the maven-bundle-plugin configuration
    DefaultDataAttributeCache.class.getName();
  }

  private final Class[] customDataFactories = {
    SQLStreamingReportDataFactory.class
  };

  private void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
          clazz.getName(), "", "", true, false, true, false, false, false, false, false,
          new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }

}
