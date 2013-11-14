package pt.webdetails.cda.cache.scheduler;

import org.pentaho.platform.api.scheduler2.Job;

import pt.webdetails.cda.settings.SettingsManager;

/**
 * 
 */
public class ScheduledQueryExecution extends QueryExecution {
  public ScheduledQueryExecution( SettingsManager settingsMgr, String jsonString, Job job ) throws Exception {
    super(settingsMgr, jsonString );
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
