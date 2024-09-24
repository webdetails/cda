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

import org.pentaho.reporting.libraries.base.boot.AbstractBoot;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Properties-based configuration holder.
 */
public class CdaBoot extends AbstractBoot {

  /**
   * The singleton instance of the Boot class.
   */
  private static CdaBoot instance = new CdaBoot();
  /**
   * The project info contains all meta data about the project.
   */
  private ProjectInformation projectInfo;


  protected CdaBoot() {
    projectInfo = CdaInfo.getInstance();
  }

  /**
   * Returns the singleton instance of the boot utility class.
   *
   * @return the boot instance.
   */
  public static CdaBoot getInstance() {
    return instance;
  }


  /**
   * Loads the configuration. This will be called exactly once.
   *
   * @return The configuration.
   */
  @Override
  protected Configuration loadConfiguration() {
    return createDefaultHierarchicalConfiguration( "/pt/webdetails/cda/cda.properties", "/cda.properties", true,
      CdaBoot.class );
  }


  /**
   * Performs the boot.
   */
  @Override
  protected void performBoot() {
    // any manual init work goes in here ...
  }


  /**
   * Returns the project info.
   *
   * @return The project info.
   */
  @Override
  protected ProjectInformation getProjectInfo() {
    return projectInfo;
  }
}
