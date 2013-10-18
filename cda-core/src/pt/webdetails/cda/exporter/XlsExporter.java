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

package pt.webdetails.cda.exporter;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class XlsExporter extends AbstractKettleExporter
{
  
  private static final Log logger = LogFactory.getLog(XlsExporter.class);

  public static final String TEMPLATE_NAME_SETTING = "templateName";
  
  private String attachmentName;
  private String templateName;
  private boolean includeHeader;

  public XlsExporter(HashMap <String,String> extraSettings)
  {
    super(extraSettings);
    this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "cda-export." + getType());
    this.templateName = getSetting(TEMPLATE_NAME_SETTING, null);
    IReadAccess repository = CdaEngine.getRepo().getUserContentAccess("/");
//    if(templateName != null){s
//      templateName = repository.
////      templateName = repository.getSolutionPath(templateName);
//    }
    includeHeader = Boolean.parseBoolean(getSetting( COLUMN_HEADERS_SETTING, "true"));
  }
  
  protected String getExportStepDefinition(String name){
    ExcelOutputMeta excelOutput = new ExcelOutputMeta();
    excelOutput.setDefault();
    excelOutput.setFileName("${java.io.tmpdir}/" + getFileName());
    excelOutput.setHeaderEnabled(includeHeader);
    if(templateName != null){
      excelOutput.setTemplateEnabled(true);
      excelOutput.setTemplateFileName(templateName);
      excelOutput.setTemplateAppend(true);
    }
    
    StepMeta meta = new StepMeta(name, excelOutput);
    try {
      return meta.getXML();
    } catch (KettleException e) {
      logger.error(e);
      //kept as a fallback for now
      return getExportStepDefinitionS(name);
    }
  }

  protected String getExportStepDefinitionS(String name)
  {
    StringBuilder xml = new StringBuilder();

    //TODO: use meta instead of string xml?
    
    xml.append("<step>\n" +
        "    <name>"+ name + "</name>\n" +
        "    <type>ExcelOutput</type>\n" +
        "    <description/>\n" +
        "    <distribute>Y</distribute>\n" +
        "    <copies>1</copies>\n" +
        "         <partitioning>\n" +
        "           <method>none</method>\n" +
        "           <schema_name/>\n" +
        "           </partitioning>\n" +
        "    <header>"+ (includeHeader? "Y" : "N")  + "</header>\n" +
        "    <footer>N</footer>\n" +
        "    <encoding/>\n" +
        "    <append>N</append>\n" +
        "    <add_to_result_filenames>Y</add_to_result_filenames>\n" +
        "    <file>\n" +
        "      <name>${java.io.tmpdir}&#47;");

    xml.append(getFileName());

    xml.append("</name>\n" +
        "      <extention>xls</extention>\n" +
        "      <do_not_open_newfile_init>N</do_not_open_newfile_init>\n" +
        "      <split>N</split>\n" +
        "      <add_date>N</add_date>\n" +
        "      <add_time>N</add_time>\n" +
        "      <SpecifyFormat>N</SpecifyFormat>\n" +
        "      <date_time_format/>\n" +
        "      <sheetname>Sheet1</sheetname>\n" +
        "      <autosizecolums>N</autosizecolums>\n" +
        "      <protect_sheet>N</protect_sheet>\n" +
        "      <password>Encrypted </password>\n" +
        "      <splitevery>0</splitevery>\n" +
        "      </file>\n" +
        "    <template>\n" +
        "      <enabled>" + (this.templateName != null ? "Y" : "N") + "</enabled>\n" +
        "      <append>Y</append>\n" +
        "      <filename>" + (this.templateName != null ? this.templateName : "template.xls") + "</filename>\n" +
        "    </template>\n" +
        "    <fields>\n" +
        "    </fields>\n" +
        "     <cluster_schema/>\n" +
        " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n" +
        "      <xloc>498</xloc>\n" +
        "      <yloc>253</yloc>\n" +
        "      <draw>Y</draw>\n" +
        "      </GUI>\n" +
        "    </step>");

    return xml.toString();

  }

  public String getMimeType()
  {
    return "application/vnd.ms-excel";
  }

  public String getType()
  {
    return "xls";
  }

    public String getAttachmentName()
  {
      return this.attachmentName;
  }


}
