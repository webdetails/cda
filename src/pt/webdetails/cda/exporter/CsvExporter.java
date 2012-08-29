/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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

  private static final Log logger = LogFactory.getLog(CsvExporter.class);
  private static final String DEFAULT_CSV_SEPARATOR_SETTING = ";";
  private static final String DEFAULT_CSV_ENCLOSURE_SETTING = "&quot;";
  public static final String CSV_SEPARATOR_SETTING = "csvSeparator";
  public static final String ATTACHMENT_NAME_SETTING = "attachmentName";
  public static final String COLUMN_HEADERS_SETTING = "columnHeaders";
  private String separator;

  private String enclosure;
  private String attachmentName;
  private String showColumnHeaders;
  
  public CsvExporter(HashMap<String, String> extraSettings) {
    if (extraSettings.containsKey(CSV_SEPARATOR_SETTING)) {
      this.separator = extraSettings.get(CSV_SEPARATOR_SETTING);
    } else {
      String sep = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.exporter.csv.Separator");
      this.separator = sep == null ? DEFAULT_CSV_SEPARATOR_SETTING : sep;
    }

    if (extraSettings.containsKey(ATTACHMENT_NAME_SETTING)) {
      this.attachmentName = extraSettings.get(ATTACHMENT_NAME_SETTING);
    } else {
      this.attachmentName = "cda-export.csv";
    }

    if (extraSettings.containsKey(COLUMN_HEADERS_SETTING)) {
      this.showColumnHeaders = extraSettings.get(COLUMN_HEADERS_SETTING).equalsIgnoreCase("false") ? "N" : "Y";
    } else {
      this.showColumnHeaders = "Y";
    }   

    String enc = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.exporter.csv.Enclosure");
    this.enclosure = enc == null ? DEFAULT_CSV_ENCLOSURE_SETTING : enc;

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