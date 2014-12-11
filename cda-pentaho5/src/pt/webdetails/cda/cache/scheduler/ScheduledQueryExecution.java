/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.cache.scheduler;

import org.pentaho.platform.api.scheduler2.Job;

import pt.webdetails.cda.settings.SettingsManager;


public class ScheduledQueryExecution extends QueryExecution {
  public ScheduledQueryExecution( SettingsManager settingsMgr, String jsonString, Job job ) throws Exception {
    super( settingsMgr, jsonString );
    this.job = job;
  }

  private Job job;

  public Job getJob() {
    return job;
  }

  public void setJob( Job job ) {
    this.job = job;
  }

  public String getCronString() {
    return job.getJobTrigger().getCronString();
  }
}
