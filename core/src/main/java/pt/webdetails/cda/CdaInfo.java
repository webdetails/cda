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

package pt.webdetails.cda;

import org.pentaho.reporting.engine.classic.core.ClassicEngineInfo;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Todo: Document me!
 */
public class CdaInfo extends ProjectInformation {
  private static final CdaInfo instance;

  static {
    instance = new CdaInfo();
    instance.initialize();
  }

  public static CdaInfo getInstance() {
    return instance;
  }

  public CdaInfo() {
    super( "cda", "CDA - Community Data Access" );
  }

  private void initialize() {
    // TODO: Doesn't this need an update?
    setLicenseName( "MPL" );
    setInfo( "http://cda.webdetails.org" );
    setCopyright( "Copyright 2009 - 2013 Webdetails, a Pentaho company" );

    setBootClass( "pt.webdetails.cda.CdaBoot" );

    addLibrary( ClassicEngineInfo.getInstance() );
  }
}
