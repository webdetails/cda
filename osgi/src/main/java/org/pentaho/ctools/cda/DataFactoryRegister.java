package org.pentaho.ctools.cda;

import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cda.utils.streaming.SQLStreamingReportDataFactory;

public class DataFactoryRegister {
  public DataFactoryRegister( CdaEngine cdaEngine ) {
  }

  public void init() throws InitializationException {
    registerCustomDataFactories();
  }

  private final Class[] customDataFactories = {
      KettleDataFactory.class,
      SQLStreamingReportDataFactory.class };

  private void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
          clazz.getName(), "", "", true, false, true, false, false, false, false, false,
          new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }

}
