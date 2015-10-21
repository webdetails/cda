/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.exporter;

import java.util.Map;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;


public class XlsExporter extends AbstractKettleExporter {
  public static final String TEMPLATE_NAME_SETTING = "templateName";

  private String attachmentName;
  private String templateName;
  private boolean includeHeader;

  public XlsExporter( Map<String, String> extraSettings ) {
    super( extraSettings );
    this.attachmentName = getSetting( ATTACHMENT_NAME_SETTING, "cda-export." + getType() );
    this.templateName = getSetting( TEMPLATE_NAME_SETTING, null );
    //IReadAccess repository = CdaEngine.getRepo().getUserContentAccess("/");
    // TODO: test this
    // paths should already be provided in relation to the 'solution base'
    //    if(templateName != null){
    //      templateName = repository.getSolutionPath(templateName);
    //    }
    includeHeader = Boolean.parseBoolean( getSetting( COLUMN_HEADERS_SETTING, "true" ) );
  }

  protected StepMeta getExportStepMeta( String name ) {
    ExcelOutputMeta excelOutput = new ExcelOutputMeta();
    excelOutput.setDefault();
    excelOutput.setFileName( "${java.io.tmpdir}/" + getFileName() );
    excelOutput.setHeaderEnabled( includeHeader );
    if ( templateName != null ) {
      excelOutput.setTemplateEnabled( true );
      excelOutput.setTemplateFileName( templateName );
      excelOutput.setTemplateAppend( true );
    }
    StepMeta stepMeta = new StepMeta( name, excelOutput );
    stepMeta.setCopies( 1 );
    return stepMeta;
  }

  public String getMimeType() {
    return "application/vnd.ms-excel";
  }

  public String getType() {
    return "xls";
  }

  public String getAttachmentName() {
    return this.attachmentName;
  }


}
