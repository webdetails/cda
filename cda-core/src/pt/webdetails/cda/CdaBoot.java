/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import org.pentaho.reporting.libraries.base.boot.AbstractBoot;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Todo: Document me!
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
  protected Configuration loadConfiguration()//XXX Added @Override
  {
    return createDefaultHierarchicalConfiguration("/pt/webdetails/cda/cda.properties",
            "/cda.properties", true, CdaBoot.class);
  }


  /**
   * Performs the boot.
   */
    @Override
  protected void performBoot()//XXX Added @Override
  {
    // any manual init work goes in here ...
  }


  /**
   * Returns the project info.
   *
   * @return The project info.
   */
  @Override
  protected ProjectInformation getProjectInfo()//XXX Added @Override
  {
    return projectInfo;
  }
}
