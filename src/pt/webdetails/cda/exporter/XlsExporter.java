package pt.webdetails.cda.exporter;

import java.util.HashMap;

import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class XlsExporter extends AbstractKettleExporter
{

  public static final String ATTACHMENT_NAME_SETTING = "attachmentName";
  public static final String TEMPLATE_NAME_SETTING = "templateName";
  
  private String attachmentName;
  private String templateName;

  public XlsExporter(HashMap <String,String> extraSettings)
  {

    this.attachmentName = getSetting(extraSettings, ATTACHMENT_NAME_SETTING, "cda-export." + getType());
    this.templateName = getSetting(extraSettings, TEMPLATE_NAME_SETTING, null);
    if(templateName != null && !templateName.startsWith("/")){
      templateName = RepositoryAccess.getSolutionPath(templateName);
    }
  }

  protected String getExportStepDefinition(String name)
  {
    StringBuilder xml = new StringBuilder();


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
        "    <header>Y</header>\n" +
        "    <footer>N</footer>\n" +
        "    <encoding/>\n" +
        "    <append>N</append>\n" +
        "    <add_to_result_filenames>Y</add_to_result_filenames>\n" +
        "    <file>\n" +
        "      <name>${java.io.tmpdir}&#47;");

    xml.append(getFileName());

    //TODO: path for template needs change?

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
        "    <fields>\n" + //TODO: if we want date formatting, this looks like the place
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