/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda;

import org.pentaho.reporting.engine.classic.core.ClassicEngineInfo;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Todo: Document me!
 * <p/>
 * Date: 09.02.2010
 * Time: 17:51:47
 *
 * @author Thomas Morgner.
 */
public class CdaInfo extends ProjectInformation
{
  private static CdaInfo instance;

  public static synchronized CdaInfo getInstance()
  {
    if (instance == null)
    {
      instance = new CdaInfo();
      instance.initialize();
    }
    return instance;
  }

  public CdaInfo()
  {
    super("cda", "CDA - Community Data Access");
  }

  private void initialize()
  {
    setLicenseName("MPL");
    setInfo("http://cda.webdetails.org");
    setCopyright("Copyright 2009 - 2013 Webdetails, a Pentaho company");

    setBootClass("pt.webdetails.cda.CdaBoot");

    addLibrary(ClassicEngineInfo.getInstance());
  }
}
