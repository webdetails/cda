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


package pt.webdetails.cda.dataaccess;

import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.MondrianConnectionProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoCubeFileProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianConnectionProvider;

public class PentahoCubeFileProviderSetter implements ICubeFileProviderSetter {

  @Override
  public void setCubeFileProvider( AbstractNamedMDXDataFactory factory, String catalog ) {

    factory.setCubeFileProvider( new PentahoCubeFileProvider( catalog ) );

    try {
      factory.setMondrianConnectionProvider( (MondrianConnectionProvider) PentahoSystem.getObjectFactory()
        .get( PentahoMondrianConnectionProvider.class, "MondrianConnectionProvider", null ) );
    } catch ( ObjectFactoryException e ) { //couldn't get object
      factory.setMondrianConnectionProvider( new PentahoMondrianConnectionProvider() );
    }
  }
}
