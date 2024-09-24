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
