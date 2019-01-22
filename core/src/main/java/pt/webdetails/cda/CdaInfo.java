/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
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
