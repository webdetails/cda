/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.dataaccess;

import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.MondrianConnectionProvider;
import org.pentaho.reporting.platform.plugin.connection.PentahoMondrianConnectionProvider;

/**
 *
 * @author joao
 */
public class PentahoCubeFileProviderSetter implements ICubeFileProviderSetter {
    @Override
    public void setCubeFileProvider(AbstractNamedMDXDataFactory factory, String catalog) { 
        
        factory.setCubeFileProvider(new DefaultCubeFileProvider(catalog));
    
        //XXX this method already receives a catalog, setCubeFileProvider above enough?
        //factory.setCubeFileProvider(new PentahoCubeFileProvider(mondrianConnectionInfo.getCatalog()));
      try
      {
        factory.setMondrianConnectionProvider((MondrianConnectionProvider) PentahoSystem.getObjectFactory().get(PentahoMondrianConnectionProvider.class, "MondrianConnectionProvider", null));
      }
      catch (ObjectFactoryException e)
      {//couldn't get object
        factory.setMondrianConnectionProvider(new PentahoMondrianConnectionProvider());
      }
    }
    
    
    
}
