package pt.webdetails.cda.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.table.TableModel;

import org.apache.axis2.databinding.types.xsd.Decimal;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * JsonExporter
 * <p/>
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class XmlExporter extends AbstractExporter
{

  public XmlExporter(HashMap <String,String> extraSettings)
  {
    super();
  }


  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException
  {


    final Document document = DocumentHelper.createDocument();

    // Generate metadata

    final Element root = document.addElement("CdaExport");

    final Element metadata = root.addElement("MetaData");

    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();

    for (int i = 0; i < columnCount; i++)
    {
      final Element columnInfo = metadata.addElement("ColumnMetaData");
      columnInfo.addAttribute("index", (String.valueOf(i)));
      columnInfo.addAttribute("type", getColType(tableModel.getColumnClass(i)));
      columnInfo.addAttribute("name", tableModel.getColumnName(i));

    }

    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    final Element resultSet = root.addElement("ResultSet");
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {

      final Element row = resultSet.addElement("Row");

      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        final Element col = row.addElement("Col");

        final Object value = tableModel.getValueAt(rowIdx, colIdx);
        if (value instanceof Date)
        {
          col.setText(format.format(value));
        }
        else if (value != null)
        {
          // numbers can be safely converted via toString, as they use a well-defined format there
          col.setText(value.toString());
        }
        else{
          col.addAttribute("isNull","true");
        }

      }
    }

    try
    {
      final Writer writer = new BufferedWriter(new OutputStreamWriter(out));

      document.setXMLEncoding("UTF-8");
      document.write(writer);
      writer.flush();
    }
    catch (IOException e)
    {
      throw new ExporterException("IO Exception converting to utf-8", e);
    }

  }

  public String getMimeType()
  {
    return "text/xml";
  }

  public String getAttachmentName()
  {
    // No attachment required
    return null;
  }
}
