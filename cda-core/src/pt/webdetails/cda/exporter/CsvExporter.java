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
import pt.webdetails.cda.CdaBoot;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class CsvExporter extends AbstractKettleExporter
{
  
  public static final String CSV_SEPARATOR_SETTING = "csvSeparator";
  public static final String CSV_QUOTE_SETTING = "csvQuote";

  private static final Log logger = LogFactory.getLog(CsvExporter.class);
  private static final String DEFAULT_CSV_SEPARATOR_SETTING = ";";
  private static final String DEFAULT_CSV_ENCLOSURE_SETTING = "&quot;";
  private String separator;

  private String enclosure;
  private String attachmentName;
  private String showColumnHeaders;
  
  public CsvExporter(HashMap<String, String> extraSettings) {
    super(extraSettings);
    
    this.separator = getSetting( 
        CSV_SEPARATOR_SETTING, 
        CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.exporter.csv.Separator", DEFAULT_CSV_SEPARATOR_SETTING));

    this.enclosure = getSetting(
        CSV_QUOTE_SETTING,
        CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.exporter.csv.Enclosure", DEFAULT_CSV_ENCLOSURE_SETTING));
    
    this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "cda-export.csv");

    this.showColumnHeaders = Boolean.parseBoolean(getSetting(COLUMN_HEADERS_SETTING, "true")) ? "Y" : "N";

    logger.debug("Initialized CsvExporter with attachement filename '" + attachmentName + "'");
    logger.debug("Initialized CsvExporter with enclosure '" + enclosure + "'");
    logger.debug("Initialized CsvExporter with show columns '" + showColumnHeaders.toString() + "'");

    logger.debug("Initialized CsvExporter with separator '" + separator + "'");

  }

  protected String getExportStepDefinition(final String name)
  {
    StringBuilder xml = new StringBuilder();


    xml.append("<step>\n" +
        "    <name>"+ name + "</name>\n" +
        "    <type>TextFileOutput</type>\n" +
        "    <description/>\n" +
        "    <distribute>Y</distribute>\n" +
        "    <copies>1</copies>\n" +
        "         <partitioning>\n" +
        "           <method>none</method>\n" +
        "           <schema_name/>\n" +
        "           </partitioning>\n" +
        "    <separator>" + this.separator + "</separator>\n" +
        "    <enclosure>" + this.enclosure + "</enclosure>\n" +
        "    <enclosure_forced>Y</enclosure_forced>\n" +
        "    <header>" + this.showColumnHeaders + "</header>\n" +
        "    <footer>N</footer>\n" +
        "    <format>DOS</format>\n" +
        "    <compression>None</compression>\n" +
        "    <encoding/>\n" +
        "    <endedLine/>\n" +
        "    <fileNameInField>N</fileNameInField>\n" +
        "    <fileNameField/>\n" +
        "    <file>\n" +
        "      <name>${java.io.tmpdir}&#47;");

    xml.append(getFileName());


    xml.append("</name>\n" +
        "      <is_command>N</is_command>\n" +
        "      <do_not_open_new_file_init>N</do_not_open_new_file_init>\n" +
        "      <extention>csv</extention>\n" +
        "      <append>N</append>\n" +
        "      <split>N</split>\n" +
        "      <haspartno>N</haspartno>\n" +
        "      <add_date>N</add_date>\n" +
        "      <add_time>N</add_time>\n" +
        "      <SpecifyFormat>N</SpecifyFormat>\n" +
        "      <date_time_format/>\n" +
        "      <add_to_result_filenames>Y</add_to_result_filenames>\n" +
        "      <pad>N</pad>\n" +
        "      <fast_dump>N</fast_dump>\n" +
        "      <splitevery>0</splitevery>\n" +
        "    </file>\n" +
        "    <fields>\n" +
        "    </fields>\n" +
        "     <cluster_schema/>\n" +
        " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n" +
        "      <xloc>474</xloc>\n" +
        "      <yloc>139</yloc>\n" +
        "      <draw>Y</draw>\n" +
        "      </GUI>\n" +
        "    </step>");

    return xml.toString();

  }

  public String getMimeType()
  {
    return "text/csv";
  }

  public String getAttachmentName()
  {
    return this.attachmentName;
  }

  public String getType()
  {
    return "csv";
  }


}
