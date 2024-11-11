/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.cache.scheduler;

import org.pentaho.platform.api.scheduler2.IJob;

import pt.webdetails.cda.settings.SettingsManager;


public class ScheduledQueryExecution extends QueryExecution {
  public ScheduledQueryExecution( SettingsManager settingsMgr, String jsonString, IJob job ) throws Exception {
    super( settingsMgr, jsonString );
    this.job = job;
  }

  private IJob job;

  public IJob getJob() {
    return job;
  }

  public void setJob( IJob job ) {
    this.job = job;
  }

  public String getCronString() {
    return job.getJobTrigger().getCronString();
  }
}
