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

import org.pentaho.reporting.libraries.base.boot.AbstractBoot;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Properties-based configuration holder.
 * <p/>
 * Date: 09.02.2010
 * Time: 17:51:22
 *
 * @author Thomas Morgner.
 */
public class CdaBoot extends AbstractBoot
{

  /**
   * The singleton instance of the Boot class.
   */
  private static CdaBoot instance;
  /**
   * The project info contains all meta data about the project.
   */
  private ProjectInformation projectInfo;


  protected CdaBoot()
  {
    projectInfo = CdaInfo.getInstance();
  }


  /**
   * Returns the singleton instance of the boot utility class.
   *
   * @return the boot instance.
   */
  public static synchronized CdaBoot getInstance()
  {
    if (instance == null)
    {
      instance = new CdaBoot();
    }
    return instance;
  }


  /**
   * Loads the configuration. This will be called exactly once.
   *
   * @return The configuration.
   */
  @Override
  protected Configuration loadConfiguration()
  {
    return createDefaultHierarchicalConfiguration("/pt/webdetails/cda/cda.properties",
            "/cda.properties", true, CdaBoot.class);
  }


  /**
   * Performs the boot.
   */
    @Override
  protected void performBoot()
  {
    // any manual init work goes in here ...
  }


  /**
   * Returns the project info.
   *
   * @return The project info.
   */
  @Override
  protected ProjectInformation getProjectInfo()
  {
    return projectInfo;
  }
}
