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

import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DefaultCubeFileProvider;

public class DefaultCubeFileProviderSetter implements ICubeFileProviderSetter {

  @Override
  public void setCubeFileProvider( AbstractNamedMDXDataFactory factory, String catalog ) {
    factory.setCubeFileProvider( new DefaultCubeFileProvider( catalog ) );
  }

}
