package org.pentaho.ctools.cda.connections.dataservices;

import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.DriverConnectionProvider;
import pt.webdetails.cda.connections.dataservices.IDataservicesLocalConnection;

import java.net.MalformedURLException;
import java.util.Map;

public class DataservicesLocalConnection implements IDataservicesLocalConnection {
  @Override
  public DriverConnectionProvider getDriverConnectionProvider(Map<String, String> dataserviceParameters) throws MalformedURLException {
    return new DriverConnectionProvider( );
  }
}
