package pt.webdetails.cda.exporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class CsvExporter extends AbstractKettleExporter
{

  private static final Log logger = LogFactory.getLog(CsvExporter.class);


  public CsvExporter()
  {
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
        "    <separator>;</separator>\n" +
        "    <enclosure>&quot;</enclosure>\n" +
        "    <enclosure_forced>N</enclosure_forced>\n" +
        "    <header>Y</header>\n" +
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
    return "cda-export.csv";
  }

  public String getType()
  {
    return "csv";
  }


}